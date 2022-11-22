package suncertify;

import java.io.FileNotFoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import suncertify.db.FieldParsers;
import suncertify.db.Record;
import suncertify.db.RecordNotFoundException;
import suncertify.db.TableSchema;
import suncertify.fields.Field;

/**
 * The methods that clients can invoke on servers.
 *
 * @author Robert Mollard
 */
public interface ClientToServerConnection extends Remote {

    /**
     * Connect to a server. The client is passed as a parameter so that the
     * server has a reference to call the client back with.
     *
     * @param client the client to register with the server
     * @return the server's current time
     * @throws RemoteException if there is a communication problem
     */
    Date connectToServer(ServerToClientConnection client)
            throws RemoteException;

    /**
     * Disconnect a client from a server.
     *
     * @param client the client to disconnect
     * @throws RemoteException if there is a communication problem
     */
    void disconnectFromServer(ServerToClientConnection client)
            throws RemoteException;

    /**
     * Get the database table schema.
     *
     * @return the table schema
     * @throws RemoteException if there is a communication problem
     */
    TableSchema getSchema() throws RemoteException;

    /**
     * Get the modification policy that the server uses to determine if a record
     * is modifiable. This can help the client to decide if a record should be
     * modifiable or not without communicating with the server.
     *
     * If the client decides that a record is modifiable, the server may still
     * veto the modification.
     *
     * @return the record modification policy
     * @throws RemoteException if there is a communication problem
     */
    RecordModificationPolicy getModificationPolicy() throws RemoteException;

    /**
     * Get the parsers that are used to convert fields to strings and vice
     * versa.
     *
     * @return the field parsers
     * @throws RemoteException if there is a communication problem
     */
    FieldParsers getFieldParsers() throws RemoteException;

    /**
     * Perform a search for records matching the specified criteria. This method
     * can be called after connectToServer is called. A null entry in the
     * criteria list will match any value. If criteria is null, all records will
     * be returned.
     *
     * The search results can be obtained with
     * the <code>getSearchResults</code> method.
     *
     * @param client the caller
     * @param criteria the search criteria.
     *        Can be null. If not null, the number of  items in the
     *        list must match the number of fields in the records.
     *        Any item in the list may be
     *        null (which indicates a wildcard on that field).
     * @param onlyShowModifiableRecords if true, only the modifiable
     *        records that match the criteria will be returned.
     *        If false, all records that match the criteria will be returned.
     * @return The number of matching records
     * @throws RemoteException if there is a communication problem
     */
    int search(ServerToClientConnection client, List<Criterion> criteria,
            boolean onlyShowModifiableRecords) throws RemoteException;

    /**
     * Get the next chunk of results of the previous search. The size of the
     * list returned may be smaller than the total number of search results, so
     * this method may need to be called multiple times to get all the search
     * results.
     *
     * @param client the caller
     * @return A list of Records that match the given criteria.
     * @throws RemoteException if there is a communication problem
     */
    List<Record> getSearchResults(ServerToClientConnection client)
            throws RemoteException;

    /**
     * Modify the specified record. This will fail if the record has been
     * modified in the mean time by another client. The server maintains a
     * timestamp for each record for each client.
     *
     * @param client the caller
     * @param recordNumber the record number of the record to modify
     * @param newFields the new fields to write for the record
     *
     * @throws RemoteException if there is a communication problem
     * @throws RecordNotFoundException if the record
     *         number does not correspond to a
     *         record that is not marked as deleted
     * @throws StaleRecordException if the record has been
     *         modified without the client's knowledge
     * @throws RecordNotModifiableException
     *         if the server does not allow the record to be modified
     */
    void modifyRecord(ServerToClientConnection client, int recordNumber,
            List<Field> newFields) throws RemoteException,
            RecordNotFoundException, StaleRecordException,
            RecordNotModifiableException;

    /**
     * Change the server database file.
     * This is currently only used in standalone mode.
     *
     * @param progressListener a listener that will be
     *        informed of the file parsing progress
     * @param newFileName the filename of the new database file to use
     *
     * @throws RemoteException if there is a communication problem
     * @throws FileNotFoundException if the file could not be found
     * @throws ParseException if there was a problem parsing the file
     */
    void setServerFile(final ProgressListener progressListener,
            final String newFileName) throws RemoteException,
            FileNotFoundException, ParseException;

}
