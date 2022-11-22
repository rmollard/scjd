package suncertify.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import suncertify.ServerToClientConnection;
import suncertify.utils.Localization;

/**
 * An RMI implementation of the methods that a server
 * can invoke on a client that is connected to it.
 *
 * @author Robert Mollard
 */
public final class RMIServerToClientConnection
    extends UnicastRemoteObject implements ServerToClientConnection {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 8720349370808127001L;

    /**
     * The listener that will be notified when the server shuts down.
     */
    private transient ServerShutDownListener listener;

    /**
     * Create a new RMI server-to-client connection.
     * The given listener will be
     * informed when the server shuts down.
     *
     * @param listener the listener that will be
     *        notified when the server shuts down. Must not be null.
     * @throws RemoteException if there is a network problem
     * @throws IllegalArgumentException if <code>listener</code> is null
     */
    RMIServerToClientConnection(final ServerShutDownListener listener)
            throws RemoteException {
        if (listener == null) {
            throw new IllegalArgumentException("Listener must not be null");
        }
        this.listener = listener;
    }

    /** {@inheritDoc} */
    public void serverStopping() {
        listener.serverStopping();
    }

    /** {@inheritDoc} */
    public String getDescription() {
        String result;
        try {
            InetAddress address = InetAddress.getLocalHost();
            result = Localization.getString("client.description",
                    address.getHostName(), address.getHostAddress());
        } catch (UnknownHostException e) {
            result = Localization.getString("client.unknownHost");
        }
        return result;
    }

}
