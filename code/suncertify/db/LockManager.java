package suncertify.db;

/**
 * This class keeps track of which objects are locked.
 * Implementers of this interface must be thread safe.
 * Implementers do not necessarily know anything about the types
 * of objects that are locked.
 *
 * @author Robert Mollard
 */
public interface LockManager {

    /**
     * Lock a record. The current thread will wait
     * until the lock is available.
     *
     * @param recordNumber the record number to lock
     * @param client the lock owner
     * @throws RecordNotFoundException if the record does not exist or
     *         is deleted
     */
    void lock(int recordNumber, Object client)
        throws RecordNotFoundException;

    /**
     * Unlock a record. The lock must be currently
     * owned by the client.
     *
     * @param recordNumber the record number to unlock
     * @param client the lock owner
     * @throws RecordNotFoundException if the record does not exist or
     *         is deleted
     */
    void unlock(int recordNumber, Object client)
        throws RecordNotFoundException;

    /**
     * Get the owner of a given record lock.
     *
     * @param recordNumber the record number
     * @return the owner of the lock (null if not locked)
     * @throws RecordNotFoundException if the record does not exist or
     *         is deleted
     */
    Object getLockOwner(int recordNumber)
        throws RecordNotFoundException;

}
