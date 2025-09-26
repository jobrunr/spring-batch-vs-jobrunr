package org.jobrunr.demo.batch.common;

public record Person(String firstName, String lastName) {

    public Person transform() {
        return new Person(firstName.toUpperCase(), lastName.toUpperCase());
    }
}
