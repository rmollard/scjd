package suncertify.db;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import suncertify.fields.BooleanField;
import suncertify.fields.CurrencyField;
import suncertify.fields.DateField;
import suncertify.fields.Field;
import suncertify.fields.FieldDetails;
import suncertify.fields.IntegerField;
import suncertify.fields.StringField;

/**
 * Table-specific information about the format of the fields.
 * Each Field has a parser, which can convert a String into a Field
 * and vice versa.
 *
 * Meta-data for each field is given in a .properties file.
 * Property keys consist of the field name appended with
 * <code>.type</code>, <code>.modifiable</code>, <code>.searchable</code>,
 * or <code>.displayable</code>, and a value
 * that is not <code>true</code> or <code>yes</code>
 * will be interpreted as false.
 *
 * Type may be one of the following:
 * <ul>
 * <li>string</li>
 * <li>integer</li>
 * <li>date</li>
 * <li>boolean</li>
 * <li>currency</li>
 * </ul>
 *
 * Example:
 * <pre>
 * owner.type=integer
 * owner.modifiable=true
 * owner.searchable=false
 * owner.displayable=true
 * </pre>
 *
 * By default, a field is of type string, and is
 * displayable but not searchable or modifiable.
 *
 * @author Robert Mollard
 */
public final class URLyBirdSchema implements TableSchema {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 3290671833464094239L;

    /**
     * The name of the properties file containing the
     * schema information.
     */
    private static final String FILE_NAME =
        "URLyBirdSchema.properties";

    /**
     * Key name of "type" parameter in the properties file.
     */
    private static final String TYPE = "type";

    /**
     * Value name of the "string" field type.
     */
    private static final String STRING_CLASS = "string";

    /**
     * Value name of the "integer" field type.
     */
    private static final String INTEGER_CLASS = "integer";

    /**
     * Value name of the "date" field type.
     */
    private static final String DATE_CLASS = "date";

    /**
     * Value name of the "boolean" field type.
     */
    private static final String BOOLEAN_CLASS = "boolean";

    /**
     * Value name of the "currency" field type.
     */
    private static final String CURRENCY_CLASS = "currency";

    /**
     * Key name of "searchable" parameter in the properties file.
     */
    private static final String SEARCHABLE = "searchable";

    /**
     * Key name of "modifiable" parameter in the properties file.
     */
    private static final String MODIFIABLE = "modifiable";

    /**
     * Key name of "displayable" parameter in the properties file.
     */
    private static final String DISPLAYABLE = "displayable";

    /**
     * Value of "true" for boolean keys.
     */
    private static final String TRUE = "true";

    /**
     * Alternative value for "true" for boolean keys.
     */
    private static final String YES = "yes";

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify.db");

    /**
     * Convenience map for looking up classes by string.
     */
    private final Map<String, Class<? extends Field>> fieldClasses =
        new HashMap<String, Class<? extends Field>>();

    /**
     * Convenience map for looking up field indexes by string.
     */
    private final Map<String, Integer> fieldIndexes =
        new HashMap<String, Integer>();

    /**
     * The list of field details for this schema.
     */
    private final List<FieldDetails> schema =
        new ArrayList<FieldDetails>();

    /**
     * The name of each field class.
     */
    private static final Map<String, Class<? extends Field>> CLASS_NAMES =
        new HashMap<String, Class<? extends Field>>();

    /**
     * A set of Properties loaded from the file.
     */
    private static final Properties PROPS = new Properties();

    static {
        DataInputStream in = null;
        //Load properties from file
        try {
            in = new DataInputStream(
                URLyBirdSchema.class.getResourceAsStream(FILE_NAME));

            PROPS.load(in);
        } catch (FileNotFoundException e) {
            LOGGER.severe("Could not open schema file: " + FILE_NAME);
        } catch (IOException e) {
            LOGGER.severe("IOException when reading schema: " + e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.severe("IOException when closing parser file: " + e);
                }
            }
        }

