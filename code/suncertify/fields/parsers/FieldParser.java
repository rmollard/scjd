package suncertify.fields.parsers;

import java.io.Serializable;

import suncertify.fields.Field;

/**
 * An parser for parsing database field values according to some database
 * file format.
 * The methods <code>valueOf</code> and <code>getString</code>
 * are inverses of each other.
 *
 * Classes implementing this interface must be thread safe.
 *
 * @author Robert Mollard
 */
public interface FieldParser extends Serializable {

    /**
     * Creates a new Field instance by parsing the given String according
     * to a database-specific format.
     * This method is the inverse of the <code>getString</code> method.
     *
     * @param stringToParse The string to parse
     * @return a new Field instance
     * @throws IllegalArgumentException if unable to parse string
     */
    Field valueOf(String stringToParse);

    /**
     * Gets the value of the given Field in a database-specific string format.
     * This method is the inverse of the <code>valueOf</code> method.
     *
     * @param field the field to parse
     * @return A string representation of the field
     */
    String getString(Field field);

}
