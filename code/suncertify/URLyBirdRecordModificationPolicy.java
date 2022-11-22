package suncertify;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import suncertify.db.TableSchema;
import suncertify.fields.DateField;
import suncertify.fields.Field;
import suncertify.fields.FieldDetails;

/**
 * Determines whether a given record is modifiable according to the URLyBird
 * business rules.
 *
 * A record is modifiable if it has at least one modifiable field and the date
 * field (if any) falls within the range specified in the properties file.
 *
 * @author Robert Mollard
 */
public final class URLyBirdRecordModificationPolicy implements
        RecordModificationPolicy {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -2963450549947087974L;

    /**
     * The name of the properties file containing the
     * record modification policy details.
     */
    private static final String FILE_NAME =
        "URLyBirdRecordModificationPolicy.properties";

    /**
     * The name of the property specifying the lower bound of the required time
     * range.
     */
    private static final String BEFORE_PROPERTY = "before";

    /**
     * The name of the property specifying the upper bound of the required time
     * range.
     */
    private static final String AFTER_PROPERTY = "after";

    /**
     * The name of the property specifying the name of the field that must be
     * withing the required time range.
     */
    private static final String DATE_PROPERTY = "dateFieldName";

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify");

    /**
     * The default lower time limit for booking a hotel room, measured from the
     * current server date, in milliseconds.
     */
    private static final long DEFAULT_LOWER_TIME_LIMIT = 0;

    /**
     * The default upper time limit for booking a hotel room, measured from the
     * current server date (48 hours in milliseconds).
     */
    private static final long DEFAULT_UPPER_TIME_LIMIT = 48 * 60 * 60 * 1000;

    /**
     * Schema of the database table.
     */
    private final TableSchema schema;

    /**
     * Lower time limit.
     */
    private long lowerBound;

    /**
     * Upper time limit.
     */
    private long upperBound;

    /**
     * The name of the field to be checked to ensure it is within the lower and
     * upper time limits.
     */
    private String dateFieldName;

    /**
     * A set of Properties loaded from the file.
     */
    private static final Properties PROPS = new Properties();

    static {
        DataInputStream in = null;
        //Load properties file
        try {
            in = new DataInputStream(
                URLyBirdRecordModificationPolicy.class
                        .getResourceAsStream(FILE_NAME));

            PROPS.load(in);

        } catch (FileNotFoundException e) {
            LOGGER.severe("Could not open policy file: " + FILE_NAME);
        } catch (IOException e) {
            LOGGER.severe("IOException when reading policy file: " + e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.severe("IOException when closing policy file: " + e);
                }
            }
        }
    }

    /**
     * Construct a new instance with the given schema.
     *
     * @param schema the database table schema to use.
     */
    public URLyBirdRecordModificationPolicy(final TableSchema schema) {
        this.schema = schema;
        dateFieldName = PROPS.getProperty(DATE_PROPERTY);
        lowerBound = getLong(AFTER_PROPERTY, DEFAULT_LOWER_TIME_LIMIT);
        upperBound = getLong(BEFORE_PROPERTY, DEFAULT_UPPER_TIME_LIMIT);
    }

    /** {@inheritDoc} */
    public boolean isRecordModifiable(
            final List<Field> fields, final Date currentServerDate) {
        boolean hasModifiableField = false;

        //Look for a modifiable field
        final List<FieldDetails> theList = schema.getFields();
        for (int i = 0; i < theList.size() && !hasModifiableField; i++) {
            FieldDetails details = theList.get(i);
            if (details.isModifiable()) {
                hasModifiableField = true;
            }
        }

        boolean isWithinTime = true;

        if (dateFieldName != null) {
            final DateField dateField = (DateField)
                fields.get(schema.getFieldIndexByName(dateFieldName));
            isWithinTime = isRecordWithinSpecifiedTime(currentServerDate,
                    lowerBound, upperBound, dateField);
        }

        return hasModifiableField && isWithinTime;
    }

    /**
     * Determine if a record's booking date occurs in the future and before a
     * given future time.
     *
     * @param currentServerDate the current date on the server
     * @param lowerRange the lower limit of how many milliseconds
     *        in the future the booking time can be (may be negative
     *        to indicate a time in the past).
     * @param upperRange the upper limit of how many milliseconds
     *        in the future the booking time can be (may be negative
     *        to indicate a time in the past).
     * @param dateField the <code>DateField</code> in the record to test
     * @return true if the record's booking date is within
     *         <code>millisInFuture</code> milliseconds
     *         of <code>currentServerDate</code>
     */
    private boolean isRecordWithinSpecifiedTime(final Date currentServerDate,
            final long lowerRange, final long upperRange,
            final DateField dateField) {
        boolean result = true;

        final Date bookingDate = dateField.getValue();

        //Check date is not before the lower limit
        final Date currentServerDatePlusLowerLimit =
            new Date(currentServerDate.getTime() + lowerRange);

        if (bookingDate.before(currentServerDatePlusLowerLimit)) {
            result = false;
        }

        //Check date is not after the upper limit
        final Date currentServerDatePlusUpperLimit =
            new Date(currentServerDate.getTime() + upperRange);

        if (bookingDate.after(currentServerDatePlusUpperLimit)) {
            result = false;
        }
        return result;
    }

    /**
     * Get the numeric value of the property with the given key.
     *
     * @param key the property name to look up
     * @param defaultValue the default value to return
     *        if <code>key</code> is not found or cannot be parsed
     * @return the value of the property with the given key name, or
     *         <code>defaultValue</code> if the property could not be parsed
     */
    private long getLong(final String key, final long defaultValue) {
        long result = defaultValue;
        final String numberString = PROPS.getProperty(key);

        if (numberString != null) {
            try {
                result = Long.parseLong(numberString);
            } catch (NumberFormatException e) {
                LOGGER.warning("Could not parse string as integer: "
                        + numberString);
            }
        }
        return result;
    }

}
