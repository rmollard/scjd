package suncertify.db;

import java.io.Serializable;
import java.util.List;

import suncertify.fields.Field;
import suncertify.fields.FieldDetails;

/**
 * Interface for getting information about the
 * schema of a <code>Table</code>.
 *
 * @author Robert Mollard
 */
public interface TableSchema extends Serializable {

    /**
     * Get the index of a <code>Field</code> by its name.
     *
     * @param fieldName the name of the Field
     * @return the index of the Field with the given name
     * @throws IllegalArgumentException if
     *         <code>fieldName</code> was not found
     */
    int getFieldIndexByName(String fieldName);

    /**
     * Get the class of a <code>Field</code> by its name.
     *
     * @param fieldName the name of the <code>Field</code>
     * @return the class of the <code>Field</code> with the given name,
     *         or null if not found
     * @throws IllegalArgumentException if
     *         <code>fieldName</code> was not found
     */
    Class<? extends Field> getFieldClassByName(String fieldName);

    /**
     * Get the class of a <code>Field</code> by
     * its index in the table schema.
     *
     * @param fieldIndex the index of the
     *        desired <code>Field</code>, starts at 0
     * @return the class of the <code>Field</code> with the given index,
     *         or null if not found
     * @throws IllegalArgumentException if
     *         <code>fieldIndex</code> was not found
     */
    Class<? extends Field> getFieldClassByIndex(int fieldIndex);

    /**
     * Get all the field details for the schema.
     *
     * @return details for all the fields
     */
    List<FieldDetails> getFields();

}
