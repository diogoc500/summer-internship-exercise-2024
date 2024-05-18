package com.premiumminds.internship.teknonymy;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Challenge note:
 * "I implemented both an iterative and a recursive solution. Both of them are O(n). Both take
 * almost the same time. I decided to present the iterative version for two reasons:
 * - It is more scalable. The recursive version can throw stack overflows if the tree is too tall.
 * - The recursive version has to use an additional data record.
 *
 * I still left the recursive solution because it is interesting to compare both approaches - while the iterative
 * version uses a breadth-first approach, the recursive version uses a depth first approach.
 *
 * To use the recursive version just uncomment the commented line in 'getTeknonymy'.
 *
 * I also developed a parallel version of the recursive code to take advantage of multicore systems. However, for
 * some reason, the object returned by 'Executors.newFixedThreadPool(n)' would hang if too many task submissions
 * were made to it, so even though the code was correct, its completion would depend on 'n'. To choose a correct value
 * for 'n', one would have to calculate the number of nodes in the tree before allocating threads. With the current
 * implementation of the tree, it is not faster to find the number of nodes and then execute the parallel version
 * than it is to just execute one of the sequential algorithms, so I decided not to include the parallel version in my
 * final submission."
 */

class TeknonymyService implements ITeknonymyService {

    /**
     * Method to get a Person Teknonymy Name
     * @param person person
     * @return String which is the Teknonymy Name
     */
    @Override
    public String getTeknonymy(Person person){
        return getTeknonymy_iter(person);
        // return getTeknonymy_rec(person);

        // TODO: REMOVE
        // return getTeknonymy_par(person);
    }

    // ===== ITERATIVE SOLUTION =====

    /**
     * Method to get a Person Teknonymy Name.
     * Finds the oldest of the farthest child of the 'person' tree, computes the teknonym and returns it.
     *
     * This method uses breadth-first search.
     *
     * @param person person
     * @return String which is the Teknonymy Name
     */
    private String getTeknonymy_iter(Person person) {
        Person oldestPerson = null;
        int oldestPersonDepth = -1; // The root will be considered to be at depth '0'

        // Breadth-First Search
        Queue<Person> currentChildren = new LinkedList<>(); // Current tree level being searched
        Queue<Person> nextChildren = new LinkedList<>(); // Buffer for the next tree level

        currentChildren.add(person);

        Person currentChild;
        while (!currentChildren.isEmpty()) { // While there are still nodes to search through
            oldestPerson = null;
            oldestPersonDepth++;

            while ((currentChild = currentChildren.poll()) != null) { // While current level still has nodes to search
                if (currentChild.children() != null) // Current node's children will be searched on the next iteration
                    nextChildren.addAll(List.of(currentChild.children()));

                if (oldestPerson == null || currentChild.isOlder(oldestPerson)) // Best candidate for the current level
                    oldestPerson = currentChild;
            }

            // Switch buffers
            Queue<Person> aux = currentChildren;
            currentChildren = nextChildren;
            nextChildren = aux;
        }

        return buildTeknonymy(person, oldestPerson, oldestPersonDepth);
    }

    // ===== RECURSIVE SOLUTION =====

    /**
     * Entrance method for the recursive 'getFarthestOldestChild' method.
     * @param person person
     * @return String which is the Teknonymy Name
     */
    public String getTeknonymy_rec(Person person){
        ChildDepth cd = getFarthestOldestChild(new ChildDepth(person, -1));
        return buildTeknonymy(person, cd.person, cd.depth);
    }

    /**
     * Aux. data class for the recursive algorithm.
     * @param person person
     * @param depth depth relative to the bottom of the tree, not the root.
     */
    private record ChildDepth(Person person, int depth) {
    }

