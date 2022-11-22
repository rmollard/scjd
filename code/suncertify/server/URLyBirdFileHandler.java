package suncertify.server;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import suncertify.ProgressListener;
import suncertify.db.FieldParsers;
import suncertify.db.Record;
import suncertify.db.SerialNumber;
import suncertify.db.TableSchema;
import suncertify.db.URLyBirdFieldParsers;
import suncertify.db.URLyBirdSchema;
import suncertify.fields.Field;
import suncertify.fields.parsers.FieldParser;

/**
 * This class contains file-format dependent parsing methods
 * for reading and writing URLyBird 1.3.2 database files.
 *
 * This class is thread safe.
 *
 * @author Robert Mollard
 */
public final class URLyBirdFileHandler implements FileHandler {

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify.server");

    /**
     * The name of this class, used by the logger.
     */
    private static final String THIS_CLASS =
        "suncertify.server.URLyBirdFileHandler";

    /**
     * Lock to ensure mutual exclusion of writers.
     */
    private ReadWriteLock fileLock = new ReentrantReadWriteLock();

    /**
     * Magic cookie value. Identifies the file as a data file.
     */
    private static final int MAGIC = 0x102;

    /**
     * Length (in bytes) of the magic cookie value.
     */
    private static final int MAGIC_LENGTH = 4;

    /**
     * Length (in bytes) of the "offset" tag that gives the offset to
     * the start of the first record in the file.
     */
    private static final int OFFSET_LENGTH = 4;

    /**
     * Length (in bytes) of the tag that gives the
     * number of fields in each record.
     */
    private static final int FIELDS_PER_RECORD_LENGTH = 2;

    /**
     * Length (in bytes) of the tag that gives the
     * length of each field name.
     */
    private static final int FIELD_NAME_LENGTH = 2;

    /**
     * Length (in bytes) of the tag that gives the
     * length of the field.
     */
    private static final int FIELD_LENGTH_LENGTH = 2;

    /**
     * Length (in bytes) of the "deleted" flag for each record.
     */
    private static final int RECORD_FLAG_LENGTH = 2;

    /**
     * Flag indicating that the record is not deleted.
     * Note that this flag needs to be written as 2 bytes.
     */
    private static final short VALID_FLAG = 0x00;

    /**
     * Flag indicating that the record is deleted.
     * Note that this flag needs to be written as 2 bytes.
     */
    private static final short DELETED_FLAG = (short) 0x8000;

    /**
     * The character (null) used for padding records that are too short.
     */
    private static final char PADDING_CHARACTER = '\0';

    /**
     * Indicates that a variable has not been initialized.
     */
    private static final int UNSET = -1;

    /**
     * The singleton instance of this class.
     */
    private static final URLyBirdFileHandler INSTANCE =
        new URLyBirdFileHandler();

    /**
     * The distance from the start of the
     * file to the first record (in bytes).
     */
    private int offsetToFirstRecord = UNSET;

    /**
     * The length of each record (including the "deleted" flag), in bytes.
     * Each record has the same length,
     * short records are padded with spaces.
     */
    private int recordLength;

    /**
     * The file containing the records.
     */
    private RandomAccessFile theFile = null;

    /**
     * The names of the fields contained in the file.
     */
    private List<String> fieldNames = null;

    /**
     * The length (in bytes) of each field contained in the file.
     */
    private List<Integer> fieldLengths = null;

    /**
     * The progress listeners that have been added with
     * the <code>addProgressListener</code> method.
     */
    private final Set<ProgressListener> listeners;

    /**
     * The schema of the table contained in the file. We use it
     * to find out the class of each field.
     */
    private TableSchema schema;

    /**
     * The parser for each field in the table.
     */
    private final FieldParsers parsers;

    /**
     * Gets set to true when the file handler is shut down.
     */
    private volatile boolean isShutDown = false;

    /**
     * Get the singleton instance of this class.
     *
     * @return the singleton instance
     */
    public static URLyBirdFileHandler getInstance() {
        return INSTANCE;
    }

    /**
     * Private constructor to enforce singleton rule.
     */
    private URLyBirdFileHandler() {
        parsers = URLyBirdFieldParsers.getInstance();
        listeners = new HashSet<ProgressListener>();
    }

