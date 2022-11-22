package suncertify.db;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import suncertify.fields.Field;

/**
 * A database table. Classes that implement this
 * interface should be thread safe.
 *
 * @author Robert Mollard
 */
public interface Table {

    /**
     * Add an existing <code>Record</code> to the table,
     * for example when populating
     * the table from a database file.
     * The <code>Record</code> will not be written to file.
     *
     * @param existingRecord the <code>Record</code> to add
     */
    void addExistingRecord(Record existingRecord);

    /**
     * Add a <code>Record</code> to the table, and write it to file.
     * If there is an existing <code>Record</code>
     * with the same record number, it will be overwritten.
     *
     * @param newRecord the <code>Record</code> to add
     * @throws IOException if the <code>Record</code>
     *         could not be written to file
     */
    void addModifiedRecord(Record newRecord) throws IOException;

    /**
     * Create a <code>Record</code> from the given fields,
     * and add it to the table.
     * The new <code>Record</code> will be written to the database file,
     * possibly reusing the number of a deleted <code>Record</code>.
     * The new <code>Record</code> will not be marked as deleted.
     *
     * @param fields the fields of the <code>Record</code> to be created
     * @return the newly created <code>Record</code>
     * @throws IOException if the <code>Record</code>
     *         could not be written to file
     */
    Record createRecord(List<Field> fields) throws IOException;

    /**
     * Get the <code>Record</code> with the given number,
     * or null if not found.
     *
     * @param recordNumber the record number
     * @return the <code>Record</code> with the given number
     */
    Record getRecord(int recordNumber);

    /**
     * Get all the records, together with their locks.
     * The collection returned may not reflect changes
     * to records that occur while the method is
     * being performed.
     *
     * @return all the records and their locks
     */
    Collection<LockableRecord> getAllRecords();

    /**
     * Get the schema for this <code>Table</code>.
     *
     * @return the table schema
     */
    TableSchema getSchema();

    /**
     * Get the parsers for the fields in this <code>Table</code>.
     *
     * @return the field parsers
     */
    FieldParsers getFieldParsers();

    /**
     * Add the record number of a deleted <code>Record</code> to
     * the delete list so that the number
     * can be reused when a new <code>Record</code> is created.
     *
     * @param recordNumber the record number
     *        of the deleted <code>Record</code>
     */
    void addToDeleteList(int recordNumber);

    /**
     * Shuts down the <code>Table</code> gracefully, ensuring that the
     * data file does not become corrupted.
     */
    void shutDown();
}
