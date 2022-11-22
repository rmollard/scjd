package suncertify.fields.parsers;

import suncertify.fields.Field;
import suncertify.fields.StringField;

/**
 * Trivial string parser, just uses the string as it is.
 *
 * This class is immutable.
 *
 * @author Robert Mollard
 */
public final class PlainStringParser implements FieldParser {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -4895491537463823929L;

    /**
     * Default public constructor.
     */
    public PlainStringParser() {
        //Empty
    }

    /** {@inheritDoc} */
    public StringField valueOf(final String stringToParse) {
        return new StringField(stringToParse);
    }

    /** {@inheritDoc} */
    public String getString(final Field field) {
        if (!(field instanceof StringField)) {
            throw new IllegalArgumentException("Expected a StringField");
        }
        final StringField stringField = (StringField) field;
        return stringField.getValue();
    }

    /**
     * Get a string describing the parser.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Plain string parser";
    }

}
