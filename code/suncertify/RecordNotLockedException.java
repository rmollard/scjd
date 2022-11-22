package suncertify;

/**
 * Checked exception to indicate that a client has attempted to alter a record
 * that they do not own the lock for.
 *
 * @author Robert Mollard
 */
public class RecordNotLockedException extends Exception {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 7263784728722527993L;

    /**
     * The record number of the requested record.
     */
    private final int recordNumber;

    /**
     * Constructs a new <code>RecordNotLockedException</code> with
     * <code>null</code> as its detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to <code>initCause</code>.
     *
     * @param recordNumber the record number of the record that was not locked
     */
    public RecordNotLockedException(final int recordNumber) {
        super();
        this.recordNumber = recordNumber;
    }

    /**
     * Constructs a new <code>RecordNotLockedException</code> with the
     * specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to <code>initCause</code>.
     *
     * @param recordNumber the record number of the record that was not locked
     * @param message the detail message.
     *        The detail message is saved for later
     *        retrieval by the {@link #getMessage()} method.
     */
    public RecordNotLockedException(final int recordNumber,
            final String message) {
        super(message);
        this.recordNumber = recordNumber;
    }

    /**
     * Constructs a new <code>RecordNotLockedException</code> with the
     * specified cause and a detail message of
     * <code>(cause==null ? null : cause.toString())</code> (which typically
     * contains the class and detail message of <code>cause</code>). This
     * constructor is useful for exceptions that are little more than wrappers
     * for other throwables.
     *
     * @param recordNumber the record number of the record that was not locked
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link #getCause()} method). (A <code>null</code> value is
     *        permitted, and indicates that the cause is nonexistent or
     *        unknown.)
     */
    public RecordNotLockedException(final int recordNumber,
            final Throwable cause) {
        super(cause);
        this.recordNumber = recordNumber;
    }

    /**
     * Get the record number of the record that was not locked.
     *
     * @return the record number
     */
    public final int getRecordNumber() {
        return recordNumber;
    }

}
