package org.jobrunr.demo.batch.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spring")
public class SpringBatchApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBatchApiController.class);
    private final JobLauncher jobLauncher;
    private final Job importUserJob;

    public SpringBatchApiController(JobLauncher jobLauncher, Job importUserJob) {
        this.jobLauncher = jobLauncher;
        this.importUserJob = importUserJob;
    }

    @PostMapping("/start-batch-import")
    public ResponseEntity<?> startBatchImportJob(@RequestParam(name = "file", defaultValue = "./src/main/resources/person-data.csv") String file) throws JobExecutionException, InterruptedException {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("file", file)
                    .toJobParameters();
            JobExecution jobExecution = jobLauncher.run(importUserJob, jobParameters);
            return ResponseEntity.ok("Created batch import job for file '" + file + "' with id '" + jobExecution.getJobId() + "'");
        } catch (JobExecutionException e) {
            LOGGER.error("Error starting Spring Batch importUserJob");
            throw e;
        }
    }
}
