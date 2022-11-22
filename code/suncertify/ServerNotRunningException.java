package suncertify;

/**
 * Checked exception to indicate that the server was expected to be running, but
 * was found to be not running.
 *
 * @author Robert Mollard
 */
public class ServerNotRunningException extends Exception {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 7263784728722527993L;

    /**
     * Constructs a new <code>ServerNotRunningException</code> with the
     * specified cause and a detail message of
     * <code>(cause==null ? null : cause.toString())</code> (which typically
     * contains the class and detail message of <code>cause</code>). This
     * constructor is useful for exceptions that are little more than wrappers
     * for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link #getCause()} method). (A <code>null</code> value is
     *        permitted, and indicates that the cause is nonexistent or
     *        unknown.)
     */
    public ServerNotRunningException(final Throwable cause) {
        super(cause);
    }

}
