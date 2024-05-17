package com.premiumminds.internship.teknonymy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;


@RunWith(JUnit4.class)
public class TeknonymyServiceTest {

    /**
     * The corresponding implementations to test.
     *
     * If you want, you can make others :)
     *
     */
    public TeknonymyServiceTest() {
    }

    ;

    @Test
    public void PersonNoChildrenTest() {
        Person person = new Person("John", 'M', null, LocalDateTime.of(1046, 1, 1, 0, 0));
        String result = new TeknonymyService().getTeknonymy(person);
        String expected = "";
        assertEquals(expected, result);
    }

    @Test
    public void PersonOneChildTest() {
        Person person = new Person("John", 'M', new Person[]{
                new Person("Holy", 'F', null, LocalDateTime.of(1046, 1, 1, 0, 0))
        },
                LocalDateTime.of(1046, 1, 1, 0, 0));
        String result = new TeknonymyService().getTeknonymy(person);
        String expected = "father of Holy";
        assertEquals(result, expected);
    }
}