    /** {@inheritDoc} */
    public void openFile(final File newFile) throws FileNotFoundException {
        fieldLengths = new ArrayList<Integer>();
        fieldNames = new ArrayList<String>();

        LOGGER.fine("Opening file: " + newFile.toString());

        fileLock.writeLock().lock();
        try {

            try {
                if (theFile != null) {
                    theFile.close();
                }
            } catch (IOException e) {
                LOGGER.warning("Could not close file: " + theFile);
            }
            //Use rws mode to reduce the chance of losing data
            theFile = new RandomAccessFile(newFile, "rws");
        } finally {
            fileLock.writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public List<Record> getAllRecords() throws IOException, ParseException {

        LOGGER.entering(THIS_CLASS, "getAllRecords");

        if (theFile == null) {
            throw new IllegalStateException(
                    "Must call open() before calling getAllRecords()");
        }
        List<Record> result = new ArrayList<Record>();

        fileLock.readLock().lock();

        try {
            //Ensure we are at the start of the file
            seek(0);

            final int magic = getNumber(theFile, MAGIC_LENGTH);

            if (magic != MAGIC) {
                throw new ParseException("Expected magic number " + MAGIC
                        + " but got " + magic, 0);
            }

            final int offset = getNumber(theFile, OFFSET_LENGTH);
            offsetToFirstRecord = offset;
            final int fieldsPerRecord = getNumber(theFile,
                    FIELDS_PER_RECORD_LENGTH);
            recordLength = RECORD_FLAG_LENGTH;

            //Now read each field description
            for (int field = 0; field < fieldsPerRecord; field++) {
                final int nameLength = getNumber(theFile, FIELD_NAME_LENGTH);

                byte[] fieldName = new byte[nameLength];
                theFile.readFully(fieldName);
                String currentFieldName = new String(fieldName);

                final int currentFieldLength = getNumber(theFile,
                        FIELD_LENGTH_LENGTH);

                fieldNames.add(currentFieldName);
                fieldLengths.add(currentFieldLength);
                recordLength += currentFieldLength;
            }

            //Go to the start of the record data section
            seek(offset);

            //Calculate the number of records in the file
            final long recordsInFile =
                (theFile.length() - offset) / recordLength;

            for (ProgressListener listener : listeners) {
                listener.setMaxValue((int) recordsInFile);
            }

            int recordCount = 0; //The number of records read

            //We have enough information to create the schema now
            schema = new URLyBirdSchema(fieldNames, fieldLengths);

            //Now read the records. The field lengths are now known.
            for (int record = 0; theFile.getFilePointer() < theFile.length()
                    && !isCancelled(); record++) {
                final int flag = getNumber(theFile, RECORD_FLAG_LENGTH);

                boolean isDeleted = false;
                if (flag == VALID_FLAG) {
                    isDeleted = false;
                } else if (flag == DELETED_FLAG) {
                    isDeleted = true;
                } else {
                    throw new ParseException(
                            "Unknown value seen for record validity flag: "
                                    + flag, (int) theFile.getFilePointer());
                }

                //The fields in the current record
                List<Field> thisRecordsFields = new ArrayList<Field>();

                //Read each field in the current record. We know the lengths.
                for (int field = 0; field < fieldLengths.size(); field++) {

                    byte[] fieldText = new byte[fieldLengths.get(field)];
                    theFile.readFully(fieldText);
                    String currentField = new String(fieldText);

                    String currentFieldName = fieldNames.get(field);

                    Class<? extends Field> currentFieldClass =
                        schema.getFieldClassByName(currentFieldName);

                    FieldParser currentFieldParser = parsers
                            .getParserForClass(currentFieldClass);

                    String trimmed = currentField.trim();
                    Field thisField = currentFieldParser.valueOf(trimmed);

                    thisRecordsFields.add(thisField);
                }

                recordCount++;

                for (ProgressListener listener : listeners) {
                    listener.setCurrentValue(recordCount);
                }

                Record currentRecord = new Record(
                        record, SerialNumber.createSerialNumber(),
                        isDeleted, thisRecordsFields);

                result.add(currentRecord);
            }

        } catch (EOFException e) {
            //End of file reached prematurely
            throw new ParseException(e.getMessage(),
                    (int) theFile.getFilePointer());
        } catch (NumberFormatException e) {
            //Could not parse some schema data
            throw new ParseException(e.getMessage(),
                    (int) theFile.getFilePointer());
        } catch (IllegalArgumentException e) {
            //Could not parse a particular field
            throw new ParseException(e.getMessage(),
                    (int) theFile.getFilePointer());
        } finally {
            fileLock.readLock().unlock();
        }

        return result;
    }

    /** {@inheritDoc} */
    public void shutDown() {

        fileLock.writeLock().lock();
        try {
            isShutDown = true;
            if (theFile != null) {
                theFile.close();
            }
        } catch (IOException e) {
            LOGGER.severe("IO exception when shutting down: " + e);
        } finally {
            fileLock.writeLock().unlock();
        }
    }

    /**
     * Write a record with the specified list of <code>Field</code>
     * values to the database file.
     * This may overwrite an existing record if the
     * existing <code>Record</code> is marked as "deleted".
     *
     * Note that this method might cause the size of
     * the database file to increase.
     *
     * @param record the new <code>Record</code> to write
     * @throws IOException if the <code>Record</code> could not be written
     * @throws IllegalStateException if <code>getAllRecords</code> has
     *         not already been called, or if <code>openFile</code> has
     *         not already been called.
     */
    public void writeRecord(final Record record) throws IOException {
        LOGGER.entering(THIS_CLASS, "writeRecord");

        if (theFile == null) {
            throw new IllegalStateException(
                    "Must call openFile() before calling writeRecord()");
        }

        if (offsetToFirstRecord == UNSET) {
            throw new IllegalStateException(
                    "Must call getAllRecords() before calling writeRecord()");
        }

        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        final DataOutputStream output = new DataOutputStream(byteStream);
        final List<Field> fieldsToWrite = record.getFieldList();

        if (fieldsToWrite.size() != fieldLengths.size()) {
            throw new IllegalArgumentException("Record has "
                    + fieldsToWrite.size() + " fields, expected "
                    + fieldLengths.size());
        }

        //Write the "deleted" flag
        if (record.isDeleted()) {
            output.writeShort(DELETED_FLAG);
        } else {
            output.writeShort(VALID_FLAG);
        }

        //Write each field in the record
        for (int i = 0; i < fieldsToWrite.size(); i++) {
            Field f = fieldsToWrite.get(i);
            int thisFieldLength = fieldLengths.get(i);

            //Write the new value by using the appropriate parser.
            FieldParser parser = parsers.getParserForClass(f.getClass());

            String value = parser.getString(f);
            String stringToWrite = value;
            //Trim the value if it is too long
            if (stringToWrite.length() > thisFieldLength) {
                String trimmed = stringToWrite.substring(0, thisFieldLength);
                LOGGER.fine("Trimmed " + stringToWrite + " to " + trimmed);
                stringToWrite = trimmed;
            }
            output.writeBytes(stringToWrite); //Write in ASCII format

            //Pad the remaining space in the field
            for (int j = stringToWrite.length(); j < thisFieldLength; j++) {
                output.writeByte(PADDING_CHARACTER); //Write as ASCII
            }
        }
        //Calculate the record's start position in the file
        final long startPosition = offsetToFirstRecord
                + record.getRecordNumber() * recordLength;

        fileLock.writeLock().lock();
        try {
            if (!isShutDown) {
                seek(startPosition);

                theFile.write(byteStream.toByteArray());
                LOGGER.fine("Wrote record " + record.getRecordNumber()
                        + " successfully");
            }
        } finally {
            fileLock.writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public void addProgressListener(
            final ProgressListener progressListener) {
        fileLock.readLock().lock();
        try {
            listeners.add(progressListener);
        } finally {
            fileLock.readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public void removeProgressListener(
            final ProgressListener progressListener) {
        fileLock.readLock().lock();
        try {
            listeners.remove(progressListener);
        } finally {
            fileLock.readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public TableSchema getSchema() {
        return schema;
    }

    /**
     * Seek to the given offset if not already there.
     *
     * @param offset the position to seek to, measured in
     *        bytes from the start of the file
     * @throws IOException if offset is less than 0 or an I/O error occurs
     */
    private void seek(final long offset) throws IOException {
        if (theFile == null) {
            throw new IllegalStateException(
                    "Must call open() before calling seek()");
        }

        if (theFile.getFilePointer() != offset) {
            theFile.seek(offset);
        }
    }

    /**
     * Check if any progress listener has cancelled the process of
     * reading from the file.
     *
     * @return true if cancelled by any listener
     */
    private boolean isCancelled() {
        boolean cancelled = false;
        for (Iterator<ProgressListener> i = listeners.iterator();
                i.hasNext() && !cancelled;) {
            ProgressListener listener = i.next();
            if (listener.isCancelled()) {
                cancelled = true;
            }
        }
        return cancelled;
    }

    /**
     * Read an int from the given data input by reading the
     * given number of bytes.
     *
     * @param input the input interface to read from
     * @param howManyBytes how many bytes to read. Must be 1, 2 or 4.
     * @return an int parsed by reading the given number of bytes from
     *         the input
     * @throws IOException if there is an I/O problem
     * @throws EOFException if we reach the end of <code>input</code>
     *         before reading required number of bytes
     * @throws IllegalArgumentException if <code>howManyBytes</code>
     *         is not 1, 2 or 4
     */
    private int getNumber(final DataInput input, final int howManyBytes)
        throws IOException {
        final int result;

        if (howManyBytes == 1) {
            result = input.readByte();
        } else if (howManyBytes == 2) {
            result = input.readShort();
        } else if (howManyBytes == 4) {
            result = input.readInt();
        } else {
            throw new IllegalArgumentException("Can't parse " + howManyBytes
                    + " bytes");
        }
        return result;
    }

}