    /**
     * Finds the oldest of the farthest child of the subtree which root is childDepth.person.
     * This algorithm uses recursive depth-first search. The thought process is that the result of the current node is a
     * computation made over the results of the subtrees of each of its children.
     *
     * @param childDepth Data object that contains the root of a subtree
     * @return ChildDepth object corresponding to the solution of the subtree in childDepth.
     */
    private ChildDepth getFarthestOldestChild(ChildDepth childDepth) {
        if (childDepth.person.children() == null) // Base case, node with no children (leaf)
            return new ChildDepth(childDepth.person, 0);

        Person oldestChild = null;
        int depthOfOldestChild = -1;
        for (Person child : childDepth.person.children()) {
            // Depth '-1' is used for the downwards propagation phase, but any number would do.
            ChildDepth cd = getFarthestOldestChild(new ChildDepth(child, -1));
            if (oldestChild == null){
                oldestChild = cd.person;
                depthOfOldestChild = cd.depth;
            }

            // The chosen child should be the oldest of the deepest.
            if (depthOfOldestChild < cd.depth) {
                oldestChild = cd.person;
                depthOfOldestChild = cd.depth;
            } else if (depthOfOldestChild == cd.depth && cd.person.isOlder(oldestChild))
                oldestChild = cd.person;
        }

        return new ChildDepth(oldestChild, depthOfOldestChild + 1);
    }

    // TODO: REMOVE
    // ===== PARALLEL VERSION =====
    /*public String getTeknonymy_par(Person person){
        ChildDepth cd = null;
        try{
            cd = new PersonTask(null).getTeknonymy_par(person);
        } catch (Exception e){
            System.err.println("Could not complete parallel version of getTeknonymy.\n");
            e.printStackTrace();

            return "N/A";
        }
        return buildTeknonymy(person, cd.person, cd.depth);
    }

    private class PersonTask implements Callable<ChildDepth> {
        private static ExecutorService taskExec = null;
        private final ChildDepth childDepth;

        public PersonTask(ChildDepth childDepth) {
            this.childDepth = childDepth;
        }

        private static int numOfNodes(Person person){
            if(person.children() == null)
                return 1;

            int count = 0;
            for (Person child: person.children())
                count += numOfNodes(child);

            return count + 1;
        }

        public ChildDepth getTeknonymy_par(Person person) throws Exception {
            int n = numOfNodes(person);
            taskExec = Executors.newFixedThreadPool(n);
            return taskExec.submit(new PersonTask(new ChildDepth(person, -1))).get();
        }

        @Override
        public ChildDepth call() throws Exception {
            if (childDepth.person.children() == null)
                return new ChildDepth(childDepth.person, 0);

            Person oldestChild = null;
            int depthOfOldestChild = -1;

            List<Future<ChildDepth>> futures = new LinkedList<>();
            for(Person child : childDepth.person.children())
                futures.add(taskExec.submit(new PersonTask(new ChildDepth(child, -1))));

            for(Future<ChildDepth> childDepthFuture: futures){
                ChildDepth cd = childDepthFuture.get();
                if (oldestChild == null){
                    oldestChild = cd.person;
                    depthOfOldestChild = cd.depth;
                }

                // The chosen child should not only be the deepest, but the oldest.
                if (depthOfOldestChild < cd.depth) {
                    oldestChild = cd.person;
                    depthOfOldestChild = cd.depth;
                } else if (depthOfOldestChild == cd.depth && cd.person.isOlder(oldestChild))
                    oldestChild = cd.person;
            }

            return new ChildDepth(oldestChild, depthOfOldestChild + 1);
        }
    }*/

    // ===== AUX METHODS =====
    /**
     * Builds the Teknonymy of a person given their 'descendant' and their depth in the family tree.
     *
     * @param person person.
     * @param descendant Descendant of the person.
     * @param depth Depth at which the 'descendant' is at.
     * @return Teknonymy
     */
    private String buildTeknonymy(Person person, Person descendant, int depth) {
        StringBuilder degree = new StringBuilder();

        if (depth >= 1)
            degree.insert(0, person.getParenthood());

        if (depth >= 2)
            degree.insert(0, "grand");

        for (int i = 3; i <= depth; i++)
            degree.insert(0, "great-");

        return depth == 0 ? "" : degree + " of " + descendant.name();
    }
}
