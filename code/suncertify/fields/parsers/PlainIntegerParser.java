package suncertify.fields.parsers;

import suncertify.fields.Field;
import suncertify.fields.IntegerField;

/**
 * A basic parser for integer values.
 * An empty string is allowed, in that case the
 * <code>Integer</code> will be <code>null</code>.
 *
 * This class is immutable.
 *
 * @author Robert Mollard
 */
public final class PlainIntegerParser implements FieldParser {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 4794484383055006719L;

    /**
     * Default public constructor.
     */
    public PlainIntegerParser() {
        //Empty
    }

    /** {@inheritDoc} */
    public IntegerField valueOf(final String stringToParse) {
        Integer i = null;
        if (!"".equals(stringToParse)) {
            //String is not empty, try to parse it
            try {
                i = Integer.valueOf(stringToParse);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Could not parse string", e);
            }
        }
        return new IntegerField(i);
    }

    /** {@inheritDoc} */
    public String getString(final Field field) {
        if (!(field instanceof IntegerField)) {
            throw new IllegalArgumentException("Expected an IntegerField");
        }
        final IntegerField intField = (IntegerField) field;

        String result = "";
        final Integer value = intField.getValue();
        if (value != null) {
            result = value.toString();
        }
        return result;
    }

    /**
     * Get a string describing the parser.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Plain integer parser";
    }

}
