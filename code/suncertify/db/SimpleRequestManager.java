package suncertify.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import suncertify.RecordNotLockedException;
import suncertify.StaleRecordException;
import suncertify.fields.Field;
import suncertify.fields.parsers.FieldParser;
import suncertify.server.FileHandler;
import suncertify.utils.Localization;

/**
 * Manages concurrent database requests to a table.
 * This class relies on callers being considerate and unlocking records
 * when they are finished with them.
 *
 * Thread safety: this class is thread safe
 *
 * @author Robert Mollard
 */
public final class SimpleRequestManager implements TableRequestManager {

    /**
     * The table we are handling requests for.
     */
    private final Table table;

    /**
     * Lock manager for the table.
     */
    private final LockManager lockManager;

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify.db");

    /**
     * The name of this class, used by the logger.
     */
    private static final String THIS_CLASS =
        "suncertify.db.SimpleRequestManager";

    /**
     * Create a new instance. This will create a new table, and
     * populate it with the given records. A lock will be
     * created for each record.
     *
     * @param records the existing Records in the table
     * @param schema the table schema
     * @param fieldParsers the parsers for the table's fields
     * @param fileHandler the file handler to use
     *        when writing records to file
     */
    public SimpleRequestManager(final List<Record> records,
            final TableSchema schema, final FieldParsers fieldParsers,
            final FileHandler fileHandler) {

        CombinedTable tableAndLocks =
            new CombinedTable(schema, fieldParsers, fileHandler);

        this.table = tableAndLocks;
        this.lockManager = tableAndLocks;

        //Populate table with the existing records
        for (Record r : records) {
            table.addExistingRecord(r);

            if (r.isDeleted()) {
                table.addToDeleteList(r.getRecordNumber());
            }
        }
    }

    /** {@inheritDoc} */
    public TableSchema getSchema() {
        return table.getSchema();
    }

    /** {@inheritDoc} */
    public FieldParsers getFieldParsers() {
        return table.getFieldParsers();
    }

