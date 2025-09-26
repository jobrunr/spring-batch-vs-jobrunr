package org.jobrunr.demo.batch.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/common")
public class CommonApiController {

    @GetMapping("/has-a-lot-of-person-data")
    public ResponseEntity<?> hasALotOfPersonData() {
        if (Files.exists(Path.of("./src/main/resources/a-lot-of-person-data.csv"))) {
            return ResponseEntity.ok("CSV File a-lot-person-data.csv exists");
        }
        return ResponseEntity.status(NOT_FOUND).body("CSV File a-lot-person-data.csv does NOT exists");
    }

}
