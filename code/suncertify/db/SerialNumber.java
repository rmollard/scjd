package suncertify.db;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An immutable serial number. Serial numbers are simply assigned
 * in ascending order starting from 0.
 * Serial numbers are often used as timestamps in the URLyBird system.
 * All serial numbers within a virtual machine are unique.
 *
 * @author Robert Mollard
 */
public final class SerialNumber
    implements Comparable<SerialNumber>, Serializable {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 256326670610314791L;

    /**
     * The actual serial number.
     */
    private final long value;

    /**
     * The next serial number.
     */
    private static AtomicLong nextSerial = new AtomicLong(0);

    /**
     * Private constructor to prevent inheritance.
     */
    private SerialNumber() {
        this.value = nextSerial.getAndIncrement();
    }

    /**
     * Factory method to create the next serial number.
     *
     * @return a new <code>SerialNumber</code>
     */
    public static SerialNumber createSerialNumber() {
        return new SerialNumber();
    }

    /**
     * Convenience method to compare two SerialNumbers.
     * If the other <code>SerialNumber</code> is null, returns true.
     *
     * @param other the other <code>SerialNumber</code> (can be null)
     * @return true if this is newer than the
     *             other <code>SerialNumber</code>.
     *         Returns true if <code>other</code> is null.
     */
    public boolean isNewerThan(final SerialNumber other) {
        return other == null || compareTo(other) > 0;
    }

    /**
     * Compare this serial number's value to another
     * serial number's value.
     *
     * @param that the <code>SerialNumber</code> to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     */
    public int compareTo(final SerialNumber that) {
        return Long.valueOf(this.value).compareTo(that.value);
    }

    /**
     * Determine if this object is equal to the given object.
     *
     * @param other the other object to compare to.
     *        If it is not a <code>SerialNumber</code>, then
     *        the objects are not equal.
     * @return true if <code>other</code> is a <code>SerialNumber</code>
     *         and its value is equal to this object's value.
     */
    @Override
    public boolean equals(final Object other) {
        final boolean areEqual;
        if (!(other instanceof SerialNumber)) {
            areEqual = false;
        } else {
            areEqual = (this.compareTo((SerialNumber) other) == 0);
        }
        return areEqual;
    }

    /**
     * Get the hash code value for this object.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return (int) value;
    }

    /**
     * Get a string describing the serial number.
     * The serial number's value is given.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Serial Number: " + value;
    }

}
