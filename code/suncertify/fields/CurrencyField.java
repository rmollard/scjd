package suncertify.fields;

/**
 * An immutable currency database field. The value stored is the number of
 * cents, which allows us to use a non-floating point number to avoid loss of
 * precision.
 *
 * @author Robert Mollard
 */
public final class CurrencyField implements Field {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -8873634835388702063L;

    /**
     * The currency prefix, e.g. "US$". Never null.
     */
    private final String prefix;

    /**
     * The total value, in cents.
     */
    private final long value;

    /**
     * The number of "cents" in a "dollar". For US dollars, this will be 100.
     * For currencies that do not have cents (such as the Yen) this will be 1.
     */
    private final int centsPerDollar;

    /**
     * Construct a new integer field with the given value.
     *
     * @param prefix the currency prefix, e.g. "US$". May be null.
     * @param cents the number of cents
     * @param centsPerDollar the number of cents in each dollar
     */
    public CurrencyField(final String prefix, final long cents,
            final int centsPerDollar) {

        if (prefix == null) {
            this.prefix = "";
        } else {
            this.prefix = prefix;
        }

        this.value = cents;
        this.centsPerDollar = centsPerDollar;
    }

    /**
     * Get the value contained by this <code>CurrencyField</code>.
     *
     * @return the value contained, in "cents" of the currency
     */
    public long getValue() {
        return value;
    }

    /**
     * Get the number of cents in a dollar for this currency.
     *
     * @return the number of cents per dollar
     */
    public int getCentsPerDollar() {
        return centsPerDollar;
    }

    /**
     * Get the prefix for this type of currency.
     *
     * @return the String prefix, e.g. "US$"
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Get a string describing the field.
     * The string contains the prefix and the
     * value in cents.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Currency Field [prefix: '" + prefix
                + "', value: " + value + " cents]";
    }

    /**
     * Compare this currency field to another currency field.
     *
     * @param other the <code>Field</code> to be compared.
     *        Must be a <code>CurrencyField</code>
     * @return the lexicographic comparison of the currency prefixes.
     *         If the prefixes are the same, the currency values will
     *         be compared and the return value will be -1 if this object's
     *         value is less than the other object's value, 1 if this object's
     *         value is greater than the other object's value, or 0 if both
     *         values are the same.
     * @throws ClassCastException if <code>other</code> is not a
     *         <code>CurrencyField</code>
     */
    public int compareTo(final Field other) {
        int result = 0;
        CurrencyField that = (CurrencyField) other;

        //Group currencies together
        if (this.prefix.equals(that.prefix)) {
            //Prefixes match, do a numerical comparison
            if (this.value < that.value) {
                result = -1;
            }
            if (this.value > that.value) {
                result = 1;
            }
        } else {
            /*
             * Prefixes are different, just do a String compare so that
             * amounts in the same currency are grouped together.
             */
            result = this.prefix.compareTo(that.prefix);
        }
        return result;
    }

}
