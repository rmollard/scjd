package suncertify.fields;

/**
 * Trivial class to contain a string.
 *
 * This class is immutable.
 *
 * @author Robert Mollard
 */
public final class StringField implements Field {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 5152500913653381517L;

    /**
     * The <code>String</code> contained. Never null.
     */
    private final String value;

    /**
     * Construct a new <code>StringField</code> that will
     * contain the given value.
     *
     * @param value the <code>String</code> to use.
     *        A null value is treated as an empty string.
     */
    public StringField(final String value) {
        if (value == null) {
            this.value = "";
        } else {
            this.value = value;
        }
    }

    /**
     * Get the value contained by this <code>StringField</code>.
     *
     * @return the <code>String</code> value contained
     */
    public String getValue() {
        return value;
    }

    /**
     * Get a string describing the field.
     * The string contains the string value of the field.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "String field [" + value + ']';
    }

    /**
     * Compare this string field to another string field.
     *
     * @param other the <code>Field</code> to be compared.
     *        Must be a <code>StringField</code>
     * @return the case-insensitive lexicographic comparison of
     *         this field's string, and the other field's string.
     * @throws ClassCastException if <code>other</code> is not an
     *         <code>StringField</code>
     */
    public int compareTo(final Field other) {
        final StringField that = (StringField) other;
        return this.value.compareToIgnoreCase(that.value);
    }

}
