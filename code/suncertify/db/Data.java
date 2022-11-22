package suncertify.db;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import suncertify.RecordNotLockedException;
import suncertify.StaleRecordException;
import suncertify.fields.Field;

/**
 * Client table access implementation that implements
 * the required <code>DBMain</code> interface.
 *
 * Each client should have its own unique <code>Data</code> instance.
 * The <code>Data</code> instance stores a timestamp
 * for each record that the client
 * has encountered. This lets us detect when a record has
 * been changed by some other client.
 *
 * Each client should only use its own <code>Data</code> instance.
 *
 * This class is thread safe.
 *
 * @author Robert Mollard
 */
public final class Data implements DBMain {

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify.db");

    /**
     * The name of this class, used by the logger.
     */
    private static final String THIS_CLASS = "suncertify.db.Data";

    /**
     * The timestamps of the newest known versions of records.
     * This is used to detect when records have been
     * changed by another client.
     * We map integers (record numbers) to serial numbers (timestamps).
     */
    private final Map<Integer, SerialNumber> recordTimestamps;

    /**
     * A table request manager that we forward requests to.
     */
    private final TableRequestManager requestManager;

    /**
     * Only allow other classes to instantiate through the factory method.
     *
     * @param requestManager the request manager that will be used
     *        to manage all requests.
     */
    private Data(final TableRequestManager requestManager) {
        this.requestManager = requestManager;
        this.recordTimestamps = new HashMap<Integer, SerialNumber>();
    }

    /**
     * Create a new Data instance, giving it the next timestamp available.
     *
     * @param requestManager the table request manager to use
     * @return a new Data instance
     */
    public static Data createData(final TableRequestManager requestManager) {
        return new Data(requestManager);
    }

    /**
     * Update our timestamp for a given record.
     *
     * @param recordNumber the record number
     * @param timestamp the new timestamp for the record
     */
    private void updateTimestamp(final int recordNumber,
            final SerialNumber timestamp) {
        recordTimestamps.put(recordNumber, timestamp);
    }

    /** {@inheritDoc} */
    public synchronized String[] read(final int recNo)
            throws RecordNotFoundException {

        if (recNo < 0) {
            throw new IllegalArgumentException("recNo must not be negative");
        }

        final Record record = requestManager.getRecord(recNo);

        //Update our last recorded timestamp for the record
        updateTimestamp(record.getRecordNumber(), record.getTimestamp());

        //Convert fields into strings and return them
        return requestManager
                .getStringArrayFromFieldList(record.getFieldList());
    }

    /** {@inheritDoc} */
    public synchronized void delete(final int recNo)
            throws RecordNotFoundException {
        LOGGER.entering(THIS_CLASS, "delete");

        if (recNo < 0) {
            throw new IllegalArgumentException("recNo must not be negative");
        }

        try {
            SerialNumber newTimestamp = requestManager.deleteRecord(recNo,
                    this, recordTimestamps.get(recNo));
            updateTimestamp(recNo, newTimestamp);
            LOGGER.fine("Deleted record number " + recNo);
        } catch (StaleRecordException e) {
            throw new StaleTimestampException(recNo, e);
        } catch (IOException e) {
            throw new FileWriteException(e);
        } catch (RecordNotLockedException e) {
            throw new LockException(recNo, e);
        }
    }

    /** {@inheritDoc} */
    public synchronized void update(final int recNo, final String[] data)
            throws RecordNotFoundException {
        LOGGER.entering(THIS_CLASS, "update");

        if (recNo < 0) {
            throw new IllegalArgumentException("recNo must not be negative");
        }

        //Convert array of strings to list of fields
        List<Field> newFields =
            requestManager.getFieldListFromStringArray(data);

        try {
            SerialNumber newTimestamp = requestManager.modifyRecord(
                recNo, this, recordTimestamps.get(recNo), newFields);

            //Update our timestamp
            updateTimestamp(recNo, newTimestamp);
            LOGGER.fine("Updated record number " + recNo);
        } catch (StaleRecordException e) {
            throw new StaleTimestampException(recNo, e);
        } catch (IOException e) {
            throw new FileWriteException(e);
        } catch (RecordNotLockedException e) {
            throw new LockException(recNo, e);
        }
    }

    /** {@inheritDoc} */
    public synchronized int[] find(final String[] criteria) {

        Collection<Record> matches =
            requestManager.getMatchingRecords(criteria);

        //Convert to an array of ints
        final int[] result = new int[matches.size()];
        int index = 0;
        for (Record record : matches) {
            //We don't need to update timestamps
            result[index] = record.getRecordNumber();
            index++;
        }
        return result;
    }

    /** {@inheritDoc} */
    public synchronized int create(final String[] data) {

        //Convert parameter to list of fields
        List<Field> fields = requestManager.getFieldListFromStringArray(data);
        final int result;
        try {
            Record newRecord = requestManager.createRecord(fields);
            updateTimestamp(newRecord.getRecordNumber(),
                    newRecord.getTimestamp());
            result = newRecord.getRecordNumber();
            LOGGER.fine("Created record number " + result);
        } catch (IOException e) {
            throw new FileWriteException(e);
        }
        return result;
    }

    /** {@inheritDoc} */
    public synchronized void lock(final int recNo)
            throws RecordNotFoundException {
        LOGGER.entering(THIS_CLASS, "lock");

        if (recNo < 0) {
            throw new IllegalArgumentException("recNo must not be negative");
        }

        try {
            requestManager.lock(recNo, this, recordTimestamps.get(recNo));
            LOGGER.fine("Locked record number " + recNo);
        } catch (StaleRecordException e) {
            throw new StaleTimestampException(recNo, e);
        }
    }

    /** {@inheritDoc} */
    public synchronized void unlock(final int recNo)
            throws RecordNotFoundException {
        LOGGER.entering(THIS_CLASS, "unlock");

        if (recNo < 0) {
            throw new IllegalArgumentException("recNo must not be negative");
        }

        try {
            requestManager.unlock(recNo, this, recordTimestamps.get(recNo));
            LOGGER.fine("Unlocked record number " + recNo);
        } catch (StaleRecordException e) {
            throw new StaleTimestampException(recNo, e);
        } catch (RecordNotLockedException e) {
            throw new LockException(recNo, e);
        }
    }

    /** {@inheritDoc} */
    public synchronized boolean isLocked(final int recNo)
            throws RecordNotFoundException {

        if (recNo < 0) {
            throw new IllegalArgumentException("recNo must not be negative");
        }

        /*
         * Just return the current lock status. We don't guarantee that
         * the record will remain locked or unlocked.
         */
        return requestManager.isRecordLocked(recNo);
    }

}
