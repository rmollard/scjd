package suncertify;

import suncertify.utils.Localization;

/**
 * The possible problems we could have when parsing the command line arguments
 * when starting up the URLyBird program.
 *
 * @author Robert Mollard
 */
final class StartupProblem {

    /**
     * Localized error message.
     */
    private final String errorMessage;

    /**
     * Construct a new startup problem with a localized error message.
     *
     * @param message the unlocalized error message
     */
    private StartupProblem(final String message) {
        this.errorMessage = Localization.getString(message);
    }

    /**
     * Construct a new startup problem with a localized error message with the
     * given arguments.
     *
     * @param message the unlocalized error message
     * @param args additional arguments to be substituted into the error message
     *        (already localized)
     */
    private StartupProblem(final String message, final String... args) {
        this.errorMessage = Localization.getString(message, args);
    }

    /**
     * Create a "too many arguments" warning.
     *
     * @return a warning with a localized "too many arguments" message
     */
    public static StartupProblem createTooManyArgumentsError() {
        return new StartupProblem("tooManyArguments");
    }

    /**
     * Create an "unknown argument" warning.
     *
     * @param unknownArgument the unknown argument given
     * @return a warning with a localized "unknown argument" message
     */
    public static StartupProblem createUnknownArgumentError(
            final String unknownArgument) {
        return new StartupProblem("unknownArgument", unknownArgument);
    }

    /**
     * Get the translated error message.
     *
     * @return the localized error message
     */
    public String getMessage() {
        return errorMessage;
    }
}
