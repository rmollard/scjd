package suncertify.db;

/**
 * A stack that can handle multiple threads simultaneously.
 *
 * @author Robert Mollard
 *
 * @param <E> the type of object to hold in the stack
 */
public interface ConcurrentStack<E> {

    /**
     * Put an item into the stack. This method must be thread safe.
     *
     * @param item the item to add
     */
    void push(E item);

    /**
     * Get the item that was last added to the stack.
     * Note that this method removes the item from the stack.
     * This method must be thread safe.
     *
     * @return the item that was added last.
     *         If the stack is empty, returns null.
     */
    E pop();

    /**
     * Get the item that was last added to the stack.
     * This does not remove the item.
     * This method must be thread safe.
     *
     * @return the item that was added last. If the stack is
     *         empty, returns null.
     */
    E peek();

}
