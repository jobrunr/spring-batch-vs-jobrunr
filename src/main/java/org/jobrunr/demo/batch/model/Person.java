package org.jobrunr.demo.batch.model;

public record Person(String firstName, String lastName) {

    public Person transform() {
        return new Person(firstName.toUpperCase(), lastName.toUpperCase());
    }
}
