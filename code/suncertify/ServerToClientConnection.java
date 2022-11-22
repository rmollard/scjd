package suncertify;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The methods that a server can invoke on a client that is connected to it.
 *
 * @author Robert Mollard
 */
public interface ServerToClientConnection extends Remote {

    /**
     * Notifies the client that the server is about to stop.
     *
     * @throws RemoteException if there is a network problem
     */
    void serverStopping() throws RemoteException;

    /**
     * Gets a localized description of the client connection. Typically a
     * description will contain the host name and IP address.
     *
     * @return a description of the client connection
     * @throws RemoteException if there is a network problem
     */
    String getDescription() throws RemoteException;

}
