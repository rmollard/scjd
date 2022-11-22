package suncertify.server;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import suncertify.ClientToServerConnection;
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
 * A server-side connection for clients to connect to.
 * This is a "standalone" implementation,
 * where there is no network communication.
 *
 * @author Robert Mollard
 */
public final class StandaloneClientToServerConnection implements
        ClientToServerConnection {

    /**
     * Our (restricted) view of the server.
     */
    private RequestServer server;

    /**
     * Create a new standalone client-to-server connection.
     *
     * @param server the server to delegate to (must not be null)
     * @throws IllegalArgumentException if <code>server</code> is null
     */
    StandaloneClientToServerConnection(final RequestServer server) {
        if (server == null) {
            throw new IllegalArgumentException("Server must not be null");
        }
        this.server = server;
    }

    /** {@inheritDoc} */
    public Date connectToServer(final ServerToClientConnection client) {
        server.addClient(client);
        return new Date();
    }

    /** {@inheritDoc} */
    public TableSchema getSchema() {
        return server.getSchema();
    }

    /** {@inheritDoc} */
    public void disconnectFromServer(final ServerToClientConnection client) {
        //Don't need to do anything
    }

    /** {@inheritDoc} */
    public int search(final ServerToClientConnection client,
            final List<Criterion> criteria,
            final boolean onlyShowModifiableRecords) {

        return server.getMatchingRecordCount(client, criteria,
                onlyShowModifiableRecords);
    }

    /** {@inheritDoc} */
    public void modifyRecord(final ServerToClientConnection client,
            final int recordNumber, final List<Field> newFields)
            throws RecordNotFoundException,
            StaleRecordException, RecordNotModifiableException {

        server.modifyRecord(client, recordNumber, newFields);
    }

    /** {@inheritDoc} */
    public void setServerFile(final ProgressListener progressListener,
            final String newFileName) throws FileNotFoundException,
            ParseException {

        server.setDatabaseFilename(progressListener, newFileName);
    }

    /** {@inheritDoc} */
    public List<Record> getSearchResults(
            final ServerToClientConnection client) {
        return server.getSearchResults(client);
    }

    /** {@inheritDoc} */
    public RecordModificationPolicy getModificationPolicy() {
        return server.getModificationPolicy();
    }

    /** {@inheritDoc} */
    public FieldParsers getFieldParsers() {
        return server.getFieldParsers();
    }

}
