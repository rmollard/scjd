package suncertify;

import java.io.Serializable;

import suncertify.fields.Field;

/**
 * A search request criterion for a single database field.
 *
 * @author Robert Mollard
 */
public class Criterion implements Serializable {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -5286299025487965923L;

    /**
     * The value we are searching through the records for.
     * Can be null, in which case anything will match.
     */
    private final Field value;

    /**
     * Create a new instance with the given field as the value to match.
     * If the given <code>Field</code> is null,
     * any <code>Field</code> will match.
     *
     * @param value the value to match. Can be null.
     */
    public Criterion(final Field value) {
        this.value = value;
    }

    /**
     * Get the <code>Field</code> to match.
     *
     * @return the <code>Field</code> to match
     */
    public final Field getValue() {
        return value;
    }

    /**
     * Determine if we have a match for this <code>Field</code> by using the
     * field's <code>compareTo</code> method. If our criterion field is
     * <code>null</code>, we always return true.
     *
     * @param otherValue the value to compare to
     * @return true if matches
     */
    public boolean matches(final Field otherValue) {
        final boolean result;

        if (value == null) {
            result = true; //Null matches everything
        } else {
            result = (value.compareTo(otherValue) == 0);
        }
        return result;
    }

    /**
     * Get a string describing the criterion.
     * The criterion's field value is given.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return new StringBuilder()
            .append("Criterion [")
            .append(" Value: ").append(value).append('\n')
            .append(']')
            .toString();
    }

}
