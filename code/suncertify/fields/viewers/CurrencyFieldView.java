package suncertify.fields.viewers;

import java.math.BigDecimal;

import suncertify.fields.CurrencyField;

/**
 * Trivial viewer class to display a <code>CurrencyField</code>
 * as a string.
 *
 * @author Robert Mollard
 */
public final class CurrencyFieldView implements FieldView<CurrencyField> {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 2219499001119040292L;

    /**
     * Default public constructor.
     */
    public CurrencyFieldView() {
        //Empty
    }

    /** {@inheritDoc} */
    public String getDisplayString(final CurrencyField field) {
        BigDecimal dollars = new BigDecimal(field.getValue());
        //Convert from cents to dollars
        dollars = dollars.divide(new BigDecimal(field.getCentsPerDollar()));
        dollars = dollars.setScale(2);

        return field.getPrefix() + dollars.toString();
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
