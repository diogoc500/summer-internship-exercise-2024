package com.premiumminds.internship.teknonymy;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class TeknonymyService implements ITeknonymyService {

    /**
     * Method to get a Person Teknonymy Name
     *
     * @param person person
     * @return String which is the Teknonymy Name
     */
    public String getTeknonymy(Person person) {
        Person oldestPerson = null;
        int oldestPersonDepth = -1; // The root will be considered to be at depth '0'

        // Breadth-First Search
        Queue<Person> nextChildren = new LinkedList<>();
        Queue<Person> currentChildren = new LinkedList<>();

        currentChildren.add(person);

        Person currentChild;
        while (!currentChildren.isEmpty()) {
            oldestPerson = null;
            oldestPersonDepth++;

            while((currentChild = currentChildren.poll()) != null){
                if(currentChild.children() != null)
                    nextChildren.addAll(List.of(currentChild.children()));

                if(oldestPerson == null) // First child to be considered in 'nextChildren'
                    oldestPerson = currentChild;

                if(currentChild.isOlder(oldestPerson))
                    oldestPerson = currentChild;
            }

            // Switch buffers (TODO: Is it possible to only use one buffer?)
            Queue<Person> aux = currentChildren;
            currentChildren = nextChildren;
            nextChildren = aux;
        }

        return buildTeknonymy(person, oldestPerson, oldestPersonDepth);
    }

    /**
     * Builds the Teknonym of a person given their 'descendant' and their depth in the family tree.
     *
     * @param person person.
     * @param descendant Descendant of the person.
     * @param depth Depth at which the 'descendant' is at.
     * @return Teknonym
     */
    private String buildTeknonymy(Person person, Person descendant, int depth){
        StringBuilder degree = new StringBuilder();

        if(depth >= 1)
            degree.insert(0, person.getParenthood());

        if(depth >= 2)
            degree.insert(0, "grand");

        for(int i=3; i<=depth; i++)
            degree.insert(0, "great-");

        return depth == 0? "": degree + " of " + descendant.name();
    }

    /**
     * (WIP) Parallel version getTeknonymy that takes advantage of multicore processing.
     * @param person
     * @return String which is the Teknonymy Name
     */
    private String getTek_par(Person person) {
        // TODO: WIP. Maybe it will not be necessary.
        return "";
    }
}
