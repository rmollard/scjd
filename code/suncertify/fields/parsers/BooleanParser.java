package suncertify.fields.parsers;

import suncertify.fields.BooleanField;
import suncertify.fields.Field;

/**
 * A parser for boolean values.
 *
 * This class is immutable.
 *
 * @author Robert Mollard
 */
public final class BooleanParser implements FieldParser {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 926517208984713188L;

    /**
     * The String representing true, for example "T".
     */
    private final String trueString;

    /**
     * The String representing false, for example "F".
     */
    private final String falseString;

    /**
     * Constructs a new boolean parser.
     *
     * @param trueString the String representing true, e.g. "T" or "Y"
     * @param falseString the String representing false, e.g. "F" or "N"
     */
    public BooleanParser(final String trueString, final String falseString) {
        this.trueString = trueString;
        this.falseString = falseString;
    }

    /** {@inheritDoc} */
    public BooleanField valueOf(final String stringToParse) {
        final BooleanField result;

        if (trueString.equals(stringToParse)) {
            result = new BooleanField(true);
        } else if (falseString.equals(stringToParse)) {
            result = new BooleanField(false);
        } else {
            throw new IllegalArgumentException("Expected " + trueString
                    + " or " + falseString + " but got " + stringToParse);
        }
        return result;
    }

    /** {@inheritDoc} */
    public String getString(final Field field) {
        if (!(field instanceof BooleanField)) {
            throw new IllegalArgumentException("Expected a BooleanField");
        }
        final BooleanField booleanField = (BooleanField) field;
        final String result;

        if (booleanField.getValue()) {
            result = trueString;
        } else {
            result = falseString;
        }
        return result;
    }

    /**
     * Get a string describing the parser.
     * The string contains the values of the "true" and
     * "false" strings used.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Boolean parser" + "\n[true: '" + trueString + "'\n"
                + "false: '" + falseString + "']";
    }

}
