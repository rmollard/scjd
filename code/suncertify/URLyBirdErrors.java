package suncertify;

import suncertify.utils.Localization;

/**
 * The known errors that can occur in the URLyBird program.
 *
 * @author Robert Mollard
 */
public enum URLyBirdErrors {

    /**
     * Some sort of network-related error occurred.
     */
    NETWORK_ERROR("error.networkError"),

    /**
     * The server was expected to be running, but was found to be not running.
     */
    SERVER_NOT_RUNNING("error.serverNotRunning"),

    /**
     * The file was missing, or not writable.
     */
    FILE_NOT_FOUND("error.fileNotFound"),

    /**
     * The contents of the file were not in the expected format.
     */
    FILE_CORRUPT("error.fileCorrupt"),

    /**
     * The user's view of the record has become out of date, probably because
     * another user modified the record.
     */
    STALE_RECORD("error.staleRecord"),

    /**
     * The server does not allow the record to be modified.
     */
    RECORD_NOT_MODIFIABLE("error.recordNotModifiable"),

    /**
     * The record was not found on the server.
     */
    RECORD_NOT_FOUND("error.recordNotFound"),

    /**
     * An unexpected exception occurred.
     */
    UNEXPECTED_EXCEPTION("error.unexpectedException");

    /**
     * How many errors have been created so far.
     */
    private static int errorCount;

    /**
     * The unique error number for this error.
     */
    private final int errorNumber;

    /**
     * Localized error message.
     */
    private final String message;

    /**
     * Create a new error.
     *
     * @param unlocalizedMessage the message text.
     *        This gets localized into an error message description.
     */
    private URLyBirdErrors(final String unlocalizedMessage) {
        errorNumber = ++errorCount;
        message = Localization.getString(unlocalizedMessage);
    }

    /**
     * Get the error number for this error.
     *
     * @return the error number
     */
    public int getErrorNumber() {
        return errorNumber;
    }

    /**
     * Get the description of this error, localized for the locale.
     *
     * @return the localized error description
     */
    public String getLocalizedErrorDescription() {
        return message;
    }

}
