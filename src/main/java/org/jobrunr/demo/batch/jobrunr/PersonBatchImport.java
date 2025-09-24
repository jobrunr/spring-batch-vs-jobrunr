package org.jobrunr.demo.batch.jobrunr;

import org.jobrunr.demo.batch.model.Person;
import org.jobrunr.jobs.context.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PersonBatchImport {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonBatchImport.class);
    private static final int batchSize = 10;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public PersonBatchImport(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public void importUsers(String file, JobContext jobContext) throws Exception {
        ProgressiblePersonReader personReader = new ProgressiblePersonReader(file, jobContext);
        while (personReader.hasMorePersons()) {
            List<Person> persons = personReader.read(batchSize);
            savePersons(transformPersons(persons));
            personReader.increaseProgress(persons.size());
            LOGGER.info("Processed batch: {}", persons.size());
        }
        verifyResults();
    }

    private void verifyResults() {
        LOGGER.info("!!! JOB FINISHED! Time to verify the results");
        Integer count = namedParameterJdbcTemplate.getJdbcTemplate().queryForObject("select count(*) FROM people", Integer.class);
        LOGGER.info("Found {} person records in the database", count);
    }

    private List<Person> transformPersons(List<Person> persons) {
        return persons.stream().map(this::transformPerson).toList();
    }

    private Person transformPerson(Person person) {
        final Person transformedPerson = person.transform();
        LOGGER.info("Converting ({}) into ({})", person, transformedPerson);
        return transformedPerson;
    }

    private void savePersons(List<Person> persons) {
        String sql = "INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)";
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(persons.toArray());
        namedParameterJdbcTemplate.batchUpdate(sql, batch);
    }
}
