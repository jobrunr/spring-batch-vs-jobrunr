#!/bin/bash
# Usage: ./benchmark/run.sh <spring|jobrunr> <runlabel>
# One run: fresh Postgres, fresh JVM, import the 10M row CSV, record timings + row count.
set -u
VARIANT=$1
LABEL=$2
REPO=$(cd "$(dirname "$0")/.." && pwd)
JAVA=${JAVA_HOME:+$JAVA_HOME/bin/}java
OUT=$REPO/build/benchmark; mkdir -p $OUT
RESULTS=$OUT/results.csv; [ -f $RESULTS ] || echo "label,variant,status,process_s,end_to_end_s,rows,non_upper_rows,converting_log_lines,found_line,finished_at" > $RESULTS
LOG=$OUT/run-$LABEL.log
PORT=8081
PG=spring-batch-vs-jobrunr-postgres-1
FILE=./src/main/resources/a-lot-of-person-data.csv
psqlq() { docker exec $PG psql -U batch-user -d batch -At -c "$1"; }

cd $REPO
docker compose down -v >/dev/null 2>&1

$JAVA -jar build/libs/jobrunr-spring-batch-0.0.1-SNAPSHOT.jar --server.port=$PORT > $LOG 2>&1 &
for i in $(seq 1 90); do
  curl -s -o /dev/null -w '%{http_code}' localhost:$PORT/common/has-a-lot-of-person-data 2>/dev/null | grep -q 200 && break
  sleep 1
done
sleep 10  # let startup tasks settle

T0=$(python3 -c 'import time;print(time.time())')
if [ "$VARIANT" = spring ]; then
  curl -s -m 3600 -X POST "localhost:$PORT/spring/start-batch-import?file=$FILE" > $OUT/resp-$LABEL.txt
  T1=$(python3 -c 'import time;print(time.time())')
  DUR=$(psqlq "select extract(epoch from (end_time - start_time)) from batch_job_execution order by job_execution_id desc limit 1")
  STATUS=$(psqlq "select status from batch_job_execution order by job_execution_id desc limit 1")
  E2E=$(python3 -c "print(round($T1-$T0,3))")
else
  curl -s -X POST "localhost:$PORT/jobrunr/start-batch-import?file=$FILE" > $OUT/resp-$LABEL.txt
  while true; do
    S=$(psqlq "select state from jobrunr_jobs limit 1")
    [ "$S" = SUCCEEDED ] || [ "$S" = FAILED ] && break
    sleep 1
  done
  T1=$(python3 -c 'import time;print(time.time())')
  STATUS=$S
  JSON=$(psqlq "select jobasjson from jobrunr_jobs limit 1")
  DUR=$(echo "$JSON" | python3 -c "import json,sys; h=json.load(sys.stdin)['jobHistory']; print([s for s in h if s['state']=='SUCCEEDED'][-1]['processDuration'])")
  E2E=$(echo "$JSON" | python3 -c "import json,sys; h=json.load(sys.stdin)['jobHistory']; s=[x for x in h if x['state']=='SUCCEEDED'][-1]; print(round(s['processDuration']+s['latencyDuration'],3))")
fi
ROWS=$(psqlq "select count(*) from people")
DISTINCT_UPPER=$(psqlq "select count(*) from people where first_name <> upper(first_name) or last_name <> upper(last_name)")
LOGLINES=$(grep -c "Converting (" $LOG)
FOUND=$(grep "Found .* person records" $LOG | tail -1 | sed 's/.*: //')
echo "$LABEL,$VARIANT,$STATUS,$DUR,$E2E,$ROWS,$DISTINCT_UPPER,$LOGLINES,$FOUND,$(date +%H:%M:%S)" | tee -a $RESULTS

APP=$(lsof -nP -iTCP:$PORT -sTCP:LISTEN -t)
[ -n "$APP" ] && kill $APP
for i in $(seq 1 60); do kill -0 $APP 2>/dev/null || break; sleep 1; done
grep -E "completed with|Exception|ERROR" $LOG | cut -c1-400 | grep -v "Converting" | tail -5 > $OUT/run-$LABEL.summary
rm -f $LOG
docker compose down -v >/dev/null 2>&1
