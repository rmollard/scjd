package suncertify.fields;

/**
 * An immutable boolean database field.
 *
 * @author Robert Mollard
 */
public final class BooleanField implements Field {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -4312842270830355374L;

    /**
     * The value contained.
     */
    private final boolean value;

    /**
     * Construct a new <code>BooleanField</code> with the given value.
     *
     * @param value the value to set
     */
    public BooleanField(final boolean value) {
        this.value = value;
    }

    /**
     * Get the value contained by this <code>BooleanField</code>.
     *
     * @return the boolean value contained
     */
    public boolean getValue() {
        return value;
    }

    /**
     * Get a string describing the field.
     * The string contains the value (true or false).
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Boolean field [" + value + ']';
    }

    /**
     * Compare this boolean field's value to another
     * boolean field's value.
     *
     * @param other the <code>Field</code> to be compared.
     *        Must be a <code>BooleanField</code>
     * @return zero if this object represents the same boolean value as the
     *         argument; a positive value if this object represents true
     *         and the argument represents false; and a negative value if
     *         this object represents false and the argument represents true.
     * @throws ClassCastException if <code>other</code> is not a
     *         <code>BooleanField</code>
     */
    public int compareTo(final Field other) {
        BooleanField that = (BooleanField) other;
        int result = Boolean.valueOf(value).compareTo(
                Boolean.valueOf(that.value));
        return result;
    }

}
