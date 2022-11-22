package suncertify.db;

/**
 * Exception indicating that the requested record could not be
 * found. This may be because the record does not exist or
 * has been marked as deleted.
 *
 * @author Robert Mollard
 */
public class RecordNotFoundException extends Exception {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -1608323198389671451L;

    /**
     * The record number of the requested record.
     * Can be null if the record number is unknown.
     */
    private final Integer recordNumber;

    /**
     * Constructs a new <code>RecordNotFoundException</code>
     * with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to <code>initCause</code>.
     */
    public RecordNotFoundException() {
        super();
        this.recordNumber = null;
    }

    /**
     * Constructs a new <code>RecordNotFoundException</code>
     * with the specified detail message.
     * The cause is not initialized, and may
     * subsequently be initialized by
     * a call to <code>initCause</code>.
     *
     * @param message the detail message. The detail message is saved for
     *        later retrieval by the {@link #getMessage()} method.
     */
    public RecordNotFoundException(final String message) {
        super(message);
        this.recordNumber = null;
    }

    /**
     * Constructs a new <code>RecordNotFoundException</code>
     * with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to <code>initCause</code>.
     *
     * @param recordNumber the record number of the
     *        record that could not be found
     */
    public RecordNotFoundException(final int recordNumber) {
        this.recordNumber = recordNumber;
    }

    /**
     * Constructs a new <code>RecordNotFoundException</code>
     * with the specified detail message.
     * The cause is not initialized, and may
     * subsequently be initialized by
     * a call to <code>initCause</code>.
     *
     * @param recordNumber the record number of the
     *        record that could not be found
     * @param message the detail message. The detail message is saved for
     *        later retrieval by the {@link #getMessage()} method.
     */
    public RecordNotFoundException(
            final int recordNumber, final String message) {
        super(message);
        this.recordNumber = recordNumber;
    }

    /**
     * Constructs a new <code>RecordNotFoundException</code>
     * with the specified cause and a
     * detail message of <code>(cause==null ? null : cause.toString())</code>
     * (which typically contains the class and detail message of
     * <code>cause</code>).
     * This constructor is useful for exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param recordNumber the record number of the
     *        record that could not be found
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <code>null</code> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public RecordNotFoundException(
            final int recordNumber, final Throwable cause) {
        super(cause);
        this.recordNumber = recordNumber;
    }

    /**
     * Get the record number of the record that could not be found,
     * or null if the record number is unknown.
     *
     * @return the record number
     */
    public final Integer getRecordNumber() {
        return recordNumber;
    }

}
