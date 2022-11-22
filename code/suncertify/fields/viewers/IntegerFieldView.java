package suncertify.fields.viewers;

import suncertify.fields.IntegerField;

/**
 * Viewer class to display an <code>IntegerField</code> as a string.
 * Null values are shown as blank strings.
 *
 * @author Robert Mollard
 */
public final class IntegerFieldView implements FieldView<IntegerField> {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -5634986115387783445L;

    /**
     * Default public constructor.
     */
    public IntegerFieldView() {
        //Empty
    }

    /** {@inheritDoc} */
    public String getDisplayString(final IntegerField field) {
        final Integer value = field.getValue();
        final String result;

        if (value == null) {
            result = "";
        } else {
            result = value.toString();
        }
        return result;
    }

    /** {@inheritDoc} */
    public FieldAlignment getAlignment() {
        return FieldAlignment.RIGHT;
    }

    /** {@inheritDoc} */
    public FieldWidth getFieldWidth() {
        return FieldWidth.NORMAL;
    }

}
