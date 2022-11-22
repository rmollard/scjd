package suncertify.db;

/**
 * Checked exception to indicate that a <code>Record</code> could
 * not be created or modified because it would be identical
 * to another record in an important way.
 *
 * @author Robert Mollard
 */
public class DuplicateKeyException extends Exception {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -6689165809485807888L;

    /**
     * Constructs a new <code>DuplicateKeyException</code>
     * with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to <code>initCause</code>}.
     */
    public DuplicateKeyException() {
        super();
    }

    /**
     * Constructs a new <code>DuplicateKeyException</code>
     * with the specified detail message.
     * The cause is not initialized, and may
     * subsequently be initialized by
     * a call to <code>initCause</code>.
     *
     * @param message the detail message. The detail message is saved for
     *        later retrieval by the {@link #getMessage()} method.
     */
    public DuplicateKeyException(final String message) {
        super(message);
    }

}
