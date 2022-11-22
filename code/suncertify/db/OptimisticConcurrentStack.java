package suncertify.db;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A concurrent stack using Treiber's algorithm.
 * This is very efficient for low concurrency levels, but
 * becomes slow when thrashed.
 *
 * @author Robert Mollard
 * @param <E> the type of element contained
 */
public final class OptimisticConcurrentStack<E> implements ConcurrentStack<E> {

    /*
     * The push and pop methods work
     * by speculatively doing some work, and then
     * seeing if the stack has been changed
     * by another thread.
     * If it has been changed (i.e. the compare-and-set fails),
     * the process tries again until the compare-and-set succeeds.
     */

    /**
     * Generic holder class to form elements in the stack.
     *
     * @param <T> the type of object to contain
     */
    private static final class Node<T> {

        /**
         * The item contained by this node.
         */
        final T item;

        /**
         * The next node down in the stack.
         */
        Node<T> next;

        /**
         * Create a node containing the given item.
         *
         * @param item the item to be contained
         */
        private Node(final T item) {
            this.item = item;
        }
    }

    /**
     * The top element in the stack.
     */
    private AtomicReference<Node<E>> top = new AtomicReference<Node<E>>();

    /**
     * Create a new <code>OptimisticConcurrentStack</code>
     * containing no elements.
     */
    public OptimisticConcurrentStack() {
        //Empty
    }

    /** {@inheritDoc} */
    public void push(final E item) {
        Node<E> newTop = new Node<E>(item);
        Node<E> oldTop;

        oldTop = top.get();
        newTop.next = oldTop;

        while (!top.compareAndSet(oldTop, newTop)) {
            oldTop = top.get();
            newTop.next = oldTop;
        }
    }

    /** {@inheritDoc} */
    public E pop() {
        E result = null;
        boolean ok = true;

        Node<E> oldTop;
        Node<E> newTop = null;

        oldTop = top.get();
        if (oldTop == null) {
            ok = false;
        } else {
            newTop = oldTop.next;
        }

        while (ok && !top.compareAndSet(oldTop, newTop)) {
            oldTop = top.get();
            if (oldTop == null) {
                ok = false;
            } else {
                newTop = oldTop.next;
            }
        }

        if (ok) {
            result = oldTop.item;
        }
        return result;
    }

    /** {@inheritDoc} */
    public E peek() {
        final E result;
        Node<E> oldHead = top.get();

        if (oldHead == null) {
            result = null;
        } else {
            result = oldHead.item;
        }
        return result;
    }

}
