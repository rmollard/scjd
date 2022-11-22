package suncertify.fields.parsers;

import java.math.BigDecimal;

import suncertify.fields.CurrencyField;
import suncertify.fields.Field;

/**
 * A basic parser for "normal" decimal currency values,
 * with a prefix such as a dollar sign.
 *
 * This class is immutable.
 *
 * @author Robert Mollard
 */
public final class PrefixedCurrencyParser implements FieldParser {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -1627948256641661393L;

    /**
     * The number of "cents" in a "dollar".
     * For US dollars, this will be 100.
     * For currencies that do not have cents
     * (such as the Yen) this will be 1.
     */
    private final int centsPerDollar;

    /**
     * The number of decimal places to round to.
     */
    private final int decimalPlaces;

    /**
     * Construct a new <code>PrefixedCurrencyParser</code>.
     *
     * @param centsPerDollar how many "cents" are in a "dollar" of the
     *        currency. For US dollars, this will be 100.
     *        For currencies that do not have cents (such
     *        as the Yen) this will be 1.
     * @param decimalPlaces the number of decimal places to display
     */
    public PrefixedCurrencyParser(final int centsPerDollar,
            final int decimalPlaces) {
        this.centsPerDollar = centsPerDollar;
        this.decimalPlaces = decimalPlaces;
    }

    /** {@inheritDoc} */
    public CurrencyField valueOf(final String stringToParse) {
        final int unknown = -1;

        //The index of the first digit or decimal point
        int firstIndex = unknown;
        for (int i = 0;
            i < stringToParse.length() && firstIndex == unknown; i++) {
            final char c = stringToParse.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                firstIndex = i;
            }
        }

        if (firstIndex == unknown) {
            throw new IllegalArgumentException(
                    "Currency string has no numbers: " + stringToParse);
        }

        //The currency prefix, for example "$US"
        final String prefix = stringToParse.substring(0, firstIndex);

        String numberValue = stringToParse.substring(firstIndex);
        BigDecimal amount;
        try {
            amount = new BigDecimal(numberValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Could not parse string as currency: " + stringToParse);
        }

        //Get the amount in cents
        amount = amount.multiply(new BigDecimal(centsPerDollar));

        return new CurrencyField(prefix, amount.longValue(), centsPerDollar);
    }

    /** {@inheritDoc} */
    public String getString(final Field field) {
        if (!(field instanceof CurrencyField)) {
            throw new IllegalArgumentException("Expected a CurrencyField");
        }
        final CurrencyField currencyField = (CurrencyField) field;

        BigDecimal amount = new BigDecimal(currencyField.getValue());
        //Get the amount in dollars
        amount = amount.divide(new BigDecimal(centsPerDollar));
        amount = amount.setScale(decimalPlaces);

        return currencyField.getPrefix() + amount.toString();
    }

    /**
     * Get a string describing the parser.
     * The string contains the number of cents per dollar,
     * and the number of decimal places.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Prefixed currency parser" + "\n[cents per dollar: "
                + centsPerDollar + "\n" + "decimal places: " + decimalPlaces
                + "]";
    }

}
