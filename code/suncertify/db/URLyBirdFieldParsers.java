package suncertify.db;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import suncertify.fields.BooleanField;
import suncertify.fields.CurrencyField;
import suncertify.fields.DateField;
import suncertify.fields.Field;
import suncertify.fields.IntegerField;
import suncertify.fields.StringField;
import suncertify.fields.parsers.BooleanParser;
import suncertify.fields.parsers.DateParser;
import suncertify.fields.parsers.FieldParser;
import suncertify.fields.parsers.PlainIntegerParser;
import suncertify.fields.parsers.PlainStringParser;
import suncertify.fields.parsers.PrefixedCurrencyParser;

/**
 * A collection of parsers for fields in the
 * URLyBird database file format.
 * The properties file can contain the date format for
 * parsing date values, and the "true" and "false" values
 * for parsing boolean values.
 *
 * @author Robert Mollard
 */
public final class URLyBirdFieldParsers implements FieldParsers {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -5898827743742615462L;

    /**
     * The name of the properties file containing the field
     * parsing information.
     */
    private static final String FILE_NAME =
        "URLyBirdParsers.properties";

    /**
     * Name of the key in the properties file for "true".
     */
    private static final String DATE_KEY = "dateFormat";

    /**
     * Name of the key in the properties file for "true".
     */
    private static final String TRUE_KEY = "booleanTrue";

    /**
     * Name of the key in the properties file for "false".
     */
    private static final String FALSE_KEY = "booleanFalse";

    /**
     * Name of the key in the properties file for the number
     * of cents per dollar.
     */
    private static final String CENTS_KEY = "centsPerDollar";

    /**
     * Name of the key in the properties file for the number
     * of decimal places to display for the currency.
     */
    private static final String DECIMAL_PLACES_KEY = "decimalPlaces";

    /**
     * The date format (as understood by java.text.SimpleDateFormat).
     */
    private static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd";

    /**
     * Default value of "true" for booleans.
     */
    private static final String DEFAULT_TRUE = "Y";

    /**
     * Default value of "false" for booleans.
     */
    private static final String DEFAULT_FALSE = "N";

    /**
     * Default number of cents per dollar of currency.
     */
    private static final int DEFAULT_CENTS = 100;

    /**
     * Default number of decimal places to display.
     */
    private static final int DEFAULT_DECIMAL_PLACES = 2;

    /**
     * A map enabling us to easily get the
     * parser to use for a given field class.
     */
    private final Map<Class<? extends Field>, FieldParser>
        parserForField = new HashMap<Class<? extends Field>, FieldParser>();

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify.db");

    /**
     * A set of Properties loaded from the file.
     */
    private static final Properties PROPS = new Properties();

    static {
        DataInputStream in = null;
        //Load parser information from file
        try {
            in = new DataInputStream(
                URLyBirdFieldParsers.class.getResourceAsStream(FILE_NAME));

            PROPS.load(in);

        } catch (FileNotFoundException e) {
            LOGGER.severe("Could not open parser file: " + FILE_NAME);
        } catch (IOException e) {
            LOGGER.severe("IOException when configuring parsers: " + e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.severe("IOException when closing parser file: " + e);
                }
            }
        }
    }

    /**
     * The only instance of this class.
     */
    private static final URLyBirdFieldParsers INSTANCE =
            new URLyBirdFieldParsers();

    /**
     * Get the singleton instance of this class.
     *
     * @return the instance
     */
    public static FieldParsers getInstance() {
        return INSTANCE;
    }

    /**
     * Private constructor to enforce singleton rule.
     */
    private URLyBirdFieldParsers() {
        parserForField.put(IntegerField.class, new PlainIntegerParser());
        parserForField.put(StringField.class, new PlainStringParser());
        parserForField.put(BooleanField.class, new BooleanParser(getString(
                TRUE_KEY, DEFAULT_TRUE), getString(FALSE_KEY, DEFAULT_FALSE)));
        parserForField.put(CurrencyField.class, new PrefixedCurrencyParser(
                getInt(CENTS_KEY, DEFAULT_CENTS), getInt(DECIMAL_PLACES_KEY,
                        DEFAULT_DECIMAL_PLACES)));
        parserForField.put(DateField.class, new DateParser(getString(DATE_KEY,
                DEFAULT_DATE_FORMAT)));
    }

    /** {@inheritDoc} */
    public FieldParser getParserForClass(
            final Class<? extends Field> fieldClass) {
        return parserForField.get(fieldClass);
    }

    /**
     * Get the string with the given key from the properties file.
     * If the key is not found, <code>defaultValue</code> will be returned.
     *
     * @param key the key to search for
     * @param defaultValue value to use if key is not found
     * @return the string with the given key, or <code>defaultValue</code>
     *         if not found.
     */
    private String getString(final String key, final String defaultValue) {
        String result = PROPS.getProperty(key);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Get the numeric value of the property with the given key.
     *
     * @param key the property name to look up
     * @param defaultValue the default value to return if
     *        <code>key</code> is not found or cannot be parsed
     * @return the value of the property with the given key name,
     *         or <code>defaultValue</code> if
     *         the property could not be parsed
     */
    private int getInt(final String key, final int defaultValue) {
        int result = defaultValue;
        String numberString = PROPS.getProperty(key);

        if (numberString != null) {
            try {
                result = Integer.parseInt(numberString);
            } catch (NumberFormatException e) {
                LOGGER.warning("Could not parse string as integer: "
                        + numberString);
            }
        }
        return result;
    }

    /**
     * Get a string describing the parsers.
     * The string contains the all of the [class, parser] pairs.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("URLyBird field parsers {\n");

        Set<Class<? extends Field>> fieldClasses = parserForField.keySet();

        //Print out the [class, parser] pairs
        for (Class<? extends Field> currentClass : fieldClasses) {
            buffer.append(currentClass).append(" is parsed by: \n").append(
                    parserForField.get(currentClass)).append('\n');
        }
        buffer.append('}');

        return buffer.toString();
    }

}
