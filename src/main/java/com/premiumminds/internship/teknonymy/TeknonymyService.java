package com.premiumminds.internship.teknonymy;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exercise note:
 * "I implemented both an iterative and a recursive solution. Both of them are of O(n) time complexity. However, the
 * recursive version turned out to be the fastest. I noticed this after implementing the tests of the huge-complete trees.
 * When I was testing with tests that were only taking < 10ms, they seemed to be performing the same, so, at first, I decided
 * to present the iterative version for two reasons:
 * - It is more scalable. The recursive version can throw stack overflows if the tree is too tall.
 * - The recursive version has to use an additional data record.
 *
 * Even though the 2nd 'problem' still remains, the first turned out to be fine because for a realistic number of generations
 * and children, the program actually runs out of heap space first (too many nodes to allocate) than it throws a stack
 * overflow exception. Actually, stack overflows, on the recursive version, depends solely on the number of generations.
 * After testing for trees with 1 child each but with 1000 generations, I concluded that stack overflows were not an issue
 * for this exercise.
 *
 * In the end, I decided to present the recursive version as my solution.
 *
 * I still left the iterative solution because it is interesting to compare both approaches - while the iterative
 * version uses a breadth-first approach, the recursive version uses a depth-first approach.
 *
 * To use the iterative version just uncomment the commented line in 'getTeknonymy'.
 *
 * I also developed a recursive parallel version, but it turned out to be slower because of thread creation overhead.
 * Trying to limit the number of threads to some 'n' number and doing the sequential version of the code if no more
 * threads were available proved to be too complex for the scope of this exercise. In the end, I decided not to
 * submit the parallel version."
 */

class TeknonymyService implements ITeknonymyService {

    /**
     * Method to get a Person Teknonymy Name
     * @param person person
     * @return String which is the Teknonymy Name
     */
    @Override
    public String getTeknonymy(Person person) {
        return getTeknonymy_rec(person);
        // return getTeknonymy_iter(person);

        // TODO: Remove
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
     * Aux. data class for the recursive algorithm.
     * @param person person
     * @param depth depth relative to the bottom of the tree, not the root.
     */
    private record ChildDepth(Person person, int depth) {
    }

    /**
     * Entrance method for the recursive 'getFarthestOldestChild' method.
     * @param person person
     * @return String which is the Teknonymy Name
     */
    public String getTeknonymy_rec(Person person) {
        ChildDepth cd = getFarthestOldestChild(new ChildDepth(person, -1));
        return buildTeknonymy(person, cd.person, cd.depth);
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
            if (oldestChild == null) {
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
    /*public String getTeknonymy_par(Person person) {
        ChildDepth cd = null;
        try {
            cd = new PersonTask(null).getTeknonymy_par(person);
        } catch (Exception e) {
            System.err.println("Could not complete parallel version of getTeknonymy.\n");
            e.printStackTrace();

            return "N/A";
        }
        return buildTeknonymy(person, cd.person, cd.depth);
    }

    private class PersonTask implements Callable<ChildDepth> {

        private static final int numProcessors = Runtime.getRuntime().availableProcessors();
        private static final ExecutorService taskExec = Executors.newFixedThreadPool(numProcessors);

        private final ChildDepth childDepth;

        public PersonTask(ChildDepth childDepth) {
            this.childDepth = childDepth;
        }

        public ChildDepth getTeknonymy_par(Person person) throws Exception {
            return taskExec.submit(new PersonTask(new ChildDepth(person, -1))).get();
        }

        @Override
        public ChildDepth call() throws Exception {
            if (childDepth.person.children() == null)
                return new ChildDepth(childDepth.person, 0);

            Person oldestChild = null;
            int depthOfOldestChild = -1;

            List<Future<ChildDepth>> futures = new LinkedList<>();
            for (Person child : childDepth.person.children())
                futures.add(taskExec.submit(new PersonTask(new ChildDepth(child, -1))));

            for (Future<ChildDepth> childDepthFuture : futures) {
                ChildDepth cd = childDepthFuture.get();
                if (oldestChild == null) {
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
    private static String buildTeknonymy(Person person, Person descendant, int depth) {
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
