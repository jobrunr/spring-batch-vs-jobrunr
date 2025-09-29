## Batch processing using Spring Batch and JobRunr

This example project shows how to perform ETL jobs using both Spring Batch and JobRunr.

In this example, we follow the [example guide from Spring Batch](https://spring.io/guides/gs/batch-processing) where 
a csv file is read, transformed and then imported in a table.

#### Prerequisites
This project requires a running docker installation. Spring Boot, using `spring-boot-docker-compose`, will automatically launch a postgres docker database. 


### How to use
#### Basic example
- Start the [JobRunrSpringBatchApplication](src/main/java/org/jobrunr/demo/batch/JobRunrSpringBatchApplication.java) in your preferred IDE.
- navigate to [http://localhost:8080/](http://localhost:8080/)
- And choose how you want to import the [person-data.csv](src/main/resources/person-data.csv) using either Spring Batch or JobRunr

#### The 10 million row challenge:
You can also test an import with a file using 10 million rows. To do so:
- just run the main method from the [FakeDataGenerator](./src/test/java/org/jobrunr/demo/batch/FakeDataGenerator.java) (it is a bit too big for GitHub)
- navigate again to [http://localhost:8080/](http://localhost:8080/) which will show extra links to import the file with 10 million rows

##### Results:

We ran the 10 million row challenge on three different machines to compare performance.

| Machine | Spring Batch | JobRunr |
| :--- | :--- | :--- |
| **MacBook M4 Pro (48GB RAM)** | 2m 22s | **1m 59s** |
| **MacBook M3 Max (64GB RAM)** | 4m 31s | **3m 30s** |
| **Lightnode VPS (16 vCPU, 32GB)**| 11m 33s | **7m 55s** |


### Code Structure
- the model can be found in [src/main/java/org/jobrunr/demo/batch/common](src/main/java/org/jobrunr/demo/batch/common)
- the Spring Batch implementation can be found in [src/main/java/org/jobrunr/demo/batch/spring](src/main/java/org/jobrunr/demo/batch/spring)
- the JobRunr implementation can be found in [src/main/java/org/jobrunr/demo/batch/jobrunr](src/main/java/org/jobrunr/demo/batch/jobrunr)

> Note: as ETL jobs are not part of JobRunr (yet), we added some untested boilerplate code in [src/main/java/org/jobrunr/jobs/etl](./src/main/java/org/jobrunr/jobs/etl).
> Drop us a note if you think this should be a built-in feature in JobRunr.
