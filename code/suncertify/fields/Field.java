package suncertify.fields;

import java.io.Serializable;

/**
 * A field in a record.
 * Fields should always be immutable.
 * Fields are parsed by <code>FieldParser</code> implementors.
 *
 * @author Robert Mollard
 */
public interface Field extends Serializable, Comparable<Field> {
    //Tagging interface
}
