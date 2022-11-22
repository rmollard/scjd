package suncertify.server;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import suncertify.ClientToServerConnection;
import suncertify.Controller;
import suncertify.Criterion;
import suncertify.IconFactory;
import suncertify.NetworkServer;
import suncertify.ProgressListener;
import suncertify.RMINetworkServer;
import suncertify.RecordModificationPolicy;
import suncertify.RecordNotModifiableException;
import suncertify.ServerNotRunningException;
import suncertify.ServerToClientConnection;
import suncertify.StaleRecordException;
import suncertify.URLyBirdErrors;
import suncertify.db.FieldParsers;
import suncertify.db.Record;
import suncertify.db.RecordNotFoundException;
import suncertify.db.TableSchema;
import suncertify.fields.Field;
import suncertify.panels.AbstractInputDialog;
import suncertify.panels.FileOpenDialog;
import suncertify.panels.ProgressDialog;
import suncertify.utils.EditManager;
import suncertify.utils.Localization;

/**
 * MVC controller class for URLyBird server.
 * Standalone and network server modes are supported.
 * If running in network server mode, the server GUI will be displayed.
 *
 * There is a shutdown hook to ensure that the
 * database file is shut down gracefully.
 *
 * Thread safety: not thread safe.
 *
 * @author Robert Mollard
 */
public final class ServerController implements Controller, RequestServer {

