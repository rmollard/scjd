package suncertify.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
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
 * An RMI implementation of the
 * <code>ClientToServerConnection</code> interface.
 *
 * Clients should call <code>connectToServer</code> first.
 *
 * @author Robert Mollard
 */
public final class RMIClientToServerConnection
    extends UnicastRemoteObject implements ClientToServerConnection {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 7443550152447795803L;

    /**
     * Our (restricted) view of the server.
     */
    private transient RequestServer server;

    /**
     * Create a new RMI client-to-server connection
     * using the given server.
     *
     * @param server the server controller. Must not be null.
     * @throws RemoteException if there is a network problem
     * @throws IllegalArgumentException if <code>server</code> is null
     */
    RMIClientToServerConnection(final RequestServer server)
            throws RemoteException {
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
        server.removeClient(client);
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
            final int recordNumber,
            final List<Field> newFields) throws RecordNotFoundException,
            StaleRecordException, RecordNotModifiableException {

        server.modifyRecord(client, recordNumber, newFields);
    }

    /** {@inheritDoc} */
    public void setServerFile(final ProgressListener progressListener,
            final String newFileName) {

        //We don't currently let the network clients switch the server file.
        throw new UnsupportedOperationException("Not implemented yet");
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
