package suncertify;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Logger;

/**
 * An RMI network server responsible for providing an RMI connection.
 *
 * @author Robert Mollard
 */
public final class RMINetworkServer implements NetworkServer {

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify");

    /**
     * Default public constructor.
     */
    public RMINetworkServer() {
        //Empty
    }

    /**
     * Get the RMI server name for the given IP address and network port.
     *
     * @param ip the IP address of the server
     * @param port the networking port to be used
     * @return the RMI server name
     */
    private String getServerName(final String ip, final int port) {
        return "rmi://" + ip + ":" + port + "/URLyBirdService";
    }

    /** {@inheritDoc} */
    public void initializeServer(final int port) throws RemoteException {
        LocateRegistry.createRegistry(port);
        LOGGER.info("Server initialized on port " + port);
    }

    /** {@inheritDoc} */
    public void startServer(final String ip, final int port,
            final ClientToServerConnection connection) throws RemoteException {
        try {
            Naming.rebind(getServerName(ip, port), connection);
            LOGGER.info("Started server, ip is: " + ip + " port is: " + port);
        } catch (MalformedURLException e) {
            throw new RemoteException("Invalid URL", e);
        }
    }

    /** {@inheritDoc} */
    public void stopServer(final String ip, final int port)
        throws RemoteException, ServerNotRunningException {
        try {
            Naming.unbind(getServerName(ip, port));
            LOGGER.info("Stopped server, ip is: " + ip + " port is: " + port);
        } catch (MalformedURLException e) {
            throw new RemoteException("Invalid URL", e);
        } catch (NotBoundException e) {
            throw new ServerNotRunningException(e);
        }
    }

    /** {@inheritDoc} */
    public ClientToServerConnection getServerConnection(
            final String ip, final int port)
            throws RemoteException, ServerNotRunningException {
        ClientToServerConnection result = null;

        try {
            result = (ClientToServerConnection)
                Naming.lookup(getServerName(ip, port));
        } catch (MalformedURLException e) {
            throw new RemoteException("Invalid URL", e);
        } catch (NotBoundException e) {
            throw new ServerNotRunningException(e);
        }
        return result;
    }

}