    /**
     * Action to allow clients to connect to the server.
     */
    @SuppressWarnings("serial")
    private final class StartServerAction
        extends AbstractAction implements Runnable {

        /**
         * Thread used to perform the request.
         */
        private Thread theThread;

        private StartServerAction() {

            putValue(Action.NAME, Localization
                    .getString("server.start.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("server.start.description"));
            putValue(Action.SMALL_ICON, IconFactory.getImageIcon(Localization
                    .getString("server.start.icon")));
        }

        public void actionPerformed(final ActionEvent e) {
            start();
        }

        public void run() {
            try {
                this.setEnabled(false); //Disable until the server is stopped

                if (inputDialog.showDialog(view)) {

                    progressDialog.setLocationRelativeTo(view);
                    progressDialog.setVisible(true);

                    setDatabaseFilename(progressDialog,
                            inputDialog.getInputString());

                    progressDialog.setVisible(false);

                    final boolean cancelled = progressDialog.isCancelled();

                    if (!cancelled) {
                        networkSupport.startServer(ip, port, connection);

                        stopServer.setEnabled(true);
                    }
                }
            } catch (FileNotFoundException e) {
                handleError(URLyBirdErrors.FILE_NOT_FOUND, e);
            } catch (ParseException e) {
                handleError(URLyBirdErrors.FILE_CORRUPT, e);
            } catch (RemoteException e) {
                handleError(URLyBirdErrors.NETWORK_ERROR, e);
            } catch (Exception e) {
                handleError(URLyBirdErrors.UNEXPECTED_EXCEPTION, e);
            } finally {
                progressDialog.setVisible(false);
                //Either the start or stop action should always be enabled
                if (stopServer.isEnabled()) {
                    this.setEnabled(false);
                } else {
                    this.setEnabled(true);
                }
                theThread = null;
            }
        }

        /**
         * Creates a new thread and runs the request using
         * the new thread.
         */
        private void start() {
            theThread = new Thread(null, this, "Server action thread");
            theThread.setDaemon(true);
            theThread.start();
        }
    }

    /**
     * Simple action to stop the server.
     * Any connected clients will be informed that the
     * server is shutting down.
     */
    @SuppressWarnings("serial")
    private final class StopServerAction extends AbstractAction {

        private StopServerAction() {
            putValue(Action.NAME, Localization
                    .getString("server.stop.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("server.stop.description"));
            putValue(Action.SMALL_ICON, IconFactory.getImageIcon(Localization
                    .getString("server.stop.icon")));
        }

        public void actionPerformed(final ActionEvent ev) {
            try {
                //Whether or not we should stop the server
                boolean shouldStop = true;

                final int clientsConnected = model.getClientCount();
                if (clientsConnected > 0) {

                    final String maintext;
                    if (clientsConnected == 1) {
                        maintext =
                            Localization.getString(
                                    "server.confirmStopAndDisconnectClient");
                    } else {
                        maintext = Localization.getString(
                                "server.confirmStopAndDisconnectClients",
                                clientsConnected);
                    }

                    //Ask if the user wants to discard changes
                    int choice = JOptionPane.showConfirmDialog(view, maintext,
                            Localization.getString("server.confirmStop"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    shouldStop = (choice == JOptionPane.YES_OPTION);
                }

                if (shouldStop) {
                    model.stopServer();

                    networkSupport.stopServer(ip, port);

                    LOGGER.fine("Stopped server");
                    startServer.setEnabled(true);
                }
            } catch (RemoteException e) {
                handleError(URLyBirdErrors.NETWORK_ERROR, e);
            } catch (ServerNotRunningException e) {
                handleError(URLyBirdErrors.SERVER_NOT_RUNNING, e);
            } catch (Exception e) {
                handleError(URLyBirdErrors.UNEXPECTED_EXCEPTION, e);
            } finally {
                //Either the start or stop action should always be enabled
                if (startServer.isEnabled()) {
                    this.setEnabled(false);
                } else {
                    this.setEnabled(true);
                }
            }
        }
    }

    /**
     * Simple action to exit the program.
     */
    @SuppressWarnings("serial")
    private final class ExitAction extends AbstractAction {

        private ExitAction() {
            putValue(Action.NAME, Localization
                    .getString("server.exit.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("server.exit.description"));
        }

        public void actionPerformed(final ActionEvent e) {
            handleExit();
        }
    }

    /**
     * Simple action to select all text in the server
     * event log text area.
     */
    @SuppressWarnings("serial")
    private final class SelectAllAction extends AbstractAction {

        private SelectAllAction() {
            putValue(Action.NAME, Localization
                    .getString("server.selectAll.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("server.selectAll.description"));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                    .getChar("server.selectAll.mnemonic")));

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_A, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(final ActionEvent e) {
            view.selectAll();
        }
    }

    /**
     * Simple action to show the About dialog box.
     */
    @SuppressWarnings("serial")
    private final class HelpAboutAction extends AbstractAction {

        private HelpAboutAction() {
            putValue(Action.NAME, Localization
                    .getString("help.about.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("help.about.description"));
            putValue(Action.SMALL_ICON, IconFactory.getImageIcon(Localization
                    .getString("help.about.icon")));
        }

        public void actionPerformed(final ActionEvent e) {
            view.showAboutPanel();
        }
    }

    /**
     * Simple action to show help for the program.
     */
    @SuppressWarnings("serial")
    private final class HelpAction extends AbstractAction {

        private HelpAction() {
            putValue(Action.NAME, Localization
                    .getString("help.contents.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("help.contents.description"));
            putValue(Action.SMALL_ICON, IconFactory.getImageIcon(Localization
                    .getString("help.contents.icon")));

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_F1, 0));
        }

        public void actionPerformed(final ActionEvent e) {
            view.showHelpPanel();
        }
    }

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify.server");

    /**
     * The services provided to clients.
     */
    private ClientToServerConnection connection;

    /**
     * Networking support. Only used in network server mode.
     */
    private final NetworkServer networkSupport;

    /**
     * The MVC model.
     */
    private final ServerModel model;

    /**
     * The MVC view. This will be null if running in standalone mode.
     */
    private final ServerView view;

    /**
     * Action to start sharing the database file with any clients that
     * connect.
     */
    private final Action startServer = new StartServerAction();

    /**
     * Action to stop the server and prevent clients connecting to it.
     */
    private final Action stopServer = new StopServerAction();

    /**
     * Action to exit the program.
     */
    private final Action exitAction = new ExitAction();

    /**
     * Action to display the About dialog box.
     */
    private final Action helpAboutAction = new HelpAboutAction();

    /**
     * Action to display the help dialog.
     */
    private final Action helpAction = new HelpAction();

    /**
     * Action to select all text in the server event log.
     */
    private final Action selectAllAction = new SelectAllAction();

    /**
     * Dialog box for entering server configuration information.
     */
    private AbstractInputDialog inputDialog;

    /**
     * Progress dialog for indicating progress in opening the
     * server database file.
     */
    private final ProgressDialog progressDialog;

    /**
     * The name of the server RMI connection, in URL format.
     */
    private final String ip;

    /**
     * The port to use for networking.
     */
    private final int port;

    /**
     * Create a server controller in standalone mode.
     */
    public ServerController() {
        this(0, true);
    }

    /**
     * Create a server controller in network server mode.
     *
     * @param port the network port to use.
     */
    public ServerController(final int port) {
        this(port, false);
    }

    /**
     * Create a server controller in network server mode or
     * standalone mode.
     * If running in network server mode, the GUI will be displayed.
     *
     * @param port the network port to use.
     *        This is ignored if <code>isStandalone</code> is true.
     * @param isStandalone true if running in standalone mode, or false if
     *        running in network server mode.
     */
    private ServerController(final int port, final boolean isStandalone) {
        this.ip = "localhost";
        this.port = port;

        model = new ServerModel();

        if (isStandalone) {
            //Standalone mode uses the client view
            view = null;
            progressDialog = null;
            networkSupport = null;
        } else {
            view = new ServerView();
            networkSupport = new RMINetworkServer();

            progressDialog = new ProgressDialog(
                    view, Localization.getString("dialog.openingFile"));
            progressDialog.setTitle(
                    Localization.getString("dialog.openingFileTitle",
                            Localization.getString("programName")));

            view.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    handleExit();
                }
            });

            model.addPropertyChangeListener(view);

            inputDialog = new FileOpenDialog(
                view, Localization.getString("dialog.openFileTitle",
                    Localization.getString("programName")),
                    Localization.getString("databaseFileFilter.extension"));

            addActionHandlers();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cleanUpAtExit();
            }
        });

        stopServer.setEnabled(false); //Disabled until the server is started

        if (isStandalone) {
            connection = new StandaloneClientToServerConnection(this);
        } else {
            view.showGUI();
            try {
                connection = new RMIClientToServerConnection(this);

                networkSupport.initializeServer(port);
            } catch (RemoteException e) {
                connection = null;
                handleError(URLyBirdErrors.NETWORK_ERROR, e);
            }
        }
    }

    /**
     * Get the controller's client-to-server connection.
     *
     * @return the client-to-server connection
     */
    public ClientToServerConnection getConnection() {
        return connection;
    }

    /** {@inheritDoc} */
    public int getMatchingRecordCount(final ServerToClientConnection client,
            final List<Criterion> criteria,
            final boolean onlyShowModifiableRecords) {
        return model.getMatchingRecordCount(client, criteria,
                onlyShowModifiableRecords);
    }

    /** {@inheritDoc} */
    public List<Record> getSearchResults(
            final ServerToClientConnection client) {
        return model.getSearchResults(client);
    }

    /** {@inheritDoc} */
    public void showWarning(final String message) {
        view.showWarning(message);
    }

    /** {@inheritDoc} */
    public RecordModificationPolicy getModificationPolicy() {
        return model.getModificationPolicy();
    }

    /** {@inheritDoc} */
    public FieldParsers getFieldParsers() {
        return model.getFieldParsers();
    }

    /** {@inheritDoc} */
    public TableSchema getSchema() {
        return model.getSchema();
    }

    /** {@inheritDoc} */
    public void addClient(final ServerToClientConnection client) {
        model.addClient(client);
        LOGGER.fine("Added client to server");
    }

    /** {@inheritDoc} */
    public void removeClient(final ServerToClientConnection client) {
        model.removeClient(client);
        LOGGER.fine("Removed client from server");
    }

    /** {@inheritDoc} */
    public void modifyRecord(final ServerToClientConnection client,
            final int recordNumber,
            final List<Field> fields) throws RecordNotFoundException,
            StaleRecordException, RecordNotModifiableException {
        model.modifyRecord(client, recordNumber, fields);

        LOGGER.fine("Modified record number " + recordNumber);
    }

    /** {@inheritDoc} */
    public void setDatabaseFilename(final ProgressListener progressListener,
            final String newName)
            throws FileNotFoundException, ParseException {
        model.setDatabaseFilename(progressListener, newName);

        LOGGER.fine("Changed database file to: " + newName);
    }

    /**
     * Associate the controller actions with the command
     * options provided by the view.
     */
    private void addActionHandlers() {

        view.getToolbar().setAction(ServerToolbar.Commands.START_SERVER,
                startServer);
        view.getMenu().setAction(ServerMenuBar.Commands.START_SERVER,
                startServer);

        view.getToolbar().setAction(ServerToolbar.Commands.STOP_SERVER,
                stopServer);
        view.getMenu().setAction(ServerMenuBar.Commands.STOP_SERVER,
                stopServer);

        view.getToolbar().setAction(ServerToolbar.Commands.HELP,
                helpAction);
        view.getMenu().setAction(ServerMenuBar.Commands.HELP,
                helpAction);

        view.getMenu().setAction(ServerMenuBar.Commands.HELP_ABOUT,
                helpAboutAction);

        view.getMenu().setAction(ServerMenuBar.Commands.EXIT,
                exitAction);

        view.setAction(ServerView.Commands.SELECT_ALL,
                selectAllAction);
        view.getMenu().setAction(ServerMenuBar.Commands.SELECT_ALL,
                selectAllAction);

        view.getMenu().setAction(ServerMenuBar.Commands.COPY,
                EditManager.getCopyAction());
        view.setAction(ServerView.Commands.COPY,
                EditManager.getCopyAction());
    }

    /**
     * Handle an error by logging it and displaying an error
     * message dialog box to the user.
     *
     * @param error the error encountered
     * @param cause the <code>Throwable</code> that caused the error
     */
    private void handleError(
            final URLyBirdErrors error, final Throwable cause) {

        Throwable problem = cause;
        StringBuilder text = new StringBuilder();

        //Log the whole stack trace
        while (problem != null) {
            text.append(problem + "\n");
            StackTraceElement[] trace = problem.getStackTrace();
            //Display the stack trace text for the current problem
            for (int i = 0; i < trace.length; i++) {
                text.append("    " + trace[i] + "\n");
            }
            problem = problem.getCause();
        }

        LOGGER.warning("Exception occurred: " + text.toString());

        view.showError(error.getErrorNumber(),
                error.getLocalizedErrorDescription(), cause);
    }

    /**
     * The user wants to exit.
     * If there are any clients connected to the server,
     * we ask the user for confirmation before exiting.
     */
    private void handleExit() {

        //True if we should exit the program
        boolean shouldExit = true;
        final int clientsConnected = model.getClientCount();

        //If any clients are connected, ask the user to confirm exit
        if (clientsConnected > 0) {

            final String message; //Dialog box message
            if (clientsConnected == 1) {
                message = Localization
                        .getString("server.confirmQuitAndDisconnectClient");
            } else {
                message = Localization.getString(
                        "server.confirmQuitAndDisconnectClients",
                        clientsConnected);
            }

            final int choice = JOptionPane.showConfirmDialog(view, message,
                    Localization.getString("server.confirmQuit"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) {
                shouldExit = false;
            }
        }

        if (shouldExit) {
            System.exit(0);
        }
    }

    /**
     * Ensures a graceful shutdown by sending the disconnect
     * message to each client, and then shutting down
     * the back end.
     */
    private void cleanUpAtExit() {

        //Send shutdown messages to any clients that are connected
        for (Iterator<ServerToClientConnection> i = model.iterator();
                                                        i.hasNext();) {
            final ServerToClientConnection connectClient = i.next();

            try {
                connectClient.serverStopping();
            } catch (Exception e) {
                LOGGER.warning("Exception when shutting down server: " + e);
            }
        }

        //Safely shut down the back end
        model.shutDown();
    }

}