        //Associate the field class names with their corresponding classes
        CLASS_NAMES.put(STRING_CLASS, StringField.class);
        CLASS_NAMES.put(INTEGER_CLASS, IntegerField.class);
        CLASS_NAMES.put(DATE_CLASS, DateField.class);
        CLASS_NAMES.put(BOOLEAN_CLASS, BooleanField.class);
        CLASS_NAMES.put(CURRENCY_CLASS, CurrencyField.class);
    }

    /**
     * Create a new instance. Each field will have its maximum
     * length set to the corresponding value in <code>fieldLengths</code>
     *
     * @param fieldNames the name of each field.
     * @param fieldLengths the maximum length (in characters) of each field.
     *      Any element may be null, in which case the length of the
     *      corresponding field will be unbounded.
     */
    public URLyBirdSchema(final List<String> fieldNames,
            final List<Integer> fieldLengths) {
        addEntries(fieldNames, fieldLengths);
    }

    /** {@inheritDoc} */
    public int getFieldIndexByName(final String fieldName) {
        Integer result = fieldIndexes.get(fieldName);
        if (result == null) {
            throw new IllegalArgumentException("Field name not found: "
                    + fieldName);
        }
        return result;
    }

    /** {@inheritDoc} */
    public Class<? extends Field> getFieldClassByName(final String fieldName) {
        Class<? extends Field> result = fieldClasses.get(fieldName);
        if (result == null) {
            throw new IllegalArgumentException("Field name not found: "
                    + fieldName);
        }
        return result;
    }

    /** {@inheritDoc} */
    public Class<? extends Field> getFieldClassByIndex(final int fieldIndex) {
        Class<? extends Field> result;
        try {
            result = schema.get(fieldIndex).getFieldClass();
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Field index not found: "
                    + fieldIndex);
        }
        return result;
    }

    /** {@inheritDoc} */
    public List<FieldDetails> getFields() {
        return Collections.unmodifiableList(schema);
    }

    /**
     * Add entries to the schema.
     * The "searchable", "displayable" and "modifiable" status
     * of each field will be set based on the contents
     * of the schema properties file.
     *
     * @param fieldNames list of field names. Must have the same
     *        number of entries as <code>fieldLengths</code>.
     * @param fieldLengths list of maximum lengths of fields. The
     *        fields are assumed to be in the same order as the
     *        fields in <code>fieldNames</code>.
     *                Must have the same
     *        number of entries as <code>fieldNames</code>.
     */
    private void addEntries(
            final List<String> fieldNames, final List<Integer> fieldLengths) {
        assert fieldNames.size() == fieldLengths.size();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);

            //Try to get the type of the field, defaulting to String
            //if not specified or invalid.
            Class<? extends Field> fieldClass =
                getFieldClass(fieldName, StringField.class);

            boolean searchable =
                getBoolean(fieldName + "." + SEARCHABLE, false);
            boolean displayable =
                getBoolean(fieldName + "." + DISPLAYABLE, true);
            boolean modifiable =
                getBoolean(fieldName + "." + MODIFIABLE, false);

            Integer length = fieldLengths.get(i);

            addEntry(fieldName, fieldClass, searchable, displayable,
                    modifiable, length);
        }
    }

    /**
     * Add an entry to the schema.
     *
     * @param name the name of the field
     * @param theClass the class of the field
     * @param searchable whether the field should be included in the
     *        database search criteria
     * @param displayable whether the field should be displayed in the
     *        search results
     * @param modifiable whether the field should be modifiable by
     *        end users
     * @param maxLength the maximum length of the field, in characters.
     *        Can be null, in which case length is unbounded.
     */
    private void addEntry(final String name,
            final Class<? extends Field> theClass,
            final boolean searchable, final boolean displayable,
            final boolean modifiable, final Integer maxLength) {

        fieldIndexes.put(name, fieldClasses.size());
        fieldClasses.put(name, theClass);
        schema.add(new FieldDetails(name, theClass, searchable, displayable,
                modifiable, maxLength));
    }

    /**
     * Get the value of a boolean property from the properties file.
     *
     * @param propertyName the property name key to look up
     * @param defaultValue the value to return if
     *        <code>propertyName</code> cannot be found or parsed
     * @return the value of the boolean property that has the
     *         key name called <code>propertyName</code>, or
     *         <code>defaultValue</code> if
     *         <code>propertyName</code> cannot be found or parsed
     */
    private boolean getBoolean(
            final String propertyName, final boolean defaultValue) {
        boolean result = defaultValue;
        String boolString = PROPS.getProperty(propertyName);

        if (boolString != null) {
            if (TRUE.equals(boolString) || YES.equals(boolString)) {
                result = true;
            } else {
                result = false;
            }
        }
        return result;
    }

    /**
     * Get a string describing the schema.
     * The string contains the details for each field in the schema.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("URLyBird schema");
        buffer.append("{\n");
        for (FieldDetails details : schema) {
            buffer.append(details.toString()).append('\n');
        }
        buffer.append('}');

        return buffer.toString();
    }

    /**
     * Get the type (class) of a particular
     * field from the properties file.
     *
     * @param fieldName the name of the field to look up
     * @param defaultValue the value to return if
     *        <code>fieldName</code> cannot be found or parsed
     * @return the type (class) of the field
     *         called <code>fieldName</code>, or
     *         <code>defaultValue</code> if
     *         <code>fieldName</code> cannot be found or parsed
     */
    private Class<? extends Field> getFieldClass(
        final String fieldName, final Class<? extends Field> defaultValue) {

        Class<? extends Field> result = null;
        String className = PROPS.getProperty(fieldName + "." + TYPE);

        if (className != null) {
            //Try to find it in our map
            result = CLASS_NAMES.get(className);
            if (result == null) {
                LOGGER.warning("Unknown field class: " + className);
            }
        }

        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

}
