package suncertify.fields.viewers;

import suncertify.fields.BooleanField;
import suncertify.utils.Localization;

/**
 * Trivial viewer class to display a <code>BooleanField</code>
 * as a localized string.
 *
 * @author Robert Mollard
 */
public final class BooleanFieldView implements FieldView<BooleanField> {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -7366303432939974510L;

    /**
     * Default public constructor.
     */
    public BooleanFieldView() {
        //Empty
    }

    /** {@inheritDoc} */
    public String getDisplayString(final BooleanField field) {
        final String result;

        if (field.getValue()) {
            result = Localization.getString("client.boolean.true");
        } else {
            result = Localization.getString("client.boolean.false");
        }
        return result;
    }

    /** {@inheritDoc} */
    public FieldAlignment getAlignment() {
        return FieldAlignment.CENTER;
    }

    /** {@inheritDoc} */
    public FieldWidth getFieldWidth() {
        return FieldWidth.NARROW;
    }

}
