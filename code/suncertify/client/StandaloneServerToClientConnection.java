package suncertify.client;

import suncertify.ServerToClientConnection;
import suncertify.utils.Localization;

/**
 * A standalone implementation of the methods that a server can invoke on a
 * client that is connected to it.
 *
 * @author Robert Mollard
 */
public final class StandaloneServerToClientConnection implements
        ServerToClientConnection {

    /**
     * Default public constructor.
     */
    public StandaloneServerToClientConnection() {
        //Empty
    }

    /** {@inheritDoc} */
    public void serverStopping() {
        /*
         * We don't need to do anything.
         * In standalone mode, the client and the
         * server are both part of the same program.
         */
    }

    /** {@inheritDoc} */
    public String getDescription() {
        return Localization.getString(
                "client.standalone.connectionDescription");
    }

}
