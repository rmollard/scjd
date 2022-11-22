package suncertify.db;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import suncertify.fields.Field;
import suncertify.server.FileHandler;

/**
 * A database table that combines the functionality of a
 * <code>Table</code> and a <code>LockManager</code>
 * in order to manage its own locks.
 *
 * We maintain a collection of <code>LockableRecord</code>
 * objects in a <code>ConcurrentMap</code>, in a similar
 * fashion to Multiversion Concurrency Control
 * but with only a single version of each record.
 *
 * The locks are never deleted, but they may be reused
 * if their corresponding <code>Record</code> is deleted
 * and replaced with another <code>Record</code>.
 *
 * This class is thread safe.
 *
 * @author Robert Mollard
 */
final class CombinedTable implements LockManager, Table {

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify.db");

    /**
     * The name of this class, used by the logger.
     */
    private static final String THIS_CLASS = "suncertify.db.CombinedTable";

    /**
     * The table schema.
     */
    private final TableSchema schema;

    /**
     * Parser for each field in the table.
     */
    private final FieldParsers fieldParsers;

    /**
     * File handler to manage reading and writing to the file.
     */
    private final FileHandler fileHandler;

    /**
     * Record numbers of deleted records.
     * This allows us to reuse record numbers.
     * If a record number is in the list, the record must be unlocked.
     */
    private final ConcurrentStack<Integer> deleteList;

    /**
     * A map of all the records in the table, and their locks.
     * We use the record number as the key.
     */
    private final ConcurrentMap<Integer, LockableRecord> records;

    /**
     * The next free record number, ignoring any "holes" due to records
     * being deleted. This number will need to be incremented when a new
     * record is created without reusing a deleted record's number.
     * This number can never decrease.
     */
    private final AtomicInteger nextFreeRecordNumber = new AtomicInteger();

    /**
     * Create a new instance. There will usually only be one
     * table for each file.
     *
     * @param schema the table schema
     * @param fieldParsers parsers for the table's fields
     * @param fileHandler the file handler to use when writing records to file
     */
    public CombinedTable(final TableSchema schema,
            final FieldParsers fieldParsers, final FileHandler fileHandler) {
        this.schema = schema;
        this.fieldParsers = fieldParsers;
        this.fileHandler = fileHandler;
        this.records = new ConcurrentHashMap<Integer, LockableRecord>();
        /*
         * We use OptimisticConcurrentStack in the expectation that
         * there will not be very many concurrent modifications.
         */
        this.deleteList = new OptimisticConcurrentStack<Integer>();
    }

    /** {@inheritDoc} */
    public Collection<LockableRecord> getAllRecords() {
        return records.values();
    }

    /** {@inheritDoc} */
    public TableSchema getSchema() {
        return schema;
    }

    /** {@inheritDoc} */
    public FieldParsers getFieldParsers() {
        return fieldParsers;
    }

    /** {@inheritDoc} */
    public Record getRecord(final int recordNumber) {
        final Record result;
        LockableRecord fullRecord = records.get(recordNumber);

        if (fullRecord == null) {
            result = null;
        } else {
            result = fullRecord.getRecord();
        }
        return result;
    }

    /** {@inheritDoc} */
    public void addModifiedRecord(final Record newRecord) throws IOException {
        putRecord(newRecord, true);
        writeToFile(newRecord);
    }

    /** {@inheritDoc} */
    public void addExistingRecord(final Record newRecord) {

        //Increment nextFreeRecordNumber
        nextFreeRecordNumber.getAndIncrement();

        //Add to table but don't write to file
        putRecord(newRecord, false);
    }

    /** {@inheritDoc} */
    public Record createRecord(final List<Field> fields) throws IOException {
        LOGGER.entering(THIS_CLASS, "createRecord");
        final SerialNumber timestamp = SerialNumber.createSerialNumber();
        final boolean isReused;
        final Integer recordNumber;

        //Try to reuse a deleted number if possible
        Integer reusedNumber = deleteList.pop();
        if (reusedNumber != null) {
            isReused = true;
            recordNumber = reusedNumber;
        } else {
            isReused = false;
            recordNumber = nextFreeRecordNumber.getAndIncrement();
        }

        Record newRecord = new Record(recordNumber, timestamp, false, fields);
        putRecord(newRecord, isReused);
        writeToFile(newRecord);

        LOGGER.exiting(THIS_CLASS, "createRecord");

        return newRecord;
    }

    /** {@inheritDoc} */
    public void lock(final int recordNumber, final Object client)
            throws RecordNotFoundException {
        LockableRecord recordAndLock = records.get(recordNumber);
        if (recordAndLock == null) {
            throw new RecordNotFoundException(recordNumber,
                    "Record does not exist");
        }
        recordAndLock.lock(client);
    }

    /** {@inheritDoc} */
    public void unlock(final int recordNumber, final Object client)
            throws RecordNotFoundException {
        LockableRecord recordAndLock = records.get(recordNumber);
        if (recordAndLock == null) {
            throw new RecordNotFoundException(recordNumber,
                    "Record does not exist");
        }
        recordAndLock.unlock(client);
    }

    /** {@inheritDoc} */
    public Object getLockOwner(final int recordNumber)
            throws RecordNotFoundException {
        LockableRecord recordAndLock = records.get(recordNumber);
        if (recordAndLock == null) {
            throw new RecordNotFoundException(recordNumber,
                    "Record does not exist");
        }
        return recordAndLock.getOwner();
    }

    /** {@inheritDoc} */
    public void addToDeleteList(final int recordNumber) {
        deleteList.push(recordNumber);
    }

    /**
     * Put a record in the table. Note that the record is not
     * written to file.
     *
     * @param newRecord the <code>Record</code> to add.
     *        Note that if the new <code>Record</code> is marked as
     *        deleted, the caller should add it to the delete list.
     * @param isReused true if we are reusing a deleted record number
     */
    private void putRecord(final Record newRecord, final boolean isReused) {
        final int recNo = newRecord.getRecordNumber();
        if (isReused) {
            //We are reusing an existing lockable record.

            //Modify the existing lockable record to contain the new record.
            LockableRecord recordAndLock = records.get(recNo);
            if (recordAndLock == null) {
                throw new IllegalStateException("Record not found: " + recNo);
            }
            recordAndLock.setRecord(newRecord);
            LOGGER.finest("Put record " + recNo + " into table (reused)");
        } else {
            //Create a new lockable record and put it in the list
            LockableRecord newFullRecord = new LockableRecord(newRecord);
            records.put(recNo, newFullRecord);
            LOGGER.finest("Put new record " + recNo + " into table");
        }
    }

    /**
     * Write the given <code>Record</code> to file.
     *
     * @param newRecord the <code>Record</code> to write
     * @throws IOException if the write fails
     */
    private void writeToFile(final Record newRecord) throws IOException {
        fileHandler.writeRecord(newRecord);
    }

    /** {@inheritDoc} */
    public void shutDown() {
        fileHandler.shutDown();
    }

}
