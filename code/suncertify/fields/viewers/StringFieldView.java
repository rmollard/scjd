package suncertify.fields.viewers;

import suncertify.fields.StringField;

/**
 * Trivial viewer class to display a <code>StringField</code> as a string.
 * Null values are shown as blank strings.
 *
 * @author Robert Mollard
 */
public final class StringFieldView implements FieldView<StringField> {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 2722288840202520939L;

    /**
     * Default public constructor.
     */
    public StringFieldView() {
        //Empty
    }

    /** {@inheritDoc} */
    public String getDisplayString(final StringField field) {
        final String value = field.getValue();
        final String result;

        if (value == null) {
            result = "";
        } else {
            result = value;
        }
        return result;
    }

    /** {@inheritDoc} */
    public FieldAlignment getAlignment() {
        return FieldAlignment.LEFT;
    }

    /** {@inheritDoc} */
    public FieldWidth getFieldWidth() {
        return FieldWidth.WIDE;
    }

}
