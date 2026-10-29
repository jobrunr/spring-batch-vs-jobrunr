package org.jobrunr.demo.batch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.docker.compose.skip.in-tests=false") // start Postgres from compose.yml for this test as well
class JobRunrSpringBatchApplicationTests {

    @Test
    void contextLoads() {
    }

}
