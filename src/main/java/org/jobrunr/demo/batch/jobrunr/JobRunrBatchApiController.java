package org.jobrunr.demo.batch.jobrunr;

import org.jobrunr.jobs.JobId;
import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobrunr")
public class JobRunrBatchApiController {

    private final JobScheduler jobScheduler;

    public JobRunrBatchApiController(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    @PostMapping("/start-batch-import")
    public ResponseEntity<?> startBatchImportJob(@RequestParam(name = "file", defaultValue = "./src/main/resources/person-data.csv") String file) {
        JobId jobId = jobScheduler.<PersonBatchImport>enqueue(x -> x.importUsers(file, JobContext.Null));
        return ResponseEntity.ok("Created batch import job for file '" + file + "' with id '" + jobId + "'");
    }
}
