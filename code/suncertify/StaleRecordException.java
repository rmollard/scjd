package suncertify;

/**
 * Checked exception to indicate that a client's version of a record has become
 * out of date, perhaps by being changed by another client.
 *
 * @author Robert Mollard
 */
public class StaleRecordException extends Exception {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 5562312830342848504L;

    /**
     * The record number of the requested record.
     */
    private final int recordNumber;

    /**
     * Constructs a new <code>StaleRecordException</code> with
     * <code>null</code> as its detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to <code>initCause</code>.
     *
     * @param recordNumber the record number
     *        of the record that could not be modified
     */
    public StaleRecordException(final int recordNumber) {
        super();
        this.recordNumber = recordNumber;
    }

    /**
     * Constructs a new <code>StaleRecordException</code> with the specified
     * detail message. The cause is not initialized, and may subsequently be
     * initialized by a call to <code>initCause</code>.
     *
     * @param recordNumber the record number of the record that is out of date
     * @param message the detail message.
     *        The detail message is saved for later retrieval by
     *        the {@link #getMessage()} method.
     */
    public StaleRecordException(final int recordNumber, final String message) {
        super(message);
        this.recordNumber = recordNumber;
    }

    /**
     * Constructs a new <code>StaleRecordException</code> with the specified
     * cause and a detail message of
     * <code>(cause==null ? null : cause.toString())</code> (which typically
     * contains the class and detail message of <code>cause</code>). This
     * constructor is useful for exceptions that are little more than wrappers
     * for other throwables.
     *
     * @param recordNumber the record number of the record that is out of date
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link #getCause()} method). (A <code>null</code> value is
     *        permitted, and indicates that the cause is nonexistent or
     *        unknown.)
     */
    public StaleRecordException(
            final int recordNumber, final Throwable cause) {
        super(cause);
        this.recordNumber = recordNumber;
    }

    /**
     * Get the record number of the record that was out of date.
     *
     * @return the record number
     */
    public final int getRecordNumber() {
        return recordNumber;
    }

}
