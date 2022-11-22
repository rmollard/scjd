package suncertify.db;

/**
 * Main table access interface that is required to be implemented
 * by the <code>Data</code> class.
 * For deadlock avoidance, we rely on clients
 * being considerate and only locking one record at a time.
 *
 * @author Robert Mollard
 */
public interface DBMain {

    /**
     * Reads a record from the file. Returns an array where each
     * element is a record value.
     *
     * No lock is required. The returned array elements will be in the order
     * defined by the table schema.
     *
     * @param recNo the record number. Must not be negative.
     *
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     *
     * @return an array of String values. Each String represents the
     *         String representation of the value of the corresponding
     *         record field. The Strings will be in the same format used
     *         by the field parsers that read and write records to and
     *         from the database file.
     */
    String[] read(int recNo) throws RecordNotFoundException;

    /**
     * Modifies the fields of a record. The new value for field n
     * appears in data[n].
     *
     * The client must have a valid lock for the record.
     * The data elements must be in the order
     * defined by the table schema.
     *
     * The client should invoke unlock() after invoking this method.
     * This method may be invoked multiple times with the same lock.
     *
     * @param recNo the record number.
     * @param data an array of String values representing the changed fields.
     *        Any element may be null, in which case the field will remain
     *        unchanged. The elements must be in the order defined by
     *        the table schema.
     *        Elements should be in the format expected
     *        by the table's parsers.
     *
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     *
     * @throws LockException if the record is not locked by the client
     * @throws StaleTimestampException if the client's lock is out of date
     * @throws FileWriteException if this change could not be successfully
     *         committed to the data file
     * @throws IllegalArgumentException if <code>data</code> is has the wrong
     *         number of fields, or cannot be parsed.
     *         IllegalArgumentException could also be thrown
     *         if the given data would cause the
     *         record to have the same primary key as another record
     *         (This is not the case for the URLyBird implementation
     *         because there is no primary key).
     */
    void update(int recNo, String[] data) throws RecordNotFoundException;

    /**
     * Deletes a record, making the record number and associated disk
     * storage available for reuse.
     *
     * The client must have a valid lock for the record.
     * The client will automatically lose the lock once the record
     * is deleted, therefore the client should not call unlock().
     *
     * @param recNo the record number. Must not be negative.
     *
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     *
     * @throws LockException if the record is not locked by the client
     * @throws StaleTimestampException if the client's lock is out of date
     * @throws FileWriteException if this change could not be successfully
     *                 committed to the data file
     */
    void delete(int recNo) throws RecordNotFoundException;

    /**
     * Returns an array of record numbers that match the specified
     * criteria. Field n in the database file is described by
     * criteria[n]. A null value in criteria[n] matches any field
     * value. A non-null  value in criteria[n] matches any field
     * value that begins with criteria[n]. (For example, "Fred"
     * matches "Fred" or "Freddy".)
     *
     * No lock is required.
     *
     * The records returned will match all the criteria at the time when
     * the request is processed. Note that a record may be modified by
     * another client before "read" is called. The records returned should
     * therefore be treated as a list of <b>possible</b> matches.
     * The caller should call <tt>read</tt> for each number returned to
     * ensure that it really is a match.
     *
     * @param criteria array of match criteria in the format required by
     *        the parser for the corresponding field
     *
     * @return an array of record numbers of possible matches. An empty
     *         array will be returned if there are no matches.
     *
     * @throws RecordNotFoundException reserved for future use.
     *         Not currently thrown by the URLyBird implementation.
     */
    int[] find(String[] criteria) throws RecordNotFoundException;

    /**
     * Creates a new record in the database (possibly reusing a
     * deleted entry). Inserts the given data, and returns the record
     * number of the new record.
     *
     * @param data an array of String values representing the fields.
     *         The elements must be in the order defined by
     *         the table schema. All elements should be non-null.
     *         Elements should be in the format expected
     *         by the table's parsers.
     *
     * @return the record number of the newly created record
     *
     * @throws DuplicateKeyException if the primary key represented by the
     *          data exactly matches an existing record's primary key.
     *          If no primary key is defined, duplicate records can
     *          be created without throwing an exception.
     *
     * @throws FileWriteException if this change could not be successfully
     *                 committed to the data file
     */
    int create(String[] data) throws DuplicateKeyException;

    /**
     * Locks a record so that it can only be updated or deleted by this client.
     * If the specified record is already locked, the current thread gives up
     * the CPU and consumes no CPU cycles until the record is unlocked.
     *
     * If the record is already locked by this client,
     * this method has no effect.
     * Clients may hold multiple locks.
     * Note that the client will automatically lose the lock
     * when the record is deleted.
     *
     * @param recNo the record number. Must not be negative.
     *
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     *
     * @throws StaleTimestampException if the client's copy of the record
     *         is out of date
     */
    void lock(int recNo) throws RecordNotFoundException;

    /**
     * Releases the lock on a record.
     *
     * The client must have a valid lock for the record.
     * Note that locks are also automatically released after the set timeout
     * period.
     *
     * @param recNo the record number. Must not be negative.
     *
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     *
     * @throws LockException if the record is not locked by the client
     * @throws StaleTimestampException if the client's lock is out of date
     */
    void unlock(int recNo) throws RecordNotFoundException;

    /**
     * Determines if a record is currently locked. Returns true if the
     * record is locked, false otherwise.
     *
     * Note that a record's lock status may be changed
     * by another client thread,
     * so a client should not rely on the value returned by this method.
     *
     * @param recNo the record number. Must not be negative.
     *
     * @return true if this record is currently locked by any client
     *
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     */
    boolean isLocked(int recNo) throws RecordNotFoundException;

}
