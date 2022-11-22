package suncertify;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import suncertify.fields.Field;

/**
 * Business logic to determine if a given
 * record should be modifiable by a user.
 *
 * @author Robert Mollard
 */
public interface RecordModificationPolicy extends Serializable {

    /**
     * Determine if a record with the given fields can be modified by a user.
     *
     * @param fields the fields of the record to check,
     *        in the order expected by the table schema
     * @param currentServerDate the current server date
     * @return true if the record can be modified
     */
    boolean isRecordModifiable(List<Field> fields, Date currentServerDate);

}
