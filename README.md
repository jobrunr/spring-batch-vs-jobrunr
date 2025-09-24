## Batch processing using Spring Batch and JobRunr

This example project shows how to perform ETL jobs using both Spring Batch and JobRunr.

In this example, we follow the [example guide from Spring Batch](https://spring.io/guides/gs/batch-processing) where 
a csv file is read, transformed and then imported in a table.

#### Prerequisites
This project requires a running docker installation. Spring Boot, using `spring-boot-docker-compose`, will automatically launch a postgres docker database. 


### How to use
- Start the [JobRunrSpringBatchApplication](src/main/java/org/jobrunr/demo/batch/JobRunrSpringBatchApplication.java) in your preferred IDE.
- navigate to [http://localhost:8080/](http://localhost:8080/)
- And choose how you want to import the [person-data.csv](src/main/resources/person-data.csv)
  

### Code Structure
- the model can be found in [src/main/java/org/jobrunr/demo/batch/model](src/main/java/org/jobrunr/demo/batch/model)
- the Spring Batch implementation can be found in [src/main/java/org/jobrunr/demo/batch/spring](src/main/java/org/jobrunr/demo/batch/spring)
- the JobRunr implementation can be found in [src/main/java/org/jobrunr/demo/batch/jobrunr](src/main/java/org/jobrunr/demo/batch/jobrunr)
