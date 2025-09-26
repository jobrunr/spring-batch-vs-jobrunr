package org.jobrunr.demo.batch.jobrunr;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.jobrunr.demo.batch.common.Person;
import org.jobrunr.jobs.etl.FiniteStream;
import org.jobrunr.jobs.etl.JobRunrEtlTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class PersonMigrationTask extends JobRunrEtlTask<String, String, Person> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonMigrationTask.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ObjectReader csvReader;

    public PersonMigrationTask(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.csvReader = new CsvMapper()
                .readerFor(Person.class)
                .with(CsvSchema.builder()
                        .addColumn("firstName")
                        .addColumn("lastName")
                        .build());
    }

    // FiniteStream has a performance impact since it counts all lines in the file but shows a progressbar when importing files
    @Override
    protected FiniteStream<String> extract() throws Exception {
        String personFileToImport = getContext();
        return FiniteStream.usingStreamCount(() -> Files.lines(Path.of(personFileToImport)));
    }

    @Override
    protected Person transform(String csvLine) throws Exception {
        Person person = csvReader.readValue(csvLine);
        final Person transformedPerson = person.transform();
        LOGGER.info("Converting ({}) into ({})", person, transformedPerson);
        return transformedPerson;
    }

    @Override
    protected void load(List<Person> toSave) {
        String sql = "INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)";
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(toSave.toArray());
        namedParameterJdbcTemplate.batchUpdate(sql, batch);
    }

    @Override
    protected void onEnd() {
        LOGGER.info("!!! JOB FINISHED! Time to verify the results");
        Integer count = namedParameterJdbcTemplate.getJdbcTemplate().queryForObject("select count(*) FROM people", Integer.class);
        LOGGER.info("Found {} person records in the database", count);
    }
}
