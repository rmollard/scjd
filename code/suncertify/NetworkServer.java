package suncertify;

import java.rmi.RemoteException;

/**
 * A network server responsible for sharing a server connection.
 *
 * @author Robert Mollard
 */
public interface NetworkServer {

    /**
     * Perform any initialization required.
     * This method is called when the program starts up.
     *
     * @param port the networking port to be used
     * @throws RemoteException if there is a network problem
     */
    void initializeServer(int port) throws RemoteException;

    /**
     * Start sharing the given connection on the given network port,
     * using the ip address specified.
     *
     * @param ip the IP address of the server
     * @param port the networking port to be used
     * @param connection the server connection to share
     * @throws RemoteException if there is a network problem
     */
    void startServer(String ip, int port, ClientToServerConnection connection)
            throws RemoteException;

    /**
     * Stop the server and prevent clients from connecting.
     *
     * @param ip the IP address of the server
     * @param port the networking port to be used
     * @throws RemoteException if there is a network problem
     * @throws ServerNotRunningException if the server has not been started
     */
    void stopServer(String ip, int port) throws RemoteException,
            ServerNotRunningException;

    /**
     * Get a reference to the server connection.
     *
     * @param ip the IP address of the server
     * @param port the networking port to be used
     * @return a reference to the server connection
     * @throws RemoteException if there is a network problem
     * @throws ServerNotRunningException if the server has not been started
     */
    ClientToServerConnection getServerConnection(String ip, int port)
            throws RemoteException, ServerNotRunningException;

}
