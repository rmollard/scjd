package suncertify.db;

/**
 * Runtime exception to indicate a lock security exception,
 * for example when a client tries to modify a
 * record that they haven't locked.
 *
 * @author Robert Mollard
 */
public class LockException extends RuntimeException {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -1464666010396154111L;

    /**
     * The record number of the requested record.
     */
    private final int recordNumber;

    /**
     * Constructs a new <code>LockException</code>
     * with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to <code>initCause</code>.
     *
     * @param recordNumber the record number of the
     *        record that could not be modified
     */
    public LockException(final int recordNumber) {
        super();
        this.recordNumber = recordNumber;
    }

    /**
     * Constructs a new <code>LockException</code>
     * with the specified detail message.
     * The cause is not initialized, and may
     * subsequently be initialized by
     * a call to <code>initCause</code>.
     *
     * @param recordNumber the record number of the
     *        record that could not be modified
     * @param message the detail message. The detail message is saved for
     *        later retrieval by the {@link #getMessage()} method.
     */
    public LockException(final int recordNumber, final String message) {
        super(message);
        this.recordNumber = recordNumber;
    }

    /**
     * Constructs a new <code>LockException</code>
     * with the specified cause and a
     * detail message of <code>(cause==null ? null : cause.toString())</code>
     * (which typically contains the class and detail message of
     * <code>cause</code>).
     * This constructor is useful for exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param recordNumber the record number of the
     *        record that could not be modified
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <code>null</code> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public LockException(final int recordNumber, final Throwable cause) {
        super(cause);
        this.recordNumber = recordNumber;
    }

    /**
     * Get the record number of the record that had the
     * lock security problem.
     *
     * @return the record number
     */
    public final int getRecordNumber() {
        return recordNumber;
    }

}
