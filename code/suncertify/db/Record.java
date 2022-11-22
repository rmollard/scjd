package suncertify.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import suncertify.fields.Field;

/**
 * This class models the values of a database record at a particular
 * time (indicated by its timestamp).
 *
 * This class is immutable, and uses a
 * defensive <code>readResolve</code>
 * method (see Effective Java by Joshua Bloch, item 57).
 *
 * @author Robert Mollard
 */
public final class Record implements Serializable {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 7407825517598441359L;

    /**
     * Record number, identifies this record.
     * Record numbers of deleted records can be reused.
     */
    private final int recordNumber;

    /**
     * Creation timestamp. Never null.
     */
    private final SerialNumber timestamp;

    /**
     * Flag indicating that this <code>Record</code>
     * is marked as deleted.
     */
    private final boolean isDeleted;

    /**
     * All the field values as an unmodifiable list. Never null.
     */
    private final List<Field> fields;

    /**
     * Construct a new <code>Record</code> with the given values.
     *
     * @param recordNumber the record number to use. Record numbers are
     *        used to identify Records, so they should be unique.
     * @param timestamp the timestamp to use. If null, a new timestamp will
     *        be generated.
     * @param isDeleted true if this record version is marked as deleted
     * @param fields the field values to set. Can be null. We create a copy
     *        of the field list to enforce immutability.
     */
    public Record(final int recordNumber, final SerialNumber timestamp,
            final boolean isDeleted, final List<Field> fields) {

        this.recordNumber = recordNumber;
        this.isDeleted = isDeleted;

        if (timestamp == null) {
            this.timestamp = SerialNumber.createSerialNumber();
        } else {
            this.timestamp = timestamp;
        }

        if (fields == null) {
            this.fields = new ArrayList<Field>();
        } else {
            this.fields = new ArrayList<Field>(fields);
        }
    }

    /**
     * Get the number of fields in the <code>Record</code>.
     *
     * @return number of fields in this <code>Record</code>
     */
    public int getFieldCount() {
        return fields.size();
    }

    /**
     * Get an unmodifiable view of the fields.
     *
     * @return list of fields (never null)
     */
    public List<Field> getFieldList() {
        return Collections.unmodifiableList(fields);
    }

    /**
     * Get the timestamp set when this <code>Record</code> was created.
     *
     * @return The timestamp
     */
    public SerialNumber getTimestamp() {
        return timestamp;
    }

    /**
     * Get the record number.
     *
     * @return The record number
     */
    public int getRecordNumber() {
        return recordNumber;
    }

    /**
     * Determine if this <code>Record</code> is deleted.
     *
     * @return true if marked as deleted
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * Get a string describing the Record.
     * The record number, "deleted" status, timestamp
     * and fields are given. Each field is also given.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder()
            .append("Record {\n")
            .append(" Record Number [" + recordNumber + "]\n")
            .append(" Deleted [" + isDeleted + "]\n")
            .append(" Timestamp [" + timestamp + "]\n")
            .append(" Fields {\n");

        //List all the field values
        for (Field currentField : fields) {
            buff.append("  ") //Add some indenting
                    .append(currentField).append("\n");
        }
        buff.append(" }\n").append("}\n");
        return buff.toString();
    }

    /**
     * Defensive <code>readResolve</code> method to
     * enforce immutability.
     *
     * @return the deserialized <code>Record</code>
     */
    private Object readResolve() {
        return new Record(recordNumber, timestamp, isDeleted, fields);
    }

}
