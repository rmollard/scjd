package suncertify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Simple class to allow configuration
 * options to be saved to a file, and loaded from a file.
 *
 * Thread safety: this class is thread safe.
 *
 * @author Robert Mollard
 */
public final class PersistentConfiguration {

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify.client");

    /**
     * The name of our properties file.
     */
    private static final String OPTIONS_FILENAME = "suncertify.properties";

    /**
     * The file containing our saved configuration.
     */
    private static File savedOptionsFile = new File(OPTIONS_FILENAME);

    /**
     * The singleton instance of this class.
     */
    private static final PersistentConfiguration INSTANCE =
        new PersistentConfiguration();

    /**
     * The configuration properties for this application.
     */
    private final Properties parameters;

    /**
     * Creates the singleton instance and loads the parameters from the
     * properties file.
     */
    private PersistentConfiguration() {
        parameters = loadParameters();
    }

    /**
     * Get the singleton instance of this class.
     *
     * @return the instance
     */
    public static PersistentConfiguration getInstance() {
        return INSTANCE;
    }

    /**
     * Get the value of the given parameter.
     *
     * @param parameterName the name of the parameter
     *        for which the user is requesting the value
     * @return the value of the named parameter, or null if not found
     */
    public String getParameter(final SavedParameter parameterName) {
        return parameters.getProperty(parameterName.getName());
    }

    /**
     * Set the parameter with the given name to the given value.
     * The new value will be stored in the configuration file.
     *
     * @param parameterName the name of the parameter
     * @param parameterValue the value to be stored for the parameter
     */
    public void setParameter(
            final SavedParameter parameterName, final String parameterValue) {
        parameters.setProperty(parameterName.getName(), parameterValue);
        saveAllParameters();
    }

    /**
     * Loads the configuration file.
     *
     * @return properties loaded from file.
     *         Returns an empty list of properties
     *         if the file cannot be parsed properly.
     */
    private Properties loadParameters() {
        Properties loadedProperties = new Properties();
        FileInputStream input = null;

        synchronized (savedOptionsFile) {
            try {
                if (savedOptionsFile.exists() && savedOptionsFile.canRead()) {
                    input = new FileInputStream(savedOptionsFile);
                    loadedProperties.load(input);
                }
            } catch (FileNotFoundException e) {
                LOGGER.warning("Could not find configuration file "
                        + OPTIONS_FILENAME + ", " + e);
            } catch (IOException e) {
                LOGGER.warning("Could not read configuration file: " + e);
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException e) {
                    LOGGER.warning("Problem when closing input file: " + e);
                }
            }
        }
        return loadedProperties;
    }

    /**
     * Write all parameters to the configuration file.
     * We delete the file and write it again.
     */
    private void saveAllParameters() {
        FileOutputStream output = null;

        synchronized (savedOptionsFile) {
            try {
                if (savedOptionsFile.exists()) {
                    savedOptionsFile.delete();
                }
                if (savedOptionsFile.createNewFile()) {
                    output = new FileOutputStream(savedOptionsFile);
                    parameters.store(output,
                            "Automatically generated configuration file");
                } else {
                    LOGGER.warning("Could not create configuration file");
                }
            } catch (IOException e) {
                LOGGER.warning("Could not save configuration file: " + e);
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException e) {
                    LOGGER.warning("Problem when closing output file: " + e);
                }
            }
        }
    }

}