    /** {@inheritDoc} */
    public List<Field> getFieldListFromStringArray(
            final String[] fieldsAsStrings) {

        List<Field> result = new ArrayList<Field>();

        for (int i = 0; i < fieldsAsStrings.length; i++) {

            if (fieldsAsStrings[i] == null) {
                result.add(null);
            } else {
                //Get the parser for this Field
                FieldParser currentFieldParser = getParserForFieldIndex(i);
                //Use the parser to convert the Field into a String
                result.add(currentFieldParser.valueOf(fieldsAsStrings[i]));
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    public String[] getStringArrayFromFieldList(final List<Field> fields) {

        final List<String> fieldsAsStrings = new ArrayList<String>();

        for (int i = 0; fields != null && i < fields.size(); i++) {
            if (fields.get(i) == null) {
                fieldsAsStrings.add(null);
            } else {
                //Get the parser for this field
                FieldParser currentFieldParser = getParserForFieldIndex(i);

                //Use the parser to convert the Field into a String
                String valueString =
                    currentFieldParser.getString(fields.get(i));

                fieldsAsStrings.add(valueString);
            }
        }

        //Convert to String array
        final String[] result = new String[fieldsAsStrings.size()];
        for (int i = 0; i < fieldsAsStrings.size(); i++) {
            result[i] = fieldsAsStrings.get(i);
        }

        return result;
    }

    /** {@inheritDoc} */
    public boolean isRecordLocked(final int recordNumber)
            throws RecordNotFoundException {

        //First make sure the record exists and is not "deleted"
        getRecord(recordNumber, true);

        //Assume the lock exists if the record exists
        return lockManager.getLockOwner(recordNumber) != null;
    }

    /**
     * Determine if a Record matches some given criteria.
     * A record matches only if it matches all elements of the
     * criteria array and it it not marked as deleted.
     *
     * @param currentRecord the Record to check
     * @param criteria array of Strings where each
     * @return true if currentRecord matches criteria
     */
    private boolean isMatchingRecord(final Record currentRecord,
            final String[] criteria) {
        //Assume the record matches     until proven otherwise
        boolean haveMatch = true;

        if (currentRecord.isDeleted()) {
            haveMatch = false; //Don't match deleted records
        }

        //Check each field against its corresponding criterion
        for (int fieldIndex = 0;
            fieldIndex < criteria.length && haveMatch; fieldIndex++) {

            String criterion = criteria[fieldIndex];
            if (criterion != null) {
                Field thisField = currentRecord.getFieldList().get(fieldIndex);

                Class<? extends Field> currentFieldClass =
                    getSchema().getFieldClassByIndex(fieldIndex);
                FieldParser currentFieldParser =
                    getFieldParsers().getParserForClass(currentFieldClass);

                String fieldValueString =
                    currentFieldParser.getString(thisField);

                final String thisFieldAsString =
                    fieldValueString.toLowerCase(Localization.getLocale());

                //Check for mismatch
                if (!thisFieldAsString.startsWith(criteria[fieldIndex]
                        .toLowerCase(Localization.getLocale()))) {
                    haveMatch = false;
                }
            }
        }
        return haveMatch;
    }

    /** {@inheritDoc} */
    public Collection<Record> getMatchingRecords(final String[] criteria) {

        Collection<Record> matches = new ArrayList<Record>();
        Collection<LockableRecord> records = table.getAllRecords();

        //Do a linear search of all the records
        for (LockableRecord currentLocableRecord : records) {
            final Record currentRecord = currentLocableRecord.getRecord();

            if (criteria.length != currentRecord.getFieldCount()) {
                throw new IllegalArgumentException("Expected "
                        + currentRecord.getFieldCount() + " criteria, got "
                        + criteria.length);
            }

            if (isMatchingRecord(currentRecord, criteria)) {
                matches.add(currentRecord);
            }
        }
        return matches;
    }

    /**
     * Convenience method to get the parser for a particular field number.
     *
     * @param fieldIndex the index of the field
     * @return the parser for the field at the given index
     */
    private FieldParser getParserForFieldIndex(final int fieldIndex) {

        Class<? extends Field> currentFieldClass =
            getSchema().getFieldClassByIndex(fieldIndex);

        FieldParser currentFieldParser =
            getFieldParsers().getParserForClass(currentFieldClass);

        return currentFieldParser;
    }

    /** {@inheritDoc} */
    public void lock(final int recordNumber, final DBMain client,
            final SerialNumber lastKnownTimestamp)
            throws RecordNotFoundException, StaleRecordException {

        LOGGER.entering(THIS_CLASS, "lock");

        //First ensure the Record exists and is not deleted
        getRecordForRead(recordNumber, lastKnownTimestamp);

        /*
         * Now get the lock on the Record. This method blocks until
         * the record is available.
         * We rely on the server-side code being well behaved
         * in order to avoid waiting forever.
         */
        lockManager.lock(recordNumber, client);

        try {
            /*
             * The record may well have changed while
             * we were waiting, so read it again.
             */
            getRecordForRead(recordNumber, lastKnownTimestamp);
        } catch (RecordNotFoundException e) {
            //Record was deleted while we were waiting for the lock
            lockManager.unlock(recordNumber, client);
            throw e;
        } catch (StaleRecordException e) {
            //Record was modified while we were waiting for the lock
            lockManager.unlock(recordNumber, client);
            throw e;
        }
        LOGGER.exiting(THIS_CLASS, "lock");
    }

    /** {@inheritDoc} */
    public void unlock(final int recordNumber, final DBMain client,
            final SerialNumber lastKnownTimestamp)
            throws RecordNotFoundException, StaleRecordException,
            RecordNotLockedException {

        LOGGER.entering(THIS_CLASS, "unlock");

        unlock(recordNumber, client, lastKnownTimestamp, false);

        LOGGER.exiting(THIS_CLASS, "unlock");
    }

    /**
     * Unlock the record with the given record number.
     * The record must exist, but
     * a record that is marked as "deleted" can still be unlocked.
     *
     * The record must be locked by the client.
     *
     * @param recordNumber the record number
     * @param client the client requesting the lock
     * @param lastKnownTimestamp what the Record's timestamp was when the
     *        client last read the Record
     * @param allowDeleted true if the Record
     *        is allowed to be marked as deleted.
     *        If false, RecordNotFoundException will be thrown if the
     *        Record is marked as deleted.
     *
     * @throws RecordNotFoundException if the record is missing, or
     *         if allowDeleted is false and the Record is marked as deleted.
     * @throws StaleRecordException if lastKnownTimestamp is older than
     *         the Record's timestamp
     * @throws RecordNotLockedException if the
     *         Record is not locked by the client
     */
    private void unlock(final int recordNumber, final DBMain client,
            final SerialNumber lastKnownTimestamp, final boolean allowDeleted)
            throws RecordNotFoundException, StaleRecordException,
            RecordNotLockedException {

        //Check that Record exists and is locked by us
        getRecordForWrite(recordNumber, client, lastKnownTimestamp,
                allowDeleted);

        lockManager.unlock(recordNumber, client);
    }

    /**
     * Get the specified <code>Record</code>.
     *
     * @param recordNumber the record number
     * @param allowDeleted true if the <code>Record</code>
     *        is allowed to be marked as deleted.
     *        If false, RecordNotFoundException will be thrown if the
     *        <code>Record</code> is marked as deleted.
     *
     * @return the <code>Record</code> (guaranteed not to be null)
     *
     * @throws RecordNotFoundException if the <code>Record</code> is missing,
     *         or if allowDeleted
     *         is false and the <code>Record</code> is marked as deleted.
     */
    private Record getRecord(final int recordNumber,
            final boolean allowDeleted) throws RecordNotFoundException {

        Record record = table.getRecord(recordNumber);
        if (record == null) {
            throw new RecordNotFoundException(recordNumber,
                    "Record does not exist");
        }

        if (record.isDeleted() && !allowDeleted) {
            throw new RecordNotFoundException(recordNumber,
                    "Record is marked as deleted");
        }
        return record;
    }

    /** {@inheritDoc} */
    public Record getRecord(final int recordNumber)
            throws RecordNotFoundException {
        return getRecord(recordNumber, false);
    }

    /**
     * Get a <code>Record</code> for reading,
     * checking that the <code>Record</code> exists,
     * is not deleted, and the timestamp is up to date.
     *
     * @param recordNumber the record number
     * @param lastKnownTimestamp what the <code>Record</code>'s
     *        timestamp was when the
     *        client last read the <code>Record</code>
     * @return the <code>Record</code> with the given record number
     *
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a <code>Record</code>
     *         that is not marked as deleted
     * @throws StaleRecordException if lastKnownTimestamp is older than
     *         the <code>Record</code>'s timestamp
     */
    private Record getRecordForRead(final int recordNumber,
            final SerialNumber lastKnownTimestamp)
            throws RecordNotFoundException, StaleRecordException {

        Record record = getRecord(recordNumber);

        //Check timestamp
        if (record.getTimestamp().isNewerThan(lastKnownTimestamp)) {
            throw new StaleRecordException(recordNumber);
        }
        return record;
    }

    /**
     * Get a <code>Record</code> and ensure we have
     * write access (i.e. we have locked the record).
     *
     * We check
     * <ul>
     * <li>the record exists</li>
     * <li>the record has not been marked as deleted</li>
     * <li>the client owns the lock for this record</li>
     * <li>the client's timestamp for this record is up to date</li>
     * </ul>
     *
     * @param recordNumber the record number
     * @param client the client requesting the lock
     * @param lastKnownTimestamp what the record's timestamp was when the
     *        client last read the record
     * @param allowDeleted if true, a record marked as "deleted" will
     *        not cause RecordNotFoundException to be thrown.
     *
     * @return the record with the requested recordNumber
     *
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     * @throws RecordNotLockedException if the
     *         Record is not locked by the client
     * @throws StaleRecordException if lastKnownTimestamp is older than
     *         the record's timestamp
     */
    private Record getRecordForWrite(final int recordNumber,
            final DBMain client, final SerialNumber lastKnownTimestamp,
            final boolean allowDeleted) throws RecordNotFoundException,
            RecordNotLockedException, StaleRecordException {

        Record record = getRecord(recordNumber, allowDeleted);

        //Check that the lock is held by this client
        Object lockOwner = lockManager.getLockOwner(recordNumber);

        if (client != lockOwner) {
            throw new RecordNotLockedException(recordNumber);
        }

        /*
         * Check record has not been modified by someone else since the
         * client last read it
         */
        if (record.getTimestamp().isNewerThan(lastKnownTimestamp)) {
            throw new StaleRecordException(recordNumber);
        }
        return record;
    }

    /** {@inheritDoc} */
    public Record createRecord(final List<Field> fields) throws IOException {
        return table.createRecord(fields);
    }

    /** {@inheritDoc} */
    public SerialNumber deleteRecord(final int recordNumber,
            final DBMain client, final SerialNumber lastKnownTimestamp)
            throws RecordNotFoundException, RecordNotLockedException,
            StaleRecordException, IOException {

        LOGGER.entering(THIS_CLASS, "deleteRecord");

        //Mark the Record as deleted
        SerialNumber newTimestamp = modifyRecord(recordNumber, client,
                lastKnownTimestamp, true, null);

        //Unlock the record
        unlock(recordNumber, client, newTimestamp, true);

        //The lock for this record can safely be reused now.
        table.addToDeleteList(recordNumber);

        LOGGER.exiting(THIS_CLASS, "deleteRecord");

        return newTimestamp;
    }

    /** {@inheritDoc} */
    public SerialNumber modifyRecord(final int recordNumber,
            final DBMain client, final SerialNumber lastKnownTimestamp,
            final List<Field> newFields) throws RecordNotFoundException,
            RecordNotLockedException, StaleRecordException, IOException {

        return modifyRecord(recordNumber, client, lastKnownTimestamp, false,
                newFields);
    }

    /**
     * Modify an existing non-deleted record.
     *
     * @param recordNumber the record number
     * @param client the client requesting the modification
     * @param delete if true, the record will be marked as "deleted"
     * @param lastKnownTimestamp what the Record's timestamp was when the
     *        client last read the Record
     * @param newFields can be null. If null, the record's fields will
     *        remain unchanged.
     *        If not null, it must have the right number of items.
     *
     * @return the updated serial number of the record
     *
     * @throws IllegalArgumentException if newFields is not null
     *         and it contains the wrong number of items
     *
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     * @throws RecordNotLockedException if the
     *         Record is not locked by the client
     * @throws StaleRecordException if lastKnownTimestamp is older than
     *         the record's timestamp
     * @throws IOException if there is a problem storing the new
     *         version of the Record
     */
    private SerialNumber modifyRecord(final int recordNumber,
            final DBMain client, final SerialNumber lastKnownTimestamp,
            final boolean delete, final List<Field> newFields)
            throws RecordNotFoundException, RecordNotLockedException,
            StaleRecordException, IOException {

        Record oldRecord = getRecordForWrite(recordNumber, client,
                lastKnownTimestamp, false);

        List<Field> oldFields = oldRecord.getFieldList();

        //The fields to write for the new Record
        final List<Field> fieldsToWrite =
            getFieldsToWrite(oldFields, newFields);
        Record newRecordVersion = new Record(recordNumber, null, delete,
                fieldsToWrite);

        //Add the Record to the table
        table.addModifiedRecord(newRecordVersion);

        return newRecordVersion.getTimestamp();
    }

    /**
     * Combine the two given lists of Fields into one,
     * taking the entry from newFields whenever possible.
     *
     * If the entry for newFields is null, the corresponding entry
     * in oldFields will be used instead.
     * If newFields is null, oldFields will be returned.
     * If newFields is not null,
     * it must have the same number of items as oldFields.
     *
     * @param oldFields must not be null
     * @param newFields can be null
     * @return the newFields list with any blanks filled in by corresponding
     *         entries in oldFields.
     */
    private List<Field> getFieldsToWrite(final List<Field> oldFields,
            final List<Field> newFields) {

        //The fields for the new record
        final List<Field> fieldsToWrite;

        if (newFields != null && oldFields.size() != newFields.size()) {
            throw new IllegalArgumentException("Expected " + oldFields.size()
                    + " fields, was given " + newFields.size());
        }

        if (newFields == null) {
            fieldsToWrite = oldFields;
        } else {
            fieldsToWrite = new ArrayList<Field>();

            //Use new field if available. Otherwise use old field.
            for (int i = 0; i < newFields.size(); i++) {
                Field newField = newFields.get(i);

                if (newField != null) {
                    fieldsToWrite.add(newField); //Use new field
                } else {
                    fieldsToWrite.add(oldFields.get(i)); //Use old field
                }
            }
        }
        return fieldsToWrite;
    }

    /** {@inheritDoc} */
    public void shutDown() {
        table.shutDown();
    }

}
