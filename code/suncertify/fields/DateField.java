package suncertify.fields;

import java.util.Date;

/**
 * An immutable date database field.
 *
 * @author Robert Mollard
 */
public final class DateField implements Field {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 6228882167684542731L;

    /**
     * We store the value returned by the <code>getTime</code> method instead
     * of the actual <code>Date</code> object itself. This makes it slightly
     * easier to ensure that the <code>DateField</code> object is immutable.
     */
    private final long value;

    /**
     * Construct a new <code>DateField</code> with the given value.
     *
     * @param value the value to set (must not be null)
     */
    public DateField(final Date value) {
        this.value = value.getTime();
    }

    /**
     * Get the value contained by this <code>DateField</code>.
     *
     * @return the <code>Date</code> value contained
     */
    public Date getValue() {
        return new Date(value);
    }

    /**
     * Get a string describing the field.
     * The string contains the date value.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Date field [" + new Date(value) + "]";
    }

    /**
     * Compare this date field to another date field.
     *
     * @param other the <code>Field</code> to be compared.
     *        Must be a <code>DateField</code>
     * @return the value <code>0</code> if the argument Date is equal to
     *         this Date; a value less than <code>0</code> if this Date
     *         is before the Date argument; and a value greater than
     *         <code>0</code> if this Date is after the Date argument.
     * @throws ClassCastException if <code>other</code> is not a
     *         <code>DateField</code>
     */
    public int compareTo(final Field other) {
        DateField that = (DateField) other;

        return new Date(value).compareTo(new Date(that.value));
    }

}
