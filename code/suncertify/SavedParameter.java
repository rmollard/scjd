package suncertify;

/**
 * The configuration settings that can be saved to file.
 *
 * @author Robert Mollard
 */
public enum SavedParameter {

    /**
     * The database file location.
     */
    SERVER_DATABASE_PATH("databaseFileLocation"),

    /**
     * IP address of the server that the client connects to.
     */
    IP_ADDRESS("address");

    /**
     * The name of the parameter (gets written to file).
     */
    private final String myName;

    /**
     * Create a saved parameter with the given key name.
     *
     * @param keyName the key name to use
     */
    private SavedParameter(final String keyName) {
        myName = keyName;
    }

    /**
     * Get the name of the parameter (the key name that is written to file).
     *
     * @return the parameter's key name
     */
    public String getName() {
        return myName;
    }

}
