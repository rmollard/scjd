package suncertify.server;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.List;

import suncertify.Criterion;
import suncertify.ProgressListener;
import suncertify.RecordModificationPolicy;
import suncertify.RecordNotModifiableException;
import suncertify.ServerToClientConnection;
import suncertify.StaleRecordException;
import suncertify.db.FieldParsers;
import suncertify.db.Record;
import suncertify.db.RecordNotFoundException;
import suncertify.db.TableSchema;
import suncertify.fields.Field;

/**
 * Interface that should be implemented by a controller
 * providing services for URLyBird clients.
 *
 * @author Robert Mollard
 */
public interface RequestServer {

    /**
     * Add a client to the list of connected clients.
     *
     * @param client the client to add
     */
    void addClient(ServerToClientConnection client);

    /**
     * Remove a client from the list of connected clients.
     *
     * @param client the client to remove
     */
    void removeClient(ServerToClientConnection client);

    /**
     * Get the record modification policy used by the server.
     *
     * @return the record modification policy
     */
    RecordModificationPolicy getModificationPolicy();

    /**
     * Get the parsers used to convert fields into strings
     * and vice versa.
     *
     * @return the field parsers
     */
    FieldParsers getFieldParsers();

    /**
     * Get the table schema used by the server.
     *
     * @return the table schema
     */
    TableSchema getSchema();

    /**
     * Get the number of records that match the given criteria.
     *
     * @param client the client requesting the search
     * @param criteria the search criteria
     * @param onlyShowModifiableRecords if true, non-modifiable
     *        records will not be included in the search results
     * @return the number of matching records
     */
    int getMatchingRecordCount(ServerToClientConnection client,
            List<Criterion> criteria, boolean onlyShowModifiableRecords);

    /**
     * Get a list of some of the search results from the previous search.
     * This method may return only some of the matching records, so it
     * may need to be called many times for each search.
     *
     * @param client the client that performed the search
     * @return a list (probably a partial list) of the search results
     *         from the last search performed by the given client
     */
    List<Record> getSearchResults(ServerToClientConnection client);

    /**
     * Modify the record with the given record number.
     *
     * @param client the client requesting the modification
     * @param recordNumber the record number of the record to modify
     * @param fields the new values for the record
     *
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted
     * @throws StaleRecordException if the record has been modified
     *         without the client's knowledge
     * @throws RecordNotModifiableException if the server does not
     *         allow the record to be modified
     */
    void modifyRecord(ServerToClientConnection client, int recordNumber,
            List<Field> fields) throws RecordNotFoundException,
            StaleRecordException, RecordNotModifiableException;

    /**
     * Change the database file to the file with the given filename.
     *
     * @param progressListener listener to be notified of the progress
     *        in reading the contents of the database file
     * @param newName the filename of the new database file
     *
     * @throws FileNotFoundException if the file is not found,
     *         or if the file is not writable
     * @throws ParseException if there is a problem reading the contents
     *         of the new database file
     */
    void setDatabaseFilename(ProgressListener progressListener, String newName)
            throws FileNotFoundException, ParseException;

}
