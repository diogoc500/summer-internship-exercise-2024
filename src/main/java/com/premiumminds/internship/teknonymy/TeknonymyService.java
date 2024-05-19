package com.premiumminds.internship.teknonymy;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Exercise note:
 * "I implemented both an iterative and a recursive solution. Both of them are of O(n) time complexity. However, the
 * recursive version turned out to be the fastest. I noticed this after implementing the tests of the huge-complete trees.
 *
 * When I was using tests that were only taking < 10ms, they seemed to be performing the same, so, at first, I decided
 * to present the iterative version as my main solution for two reasons:
 * - It is more scalable. The recursive version can throw stack overflows if the tree is too tall.
 * - The recursive version has to use an additional data record.
 *
 * Even though the 2nd 'problem' still remains, the first turned out to be fine because for a realistic number of generations
 * and children, the program actually runs out of heap space first (too many nodes to allocate) than it throws a stack
 * overflow exception. Actually, stack overflows, on the recursive version, depend solely on the number of generations.
 * After testing for trees with 1 child each but with 1000 generations, I concluded that stack overflows were not an issue
 * for this exercise.
 *
 * In the end, I decided to present the recursive version as my main solution.
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
    }

    // ===== RECURSIVE SOLUTION =====

    /**
     * Aux. data class for the recursive algorithm.
     * @param person person
     * @param depth depth relative to the bottom of the tree, not the root.
     */
    private record PersonDepth(Person person, int depth) {
    }

    /**
     * Entrance method for the recursive 'getFarthestOldestChild' method.
     * @param person person
     * @return String which is the Teknonymy Name
     */
    public String getTeknonymy_rec(Person person) {
        PersonDepth pd = getFarthestOldestChild(new PersonDepth(person, -1));
        return buildTeknonymy(person, pd.person, pd.depth);
    }

    /**
     * Finds the oldest of the farthest child of the subtree which root is childDepth.person.
     * This algorithm uses recursive depth-first search. The thought process is that the result of the current node is a
     * computation made over the results of the subtrees of each of its children.
     *
     * @param personDepth Data object that contains the root of a subtree
     * @return ChildDepth object corresponding to the solution of the subtree in childDepth.
     */
    private PersonDepth getFarthestOldestChild(PersonDepth personDepth) {
        if (personDepth.person.children() == null) // Base case, node with no children (leaf)
            return new PersonDepth(personDepth.person, 0);

        Person oldestChild = null;
        int depthOfOldestChild = -1;
        for (Person child : personDepth.person.children()) {
            // Depth '-1' is used for the downwards propagation phase, but any number would do.
            PersonDepth pd = getFarthestOldestChild(new PersonDepth(child, -1));
            if (oldestChild == null) {
                oldestChild = pd.person;
                depthOfOldestChild = pd.depth;
            }

            // The chosen child should be the oldest of the deepest.
            if (depthOfOldestChild < pd.depth) {
                oldestChild = pd.person;
                depthOfOldestChild = pd.depth;
            } else if (depthOfOldestChild == pd.depth && pd.person.isOlder(oldestChild))
                oldestChild = pd.person;
        }

        return new PersonDepth(oldestChild, depthOfOldestChild + 1);
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
