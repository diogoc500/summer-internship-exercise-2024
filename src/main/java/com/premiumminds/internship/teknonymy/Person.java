package com.premiumminds.internship.teknonymy;

import java.time.LocalDateTime;

public record Person(String name, Character sex, Person[] children, LocalDateTime dateOfBirth) {
    public boolean isOlder(Person p) {
        return this.dateOfBirth.isBefore(p.dateOfBirth);
    }

    public String getParenthood(){
        return switch (sex) {
            case 'M' -> "father";
            case 'F' -> "mother";
            default -> "N/A";
        };
    }
}
