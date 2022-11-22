package suncertify.db;

/**
 * Runtime exception indicating that there was a problem
 * when trying to write data to a file.
 *
 * @author Robert Mollard
 */
public class FileWriteException extends RuntimeException {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 5955345679061430987L;

    /**
     * Constructs a new <code>FileWriteException</code>
     * with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to <code>initCause</code>.
     */
    public FileWriteException() {
        super();
    }

    /**
     * Constructs a new <code>FileWriteException</code>
     * with the specified detail message.
     * The cause is not initialized, and may
     * subsequently be initialized by
     * a call to <code>initCause</code>.
     *
     * @param message the detail message. The detail message is saved for
     *        later retrieval by the {@link #getMessage()} method.
     */
    public FileWriteException(final String message) {
        super(message);
    }

    /**
     * Constructs a new <code>FileWriteException</code>
     * with the specified cause and a
     * detail message of <code>(cause==null ? null : cause.toString())</code>
     * (which typically contains the class and detail message of
     * <code>cause</code>).
     * This constructor is useful for file write exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link #getCause()} method). (A <code>null</code> value is
     *        permitted, and indicates that the cause is nonexistent or
     *        unknown.)
     */
    public FileWriteException(final Throwable cause) {
        super(cause);
    }

}
