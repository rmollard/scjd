package suncertify.db;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A record and its lock.
 * Note that this class is mutable - the owner field becomes set to
 * the owner of the lock.
 * The record may also be changed to another record.
 *
 * Thread safety: this class is thread safe.
 *
 * @author Robert Mollard
 */
final class LockableRecord {

    /**
     * The <code>Record</code> contained.
     */
    private AtomicReference<Record> recordReference;

    /**
     * The lock that should be used to protect access to
     * the <code>Record</code>.
     */
    private final Lock lock;

    /**
     * The current owner of the lock.
     */
    private AtomicReference<Object> ownerReference;

    /**
     * Create a <code>LockableRecord</code> containing
     * the given <code>Record</code>.
     *
     * @param record the <code>Record</code> to use initially
     */
    LockableRecord(final Record record) {
        recordReference = new AtomicReference<Record>();
        ownerReference = new AtomicReference<Object>();

        this.recordReference.set(record);
        this.lock = new ReentrantLock();
    }

    /**
     * Get the <code>Record</code> contained.
     *
     * @return the Record
     */
    Record getRecord() {
        return recordReference.get();
    }

    /**
     * Set the record to the given record. It is assumed
     * that the caller has the right to do this.
     *
     * @param newRecord the new record
     */
    void setRecord(final Record newRecord) {
        if (ownerReference.get() != null) {
            recordReference.set(newRecord);
        }
    }

    /**
     * Lock the record and set the owner to the new owner.
     * If the lock is not available then
     * the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until the lock has been acquired.
     *
     * @param newOwner the new owner of the lock
     */
    void lock(final Object newOwner) {
        lock.lock();
        ownerReference.set(newOwner);
    }

    /**
     * Unlock the record and reset the owner to null.
     * If the lock is not owned by the given owner, this
     * method does nothing.
     *
     * @param currentOwner the current lock owner
     */
    void unlock(final Object currentOwner) {
        if (ownerReference.get() == currentOwner) {
            ownerReference.set(null);
            lock.unlock();
        }
    }

    /**
     * Get the current lock owner.
     *
     * @return the lock owner
     */
    Object getOwner() {
        return ownerReference.get();
    }

}
