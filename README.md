## Batch processing using Spring Batch and JobRunr

This example project shows how to perform ETL jobs using both Spring Batch and JobRunr.

In this example, we follow the [example guide from Spring Batch](https://spring.io/guides/gs/batch-processing) where 
a csv file is read, transformed and then imported in a table.

It accompanies the blog post [Spring Batch vs. JobRunr](https://www.jobrunr.io/en/blog/spring-batch-vs-jobrunr/).

#### Versions
| | Version |
| :--- | :--- |
| Java | 25 |
| Spring Boot | 4.1.2 |
| Spring Batch | 6.0.6 (managed by Spring Boot) |
| JobRunr | 9.0.3 (`jobrunr-spring-boot-4-starter`) |
| PostgreSQL | 17 (Docker) |
| Gradle | 9.8.2 (wrapper) |

The 2025 version of this demo (Spring Boot 3.5, Spring Batch 5.2, JobRunr 8.0) is commit `bab63a6` in the git history.

#### Prerequisites
- Java 25
- A running Docker installation. Spring Boot, using `spring-boot-docker-compose`, will automatically launch a Postgres container from [compose.yml](compose.yml) and wire the datasource. The container uses a random free host port, so the demo also starts when you already have Postgres listening on 5432.

### How to use
#### Basic example
- Start the [JobRunrSpringBatchApplication](src/main/java/org/jobrunr/demo/batch/JobRunrSpringBatchApplication.java) in your preferred IDE, or run `./gradlew bootRun`.
- navigate to [http://localhost:8080/](http://localhost:8080/)
- And choose how you want to import the [person-data.csv](src/main/resources/person-data.csv) using either Spring Batch or JobRunr
- The JobRunr dashboard runs on [http://localhost:8000/dashboard](http://localhost:8000/dashboard)

#### The 10 million row challenge:
You can also test an import with a file using 10 million rows. To do so:
- just run the main method from the [FakeDataGenerator](./src/test/java/org/jobrunr/demo/batch/FakeDataGenerator.java) (it is a bit too big for GitHub, so it is in `.gitignore`)
- navigate again to [http://localhost:8080/](http://localhost:8080/) which will show extra links to import the file with 10 million rows

To reproduce the numbers from the blog post, use [benchmark/run.sh](benchmark/run.sh). One call is one run: it wipes the Postgres container, starts the app as a jar on port 8081, imports the 10 million row file with one of the two implementations, verifies the row count and stops the app again.

```bash
./gradlew bootJar
./benchmark/run.sh spring sb1
./benchmark/run.sh jobrunr jr1
cat build/benchmark/results.csv
```

How the time is measured:
- **Spring Batch**: `end_time - start_time` of the job execution in `BATCH_JOB_EXECUTION` (the same number Spring Batch logs as `completed ... in 4m12s`).
- **JobRunr**: the `processDuration` of the `SUCCEEDED` state, which is the time between the job going to `PROCESSING` and `SUCCEEDED` (the number the dashboard shows). The time the job waits in the queue before a worker picks it up (at most the 5 second poll interval) is not included.

Both implementations are single threaded, use chunks of 100 rows, write with JDBC batch inserts and log every converted row at `INFO`, exactly like the Spring guide.

##### Results
###### MacBook Pro M4 Pro with 48GB of RAM (Java 25, Spring Boot 4.1.2, Spring Batch 6.0.6, JobRunr 9.0.3):
| | Run 1 | Run 2 | Run 3 | Median |
| :--- | :--- | :--- | :--- | :--- |
| Spring Batch 6.0.6 | 4m 02s | 4m 34s | 4m 38s | **4m 34s** |
| JobRunr 9.0.3 | 3m 13s | 3m 22s | 3m 21s | **3m 21s** |

Every run imported exactly 10,000,000 rows (checked with `select count(*) from people`).

### Upgrading from Spring Boot 3 / Spring Batch 5
What we had to change for Spring Boot 4, Spring Batch 6 and JobRunr 9:
- `spring-boot-starter-batch` became `spring-boot-starter-batch-jdbc`. In Spring Boot 4 the plain batch starter runs Spring Batch without a database (resourceless job repository), which would also drop the restart support the demo relies on.
- `spring-boot-starter-web` became `spring-boot-starter-webmvc`.
- `jobrunr-spring-boot-3-starter:8.0.2` became `jobrunr-spring-boot-4-starter:9.0.3`.
- Spring Batch 6 moved packages: `Job`, `JobExecution` and `JobExecutionException` to `org.springframework.batch.core.job`, `JobParameters(Builder)` to `org.springframework.batch.core.job.parameters`, `Step` to `org.springframework.batch.core.step`, `JobExecutionListener` to `org.springframework.batch.core.listener`, and all `org.springframework.batch.item.*` classes to `org.springframework.batch.infrastructure.item.*`.
- `chunk(100, transactionManager)` is deprecated for removal. We now use `chunk(100).transactionManager(transactionManager)`, which builds the new `ChunkOrientedStep`.
- `JobLauncher` is deprecated. The controller now uses `JobOperator#start(job, jobParameters)`.
- `JobExecution#getJobId()` is gone, we use `getJobInstanceId()`.
- Jackson 3 is the default in Spring Boot 4, so the CSV parsing in the JobRunr variant uses `tools.jackson.dataformat:jackson-dataformat-csv`.
- Gradle wrapper 8.14.3 to 9.8.2 (running Gradle on Java 25 needs Gradle 9.1 or later).

### Code Structure
- the model can be found in [src/main/java/org/jobrunr/demo/batch/common](src/main/java/org/jobrunr/demo/batch/common)
- the Spring Batch implementation can be found in [src/main/java/org/jobrunr/demo/batch/spring](src/main/java/org/jobrunr/demo/batch/spring)
- the JobRunr implementation can be found in [src/main/java/org/jobrunr/demo/batch/jobrunr](src/main/java/org/jobrunr/demo/batch/jobrunr)

> Note: as ETL jobs are not part of JobRunr (yet), we added some untested boilerplate code in [src/main/java/org/jobrunr/jobs/etl](./src/main/java/org/jobrunr/jobs/etl).
> Drop us a note if you think this should be a built-in feature in JobRunr.
