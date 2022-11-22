package suncertify.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import suncertify.ProgressListener;
import suncertify.db.Record;
import suncertify.db.TableSchema;

/**
 * Database file parser interface.
 * Implementing classes must be thread safe.
 *
 * This could be enhanced by adding a method to create a new file.
 *
 * @author Robert Mollard
 */
public interface FileHandler {

    /**
     * Open the given file. The file must be writable
     * or <code>FileNotFoundException</code> will be thrown.
     * If a file is already open, this method will close it first
     * before opening the specified file.
     *
     * @param newFile the <code>File</code> to open
     * @throws FileNotFoundException if the <code>File</code> is not found,
     *         or if the <code>File</code> is not writable
     */
    void openFile(File newFile) throws FileNotFoundException;

    /**
     * Get all the records from the file. This may be interrupted
     * by any <code>ProgressListener</code> that was added with the
     * <code>addProgressListener</code> method.
     *
     * @return all the records in the file
     *
     * @throws ParseException if the data in the file is not
     *         in the expected format
     * @throws IOException if an I/O problem occurs
     * @throws IllegalStateException if <code>openFile</code> has
     *         not already been called
     */
    List<Record> getAllRecords() throws ParseException, IOException;

    /**
     * Add a <code>ProgressListener</code> to listen to the progress of the
     * getAllRecords method. Does nothing if the given
     * <code>ProgressListener</code> is already in the listener list.
     * Any <code>ProgressListener</code> added with this method may cancel
     * a running <code>getAllRecords</code> invocation.
     *
     * @param progressListener the <code>ProgressListener</code> to add
     */
    void addProgressListener(ProgressListener progressListener);

    /**
     * Removes the given <code>ProgressListener</code>.
     *
     * @param progressListener the <code>ProgressListener</code> to remove
     */
    void removeProgressListener(ProgressListener progressListener);

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
     */
    void writeRecord(Record record) throws IOException;

    /**
     * Perform a graceful shutdown. This will involve closing the file
     * if it is open.
     */
    void shutDown();

    /**
     * Get the database schema. This will not be known until
     * <code>getAllRecords</code> is called.
     *
     * @return the database schema
     */
    TableSchema getSchema();

}
