package com.premiumminds.internship.teknonymy;

import java.time.LocalDateTime;

public record Person(String name, Character sex, Person[] children, LocalDateTime dateOfBirth) {

    /**
     * Checks if 'this' person is older than 'person'.
     * @param person person to compare with
     * @return true if 'this' person is older than 'person', false otherwise.
     */
    public boolean isOlder(Person person) {
        return this.dateOfBirth.isBefore(person.dateOfBirth);
    }

    /**
     * Get 'mother' or 'father', depending on the sex.
     * @return 'mother' if sex == 'F' or 'father' if sex == 'M'
     */
    public String getParenthood(){
        return switch (sex) {
            case 'M' -> "father";
            case 'F' -> "mother";
            default -> "N/A";
        };
    }
}
