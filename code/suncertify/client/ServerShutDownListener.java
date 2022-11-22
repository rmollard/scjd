package suncertify.client;

/**
 * A listener that is notified when the server is about to stop.
 *
 * @author Robert Mollard
 */
public interface ServerShutDownListener {

    /**
     * This method is called when the server is about to stop.
     */
    void serverStopping();

}
