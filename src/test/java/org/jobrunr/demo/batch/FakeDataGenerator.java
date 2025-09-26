package org.jobrunr.demo.batch;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import net.datafaker.Faker;
import net.datafaker.providers.base.Name;
import org.jobrunr.demo.batch.common.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;

public class FakeDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeDataGenerator.class);

    public static void main(String[] args) {
        ObjectWriter csvWriter = new CsvMapper()
                .writer(CsvSchema.builder()
                        .addColumn("firstName")
                        .addColumn("lastName")
                        .build());

        Faker faker = new Faker();
        try (FileWriter fileWriter = new FileWriter("./src/main/resources/a-lot-of-person-data.csv")) {
            SequenceWriter sequenceWriter = csvWriter.writeValues(fileWriter);
            for (int i = 0; i < 10_000_000; i++) {
                Name name = faker.name();
                sequenceWriter.write(new Person(name.firstName(), name.lastName()));
                if (i % 1000 == 0) {
                    LOGGER.info("Generated {} rows in a-lot-of-person-data.csv", i);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
