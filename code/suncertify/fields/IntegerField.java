package suncertify.fields;

/**
 * An immutable integer database field. Note that the value can be null.
 *
 * @author Robert Mollard
 */
public final class IntegerField implements Field {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 6821678543842766967L;

    /**
     * The value contained. Might be null.
     */
    private final Integer value;

    /**
     * Construct a new <code>IntegerField</code> with the given value.
     *
     * @param value the value to set
     */
    public IntegerField(final Integer value) {
        this.value = value;
    }

    /**
     * Get the value contained by this
     * <code>IntegerField</code> (might be null).
     *
     * @return the <code>Integer</code> value contained
     */
    public Integer getValue() {
        return value;
    }

    /**
     * Get a string describing the field.
     * The string contains the integer value of the field.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Integer field [" + value + ']';
    }

    /**
     * Compare this integer field to another integer field.
     *
     * @param other the <code>Field</code> to be compared.
     *        Must be a <code>IntegerField</code>
     * @return -1 if this field's value if null and the other
     *         field's value is not null;
     *         1 if this field's value is not null and the other
     *         field's value is null;
     *         0 if both field's values are null or the same;
     *         -1 if this field's value is less than the other field's value;
     *         1 if this field's value is greater than the other field's value
     * @throws ClassCastException if <code>other</code> is not an
     *         <code>IntegerField</code>
     */
    public int compareTo(final Field other) {
        final IntegerField that = (IntegerField) other;
        final int result;

        if (this.value == null && that.value != null) {
            result = -1;
        } else if (this.value != null && that.value == null) {
            result = 1;
        } else {
            final Integer ours;
            if (this.value == null) {
                ours = Integer.MIN_VALUE;
            } else {
                ours = value;
            }

            final Integer theirs;
            if (that.value == null) {
                theirs = Integer.MIN_VALUE;
            } else {
                theirs = that.value;
            }

            result = ours.compareTo(theirs);
        }
        return result;
    }

}
