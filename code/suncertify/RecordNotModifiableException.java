package suncertify;

/**
 * Checked exception to indicate that a record could not be modified.
 * This may be due to an I/O problem or security problem, for example.
 *
 * @author Robert Mollard
 */
public class RecordNotModifiableException extends Exception {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -7867114400691516799L;

    /**
     * The record number of the requested record.
     */
    private final int recordNumber;

    /**
     * Constructs a new <code>RecordNotModifiableException</code> with
     * <code>null</code> as its detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to <code>initCause</code>.
     *
     * @param recordNumber the record number of the
     *        record that could not be modified
     */
    public RecordNotModifiableException(final int recordNumber) {
        super();
        this.recordNumber = recordNumber;
    }

    /**
     * Constructs a new <code>RecordNotModifiableException</code> with the
     * specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to <code>initCause</code>.
     *
     * @param recordNumber the record number of the
     *        record that could not be modified
     * @param message the detail message.
     *        The detail message is saved for later
     *        retrieval by the {@link #getMessage()} method.
     */
    public RecordNotModifiableException(
            final int recordNumber, final String message) {
        super(message);
        this.recordNumber = recordNumber;
    }

    /**
     * Constructs a new <code>RecordNotModifiableException</code> with the
     * specified cause and a detail message of
     * <code>(cause==null ? null : cause.toString())</code> (which typically
     * contains the class and detail message of <code>cause</code>). This
     * constructor is useful for exceptions that are little more than wrappers
     * for other throwables.
     *
     * @param recordNumber the record number of the
     *        record that could not be modified
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link #getCause()} method). (A <code>null</code> value is
     *        permitted, and indicates that the cause is nonexistent or
     *        unknown.)
     */
    public RecordNotModifiableException(
            final int recordNumber, final Throwable cause) {
        super(cause);
        this.recordNumber = recordNumber;
    }

    /**
     * Get the record number of the record that could not be modified.
     *
     * @return the record number
     */
    public final int getRecordNumber() {
        return recordNumber;
    }

}
