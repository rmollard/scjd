package suncertify.fields.parsers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import suncertify.fields.DateField;
import suncertify.fields.Field;

/**
 * A parser for Date values.
 *
 * This class is immutable.
 *
 * @author Robert Mollard
 */
public final class DateParser implements FieldParser {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -8897023318111093098L;

    /**
     * Formatter for formatting Dates.
     */
    private final DateFormat formatter;

    /**
     * Construct a new <code>DateParser</code> for strings
     * in the given date format.
     *
     * @param dateFormat A <code>SimpleDateFormat</code>
     *        format string, for example "yyyy/MM/dd"
     */
    public DateParser(final String dateFormat) {
        formatter = new SimpleDateFormat(dateFormat);
    }

    /** {@inheritDoc} */
    public DateField valueOf(final String stringToParse) {
        Date date;
        try {
            date = formatter.parse(stringToParse);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Could not parse date string: "
                    + stringToParse);
        }
        return new DateField(date);
    }

    /** {@inheritDoc} */
    public String getString(final Field field) {
        if (!(field instanceof DateField)) {
            throw new IllegalArgumentException("Expected a DateField");
        }
        final DateField dateField = (DateField) field;

        return formatter.format(dateField.getValue());
    }

    /**
     * Get a string describing the parser.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Date parser";
    }

}
