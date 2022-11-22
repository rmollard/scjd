package suncertify.fields.viewers;

import java.io.Serializable;

import suncertify.fields.Field;


/**
 * A view of a Field. Fields could be displayed differently
 * based on Locale etc.
 *
 * @author Robert Mollard
 *
 * @param <E> the field type parsed by this parser
 */
public interface FieldView<E extends Field> extends Serializable {

    /**
     * Get a localized string for a given Field.
     *
     * @param field The Field to get a displayable String for
     * @return A localized string to display to the user.
     */
    String getDisplayString(final E field);

    /**
     * Get the horizontal alignment of the display
     * string (e.g. right-justified).
     *
     * @return a FieldAlignment enum
     */
    FieldAlignment getAlignment();

    /**
     * Get the preferred field display width.
     *
     * @return preferred width of the field
     */
    FieldWidth getFieldWidth();
}
