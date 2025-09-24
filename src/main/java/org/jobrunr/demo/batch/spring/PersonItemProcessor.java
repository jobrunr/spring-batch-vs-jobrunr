package org.jobrunr.demo.batch.spring;

import org.jobrunr.demo.batch.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class PersonItemProcessor implements ItemProcessor<Person, Person> {

    private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

    @Override
    public Person process(final Person person) {
        final Person transformedPerson = person.transform();

        log.info("Converting ({}) into ({})", person, transformedPerson);

        return transformedPerson;
    }

}
