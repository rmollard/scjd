package suncertify.fields.viewers;

import java.text.DateFormat;

import suncertify.fields.DateField;

/**
 * Viewer class to display a <code>DateField</code>
 * in the local format.
 *
 * @author Robert Mollard
 */
public final class DateFieldView implements FieldView<DateField> {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 4532796904574002311L;

    /**
     * Default public constructor.
     */
    public DateFieldView() {
        //Empty
    }

    /** {@inheritDoc} */
    public String getDisplayString(final DateField field) {
        final String result;

        //Format will depend on the current locale
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        result = dateFormat.format(field.getValue());
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
