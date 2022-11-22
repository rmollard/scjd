package suncertify.server;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import suncertify.Criterion;
import suncertify.ProgressListener;
import suncertify.RecordModificationPolicy;
import suncertify.RecordNotModifiableException;
import suncertify.ServerToClientConnection;
import suncertify.StaleRecordException;
import suncertify.URLyBirdRecordModificationPolicy;
import suncertify.db.DBMain;
import suncertify.db.Data;
import suncertify.db.FieldParsers;
import suncertify.db.FileWriteException;
import suncertify.db.LockException;
import suncertify.db.Record;
import suncertify.db.RecordNotFoundException;
import suncertify.db.SerialNumber;
import suncertify.db.SimpleRequestManager;
import suncertify.db.StaleTimestampException;
import suncertify.db.TableRequestManager;
import suncertify.db.TableSchema;
import suncertify.db.URLyBirdFieldParsers;
import suncertify.fields.Field;
import suncertify.fields.parsers.FieldParser;

/**
 * MVC model for the URLyBird server.
 *
 * Thread safety: this class is thread safe.
 *
 * @author Robert Mollard
 */
final class ServerModel {

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify.server");

    /**
     * The name of this class, used by the logger.
     */
    private static final String THIS_CLASS = "suncertify.server.ServerModel";

    /**
     * The number of search results to return for each client
     * request (50).
     */
    private static final int RESULTS_CHUNK_SIZE = 50;

    /**
     * Property name to use when a client has been added.
     */
    public static final String CLIENT_ADDED = "clientAddedProperty";

    /**
     * Property name to use when a client has been removed.
     */
    public static final String CLIENT_REMOVED = "clientRemovedProperty";

    /**
     * Property name to use when the server has been started.
     */
    public static final String SERVER_STARTED = "serverStartedProperty";

    /**
     * Property name to use when the server has been stopped.
     */
    public static final String SERVER_STOPPED = "serverStoppedProperty";

    /**
     * A reference to a parser that understands the database file format.
     */
    private final FileHandler fileParser;

    /**
     * Parsers used to parse strings into field values and vice versa.
     */
    private final FieldParsers fieldParsers;

    /**
     * The currently connected clients.
     */
    private final
        ConcurrentMap<ServerToClientConnection, DBMain> clients;

    /**
     * The search results of the previous search request.
     * Each client can only perform one search at a time.
     */
    private final
        ConcurrentMap<ServerToClientConnection, List<Record>> searchResults;

    /**
     * Where each client is up to in their search.
     * The integer is how
     * many records the client has gotten to date
     */
    private final
        ConcurrentMap<ServerToClientConnection, Integer> searchProgress;

    /**
     * Property change support for firing events to listeners.
     */
    private PropertyChangeSupport changeSupport;

    /**
     * Request manager to handle requests to the database table.
     */
    private TableRequestManager requestManager = null;

    /**
     * The modification policy that the server uses to determine
     * if a record can be modified by a client.
     */
    private RecordModificationPolicy policy = null;

