package suncertify.db;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import suncertify.RecordNotLockedException;
import suncertify.StaleRecordException;
import suncertify.fields.Field;

/**
 * Interface for something that manages concurrent requests
 * to a <code>Table</code>.
 * Implementors must be thread safe.
 *
 * @author Robert Mollard
 */
public interface TableRequestManager {

    /**
     * Get the record numbers of all records that match the given criteria.
     * Deleted records are never returned.
     *
     * A null value in criteria[n] matches any field
     * value. A non-null  value in criteria[n] matches any field
     * value that begins with criteria[n]. (For example, "Fred"
     * matches "Fred" or "Freddy".)
     *
     * @param criteria an array of Strings corresponding
     *        to the fields of a record. Any entry may be null.
     * @return a collection of matching records
     */
    Collection<Record> getMatchingRecords(String[] criteria);

    /**
     * Lock the record with the given record number.
     * The record must exist and not be deleted.
     * If the record is already locked
     * by this client, nothing will happen.
     *
     * If the record is locked by some other client,
     * this thread will wait until
     * the record is available.
     *
     * If an exception is thrown, the record will not be locked.
     *
     * @param recordNumber the record number
     * @param client the client requesting the lock
     * @param lastKnownTimestamp what the record's timestamp was when the
     *        client last read the record
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     * @throws StaleRecordException if lastKnownTimestamp is older than
     *         the record's timestamp
     */
    void lock(int recordNumber, DBMain client, SerialNumber lastKnownTimestamp)
            throws RecordNotFoundException, StaleRecordException;

    /**
     * Unlock the record with the given record number.
     * The record must exist and not be marked as deleted.
     *
     * The record must be locked by the client.
     *
     * @param recordNumber the record number
     * @param client the client requesting the lock
     * @param lastKnownTimestamp what the record's timestamp was when the
     *        client last read the record
     *
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     * @throws StaleRecordException if the client's copy of the
     *         timestamp for the record is older than the record's timestamp
     * @throws RecordNotLockedException if the client does not hold
     *         the lock for the requested record
     */
    void unlock(int recordNumber, DBMain client,
            SerialNumber lastKnownTimestamp)
            throws RecordNotFoundException, StaleRecordException,
            RecordNotLockedException;

    /**
     * Returns the current lock status of the given record.
     *
     * @param recordNumber the record number. Starts at 0.
     * @return true if the record is currently locked by any client
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     */
    boolean isRecordLocked(int recordNumber) throws RecordNotFoundException;

    /**
     * Get the specified <code>Record</code>.
     * The <code>Record</code> is checked to make sure it
     * exists and is not marked as "deleted".
     *
     * @param recordNumber the record number
     *
     * @return the <code>Record</code> (guaranteed not to be null)
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     */
    Record getRecord(final int recordNumber) throws RecordNotFoundException;

    /**
     * Modify a record. The record must exist and not be marked as deleted.
     *
     * @param recordNumber the record number
     * @param client the client requesting the lock
     * @param lastKnownTimestamp what the record's timestamp was when the
     *        client last read the record
     * @param newFields the new Field values for the record.
     *         Any element may be null, in which case the field will remain
     *         unchanged. The elements must be in the order defined by
     *         the table schema.
     *
     * @return the record's new timestamp
     *
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     * @throws StaleRecordException if the client's copy of the
     *         timestamp for the record is older than the record's timestamp
     * @throws IOException if there was a problem writing the file
     * @throws RecordNotLockedException if the client does not hold
     *         the lock for the requested record
     *
     * @throws IllegalArgumentException if newFields is has the wrong
     *         number of fields, or cannot be parsed
     */
    SerialNumber modifyRecord(int recordNumber, DBMain client,
            SerialNumber lastKnownTimestamp, List<Field> newFields)
            throws RecordNotFoundException, StaleRecordException, IOException,
            RecordNotLockedException;

    /**
     * Mark the requested record for deletion.
     * A record that is marked as "deleted" can be overwritten by a new record.
     *
     * The client must hold a lock for the record.
     *
     * The client will lose the lock unless an exception is thrown.
     *
     * @param recordNumber the record number
     * @param client the client requesting the lock
     * @param lastKnownTimestamp what the record's timestamp was when the
     *        client last read the record
     *
     * @return the new serial number of the record
     *         that is to be marked as deleted
     *
     * @throws IOException if there was a problem updating the file
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     * @throws StaleRecordException if the client's copy of the
     *         timestamp for the record is older than the record's timestamp
     * @throws RecordNotLockedException if the client does not hold
     *         the lock for the requested record
     */
    SerialNumber deleteRecord(int recordNumber, DBMain client,
            SerialNumber lastKnownTimestamp) throws RecordNotFoundException,
            StaleRecordException, IOException, RecordNotLockedException;

    /**
     * Create a new <code>Record</code> with the given field values.
     *
     * @param fields the field values to use
     * @return the newly created <code>Record</code>
     * @throws IOException if there was a problem writing the file
     */
    Record createRecord(final List<Field> fields) throws IOException;

    /**
     * Get the table schema.
     *
     * @return the table schema
     */
    TableSchema getSchema();

    /**
     * Get the parsers for the table's fields.
     *
     * @return the field parsers
     */
    FieldParsers getFieldParsers();

    /**
     * Get a list of fields representing the given array of strings.
     * Each field will be in the format expected by the corresponding
     * Field's parser.
     *
     * @param fieldsAsStrings the array of string values
     * @return a list of fields representing the given strings
     */
    List<Field> getFieldListFromStringArray(final String[] fieldsAsStrings);

    /**
     * Get an array of strings representing the given list of fields.
     * Each string will be in the format expected by the corresponding
     * Field's parser.
     *
     * @param fields the list of fields
     * @return an array of strings representing the given fields
     */
    String[] getStringArrayFromFieldList(final List<Field> fields);

    /**
     * Perform a graceful shutdown.
     */
    void shutDown();

}