    /**
     * Construct a new instance.
     */
    ServerModel() {
        clients =
            new ConcurrentHashMap<ServerToClientConnection, DBMain>();
        searchResults =
            new ConcurrentHashMap<ServerToClientConnection, List<Record>>();
        searchProgress =
            new ConcurrentHashMap<ServerToClientConnection, Integer>();
        fileParser = URLyBirdFileHandler.getInstance();
        fieldParsers = URLyBirdFieldParsers.getInstance();
        changeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Add a listener that will listen to server model events.
     *
     * @param listener the listener to add
     */
    void addPropertyChangeListener(final PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove an existing observer (listener).
     *
     * @param listener the listener to remove
     */
    void removePropertyChangeListener(final PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Get the number of records that match the given criteria.
     *
     * @param client the client who requested this search
     * @param criteria the search criteria.
     *        If null, all records will be returned.
     * @param onlyShowModifiableRecords true if only modifiable matching records
     *        are to be included, false if all matching records are
     *        to be included.
     * @return the number of matching records
     */
    int getMatchingRecordCount(final ServerToClientConnection client,
            final List<Criterion> criteria,
            final boolean onlyShowModifiableRecords) {

        LOGGER.entering(THIS_CLASS, "getMatchingRecords");
        //The matching records
        final List<Record> result = new ArrayList<Record>();
        //The record numbers of matching records
        int[] matchNumbers = new int[] {};
        final DBMain databaseConnection = clients.get(client);
        final String[] criteriaAsStringArray =
            getCriteriaAsStringArray(criteria);

        assert databaseConnection != null;

        try {
            matchNumbers = databaseConnection.find(criteriaAsStringArray);
        } catch (RecordNotFoundException e) {
            LOGGER.warning("No records found during search: " + e);
        }

        final Date now = new Date();

        /*
         * The find method might return some spurious matches, so
         * examine each result to ensure it really is a match.
         */
        for (int matchNumberIndex : matchNumbers) {
            final int recordNumber = matchNumberIndex;

            //Read the record to confirm that it really does match
            try {
                String[] possibleMatchFields = databaseConnection
                        .read(recordNumber);

                //The Fields for this matching record.
                List<Field> fieldList =
                    convertToFields(possibleMatchFields, criteria);

                if (fieldList != null) {
                    //Create a record from the fields
                    Record matchingRecord = new Record(recordNumber,
                        SerialNumber.createSerialNumber(), false, fieldList);

                    if (!onlyShowModifiableRecords
                            || policy.isRecordModifiable(matchingRecord
                                    .getFieldList(), now)) {
                        result.add(matchingRecord);
                    }
                }
            } catch (RecordNotFoundException e) {
                //Record was not found, probably deleted by another client.
                LOGGER.finer("Record not found in search: " + recordNumber);
            }
        }

        searchResults.put(client, result);
        searchProgress.put(client, 0);

        return result.size();
    }

    /**
     * Get a list of some of the search results from the previous search.
     * This method may return only some of the matching records, so it
     * may need to be called many times for each search.
     *
     * @param client the client that performed the search
     * @return a list (probably a partial list) of the search results
     *         from the last search performed by the given client
     */
    List<Record> getSearchResults(final ServerToClientConnection client) {
        final List<Record> completeResultList = searchResults.get(client);
        final List<Record> results = new ArrayList<Record>();
        Integer currentProgress = searchProgress.get(client);

        final int howManyToAdd = Math.min(RESULTS_CHUNK_SIZE,
                completeResultList.size() - currentProgress);

        for (int i = 0; i < howManyToAdd; i++) {
            results.add(completeResultList.get(currentProgress + i));
        }
        currentProgress += howManyToAdd;
        searchProgress.put(client, currentProgress);

        return results;
    }

    /**
     * Modify a record so that it has the given field values.
     *
     * @param client the client requesting the modification
     * @param recordNumber the record number
     * @param fields the new fields. Any element may be null, in which case
     *        the field will remain unchanged.
     * @throws RecordNotFoundException if the record number does
     *         not correspond to a record that is not marked as deleted.
     * @throws StaleRecordException if this client's copy of the record
     *         is out of date
     * @throws RecordNotModifiableException if the record could not
     *         be modified
     */
    void modifyRecord(final ServerToClientConnection client,
            final int recordNumber,
            final List<Field> fields) throws RecordNotFoundException,
            StaleRecordException, RecordNotModifiableException {

        LOGGER.entering(THIS_CLASS, "modifyRecord");

        final DBMain databaseConnection = clients.get(client);

        assert databaseConnection != null;

        //Ensure that the record is modifiable
        if (!policy.isRecordModifiable(fields, new Date())) {
            throw new RecordNotModifiableException(recordNumber,
                    "Server policy does not allow this record to be modified");
        }
        boolean lockedOK = false;
        try {
            databaseConnection.lock(recordNumber);
            lockedOK = true;
            databaseConnection.update(recordNumber,
                    requestManager.getStringArrayFromFieldList(fields));
        } catch (LockException e) {
            //If we get a LockException the locking system must be broken
            LOGGER.severe("Unexpected locking exception: " + e);
            throw new IllegalStateException(e);
        } catch (FileWriteException e) {
            throw new RecordNotModifiableException(recordNumber, e);
        } catch (StaleTimestampException e) {
            throw new StaleRecordException(recordNumber, e);
        } finally {
            //Always try to unlock the record if we locked it
            if (lockedOK) {
                databaseConnection.unlock(recordNumber);
            }
        }
    }

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
    void setDatabaseFilename(
            final ProgressListener progressListener, final String newName)
            throws FileNotFoundException, ParseException {

        final File newFile = new File(newName);
        if (!newFile.exists()) {
            throw new FileNotFoundException(newName);
        }
        fileParser.openFile(newFile);

        try {
            fileParser.addProgressListener(progressListener);

            final List<Record> allRecords = fileParser.getAllRecords();

            requestManager = new SimpleRequestManager(
                    allRecords, fileParser.getSchema(),
                    fieldParsers, fileParser);
            policy = new URLyBirdRecordModificationPolicy(
                    fileParser.getSchema());

            if (progressListener != null && !progressListener.isCancelled()) {
                firePropertyChange(
                        SERVER_STARTED, null, newFile.getCanonicalPath());
            }
        } catch (IOException e) {
            throw new ParseException(e.getMessage(), 0);
        } finally {
            fileParser.removeProgressListener(progressListener);
        }
    }

    /**
     * Get the number of clients that are currently connected.
     *
     * @return the number of clients connected
     */
    int getClientCount() {
        return clients.size();
    }

    /**
     * Get an iterator to iterate through the connected clients list.
     *
     * @return an iterator for the connected clients
     */
    Iterator<ServerToClientConnection> iterator() {
        return clients.keySet().iterator();
    }

    /**
     * Add a client to the list of connected clients.
     *
     * @param client the client to add
     */
    void addClient(final ServerToClientConnection client) {

        if (client != null) {
            assert requestManager != null;
            clients.put(client, Data.createData(requestManager));
            firePropertyChange(CLIENT_ADDED, null, client);
        }
    }

    /**
     * Remove a client from the list of connected clients.
     *
     * @param client the client to remove
     */
    void removeClient(final ServerToClientConnection client) {
        clients.remove(client);
        firePropertyChange(CLIENT_REMOVED, null, client);
    }

    /**
     * Get the database table schema.
     *
     * @return the table schema
     */
    TableSchema getSchema() {
        return requestManager.getSchema();
    }

    /**
     * Get the policy that the server uses to determine if
     * a record is modifiable by a client.
     *
     * @return the modification policy
     */
    RecordModificationPolicy getModificationPolicy() {
        return policy;
    }

    /**
     * Get the parsers used to convert fields into strings
     * and vice versa.
     *
     * @return the field parsers
     */
    FieldParsers getFieldParsers() {
        return fieldParsers;
    }

    /**
     * Sends a disconnect message to each client, and removes the
     * clients from the list of connected clients.
     * Also fires a "server stopped" property change to all
     * listeners.
     */
    void stopServer() {

        for (Iterator<ServerToClientConnection> i
                = clients.keySet().iterator(); i.hasNext();) {
            ServerToClientConnection client = i.next();

            try {
                client.serverStopping();
            } catch (RemoteException e) {
                LOGGER.warning("Could not send disconnect message to client: "
                        + e);
            }

            i.remove();
            firePropertyChange(CLIENT_REMOVED, null, client);
        }

        firePropertyChange(SERVER_STOPPED, 0, 1);
    }

    /**
     * Shut down the server gracefully.
     */
    void shutDown() {
        if (requestManager != null) {
            requestManager.shutDown();
        }
    }

    /**
     * Report a bound property update to any registered listeners.
     * No event is fired if old and new are equal and non-null.
     *
     * @param propertyName the programmatic name of the property
     *        that was changed.
     * @param oldValue the old value of the property.
     * @param newValue the new value of the property.
     */
    private void firePropertyChange(final String propertyName,
            final Object oldValue, final Object newValue) {
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Get a list of fields that match the given criteria, or
     * null if the given fields do not match all the criteria.
     *
     * @param possibleMatchFields an array of strings
     *        representing the field values
     *        of a record, in the format expected by the field parsers
     * @param criteria the criteria to match
     * @return a list of fields representing <code>possibleMatchFields</code>,
     *         or null if the record does not match all the criteria
     */
    private List<Field> convertToFields(final String[] possibleMatchFields,
            final List<Criterion> criteria) {

        List<Field> fieldList = new ArrayList<Field>();
        boolean haveMatch = true; //Assume matches until proven otherwise

        for (int i = 0; i < possibleMatchFields.length && haveMatch; i++) {
            final String fieldAsString = possibleMatchFields[i];

            Class<? extends Field> currentFieldClass =
                requestManager.getSchema().getFieldClassByIndex(i);

            //Use the appropriate parser to convert the string into a Field
            FieldParser currentFieldParser = requestManager.getFieldParsers()
                    .getParserForClass(currentFieldClass);

            //Convert the string back into a Field
            Field thisField = currentFieldParser.valueOf(fieldAsString);
            fieldList.add(thisField);

            if (criteria != null && criteria.get(i) != null) {
                //Double check against our criteria
                if (!criteria.get(i).matches(thisField)) {
                    haveMatch = false;
                    fieldList = null;
                }
            }
        }
        return fieldList;
    }

    /**
     * Create an array of strings representing the given criteria.
     * If the criteria is null, this will return an array of nulls.
     *
     * @param criteria the search criteria. May be null.
     * @return an array of Strings representing the given criteria
     */
    private String[] getCriteriaAsStringArray(final List<Criterion> criteria) {

        final int fieldCount = requestManager.getSchema().getFields().size();
        final String[] result = new String[fieldCount];

        if (criteria != null) {
            if (criteria.size() != fieldCount) {
                throw new IllegalArgumentException(
                        "Wrong number of criteria given: expected "
                                + fieldCount + " but got " + criteria.size());
            }
        }

        //Get the criterion string for each field
        for (int f = 0; f < fieldCount; f++) {
            if (criteria == null) {
                result[f] = null;
            } else {
                if (criteria.get(f) == null) {
                    result[f] = null;
                } else {
                    /*
                     * Use a field parser to convert the
                     * criterion field value into a field
                     */
                    final Class<? extends Field> currentFieldClass =
                        requestManager.getSchema().getFieldClassByIndex(f);
                    final FieldParser currentFieldParser =
                        requestManager.getFieldParsers().getParserForClass(
                                    currentFieldClass);
                    final Field matchField = criteria.get(f).getValue();

                    String value = currentFieldParser.getString(matchField);
                    result[f] = value;
                }
            }
        }
        return result;
    }

}
