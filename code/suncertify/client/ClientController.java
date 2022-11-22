package suncertify.client;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import suncertify.ClientToServerConnection;
import suncertify.Controller;
import suncertify.Criterion;
import suncertify.IconFactory;
import suncertify.NetworkServer;
import suncertify.RMINetworkServer;
import suncertify.RecordModificationPolicy;
import suncertify.RecordNotModifiableException;
import suncertify.ServerNotRunningException;
import suncertify.ServerToClientConnection;
import suncertify.StaleRecordException;
import suncertify.URLyBirdErrors;
import suncertify.WidgetFactory;
import suncertify.db.Record;
import suncertify.db.RecordNotFoundException;
import suncertify.db.TableSchema;
import suncertify.fields.Field;
import suncertify.fields.FieldDetails;
import suncertify.panels.AbstractInputDialog;
import suncertify.panels.FileOpenDialog;
import suncertify.panels.ModifyRecordPanel;
import suncertify.panels.ProgressDialog;
import suncertify.panels.RMIAddressDialog;
import suncertify.utils.EditManager;
import suncertify.utils.Localization;

/**
 * MVC controller for URLyBird client.
 * Standalone and network client modes are supported.
 *
 * @author Robert Mollard
 */
public final class ClientController implements Controller,
        TableSelectionListener, ServerShutDownListener {

    /**
     * An action that can be cancelled by the user.
     */
    private abstract class CancellableAction extends AbstractAction {

        /**
         * Gets set to true when the user cancels the action.
         */
        private volatile boolean cancelled = false;

        /**
         * Determine if the action has been cancelled by the user.
         *
         * @return true if the action has been cancelled
         */
        protected boolean isCancelled() {
            return cancelled;
        }

        /**
         * Sets the cancelled status of the action.
         *
         * @param isCancelled true if the action is cancelled
         */
        protected void setCancelled(final boolean isCancelled) {
            this.cancelled = isCancelled;
        }
    }

    /**
     * Action to connect to a server. We run the request in a new thread.
     */
    @SuppressWarnings("serial")
    private final class ConnectAction extends
        CancellableAction implements Runnable {

        /**
         * The thread used to perform the connect action.
         */
        private Thread connectThread;

        private ConnectAction() {

            if (isStandalone) {
                putValue(Action.NAME, Localization
                        .getString("client.standalone.connect.name"));
                putValue(Action.SHORT_DESCRIPTION, Localization
                        .getString("client.standalone.connect.description"));
                putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                        .getChar("client.standalone.connect.mnemonic")));
                putValue(Action.SMALL_ICON, IconFactory
                        .getImageIcon(Localization
                                .getString("client.standalone.connect.icon")));
            } else {
                putValue(Action.NAME, Localization
                        .getString("client.connect.name"));
                putValue(Action.SHORT_DESCRIPTION, Localization
                        .getString("client.connect.description"));
                putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                        .getChar("client.connect.mnemonic")));
                putValue(Action.SMALL_ICON, IconFactory
                        .getImageIcon(Localization
                                .getString("client.connect.icon")));
            }

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_O, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        /**
         * Performs the request by spawning a new thread.
         */
        private void start() {
            this.setEnabled(false);
            connectThread = new Thread(null, this, "Client action thread");
            connectThread.setDaemon(true);
            connectThread.start();
        }

        public void actionPerformed(final ActionEvent e) {
            setCancelled(false);
            start();
        }

        public void run() {

            try {
                if (connectionDialog.showDialog(view)) {

                    //Disconnect from existing connection first
                    if (disconnectAction.isEnabled()) {
                        disconnectAction.actionPerformed(null);
                    }

                    if (isStandalone) {

                        final String filename =
                            connectionDialog.getInputString();

                        progressDialog.setLocationRelativeTo(view);
                        progressDialog.setVisible(true);

                        //Open the file
                        serverConnection.setServerFile(
                                    progressDialog, filename);
                        progressDialog.setVisible(false);
                        setCancelled(progressDialog.isCancelled());
                    } else {
                        view.setCursor(WidgetFactory.WAIT_CURSOR);

                        final String ip = connectionDialog.getInputString();

                        serverConnection =
                            networkSupport.getServerConnection(ip, port);
                    }

                    if (!isCancelled()) {
                        final Date currentServerDate =
                            serverConnection.connectToServer(connection);
                        final Date now = new Date();

                        dateDifference =
                            currentServerDate.getTime() - now.getTime();

                        final TableSchema schema =
                            serverConnection.getSchema();
                        policy = serverConnection.getModificationPolicy();
                        model.setFieldParsers(
                                serverConnection.getFieldParsers());

                        model.setSchema(schema);

                        disconnectAction.setEnabled(true);

                        view.addStatusMessage(
                                Localization.getString("client.connected",
                                        connectionDialog.getDescription()));

                        searchAction.setEnabled(true);
                    }
                }
            } catch (FileNotFoundException e) {
                handleError(URLyBirdErrors.FILE_NOT_FOUND, e);
            } catch (ParseException e) {
                handleError(URLyBirdErrors.FILE_CORRUPT, e);
            } catch (RemoteException e) {
                handleError(URLyBirdErrors.NETWORK_ERROR, e);
            } catch (ServerNotRunningException e) {
                handleError(URLyBirdErrors.SERVER_NOT_RUNNING, e);
            } catch (Exception e) {
                handleError(URLyBirdErrors.UNEXPECTED_EXCEPTION, e);
            } finally {
                view.setCursor(WidgetFactory.DEFAULT_CURSOR);
                progressDialog.setVisible(false);

                this.setEnabled(!disconnectAction.isEnabled());

                connectThread = null;
            }
        }
    }

    /**
     * Action to disconnect from a server. This action should only be enabled
     * when connected to a server.
     */
    @SuppressWarnings("serial")
    private final class DisconnectAction extends AbstractAction {

        private DisconnectAction() {

            if (isStandalone) {
                putValue(Action.NAME, Localization
                        .getString("client.standalone.disconnect.name"));
                putValue(Action.SHORT_DESCRIPTION, Localization
                        .getString("client.standalone.disconnect.description"));
            } else {
                putValue(Action.NAME, Localization
                        .getString("client.disconnect.name"));
                putValue(Action.SHORT_DESCRIPTION, Localization
                        .getString("client.disconnect.description"));
            }

            putValue(Action.SMALL_ICON, IconFactory.getImageIcon(Localization
                    .getString("client.disconnect.icon")));
        }

        public void actionPerformed(final ActionEvent e) {
            try {
                disconnect(false);
            } catch (Exception ex) {
                handleError(URLyBirdErrors.UNEXPECTED_EXCEPTION, ex);
            }
        }
    }

    /**
     * A simple action to select all records in the search results table.
     */
    @SuppressWarnings("serial")
    private final class SelectAllRowsAction extends AbstractAction {

        private SelectAllRowsAction() {
            putValue(Action.NAME, Localization
                    .getString("client.selectAll.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("client.selectAll.description"));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                    .getChar("client.selectAll.mnemonic")));

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_A, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(final ActionEvent e) {
            view.getTable().selectAll();
        }
    }

    /**
     * Simple action to exit the program.
     */
    @SuppressWarnings("serial")
    private final class ExitAction extends AbstractAction {

        private ExitAction() {
            putValue(Action.NAME, Localization
                    .getString("client.exit.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("client.exit.description"));
        }

        public void actionPerformed(final ActionEvent e) {
            handleExit();
        }
    }

    /**
     * Action to perform a search for matching records. To make the GUI more
     * responsive, a new thread is created to perform the search.
     */
    @SuppressWarnings("serial")
    private final class SearchAction
        extends CancellableAction implements Runnable {

        /**
         * The thread that is used to perform the search.
         */
        private Thread searchThread;

        private SearchAction() {
            putValue(Action.NAME, Localization
                    .getString("client.search.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("client.search.description"));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                    .getChar("client.search.mnemonic")));
            putValue(Action.SMALL_ICON, IconFactory.getImageIcon(Localization
                    .getString("client.search.icon")));

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_S, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        /**
         * Performs the request by spawning a new thread.
         */
        private void start() {
            cancelSearchAction.setEnabled(true);
            this.setEnabled(false);
            searchThread = new Thread(null, this, "Search thread");
            searchThread.setDaemon(true);
            searchThread.start();
        }

        public void actionPerformed(final ActionEvent e) {
            setCancelled(false);
            start();
        }

        public void run() {

            try {
                List<FieldDetails> fields =
                    model.getCurrentSchema().getFields();
                List<Criterion> criteria = new ArrayList<Criterion>();

                for (int i = 0; i < fields.size(); i++) {
                    criteria.add(view.getFieldCriterion(i));
                }

                view.searchPerformed();

                final int resultCount = serverConnection.search(connection,
                        criteria, view.isOnlyBookableChecked());

                model.setSearchResultTotal(resultCount);

                final List<Record> results = new ArrayList<Record>();

                /*
                 * Keep reading until we have all the results or the user
                 * cancels the search.
                 */
                for (int resultsRead = 0; resultsRead < resultCount
                        && !isCancelled();) {

                    final List<Record> resultChunk =
                        serverConnection.getSearchResults(connection);

                    results.addAll(resultChunk);

                    resultsRead += resultChunk.size();

                    model.setSearchProgress(resultsRead);
                }

                if (isCancelled()) {
                    model.cancelSearch();
                } else {
                    model.setRowData(results);
                }

            } catch (RemoteException e) {
                handleError(URLyBirdErrors.NETWORK_ERROR, e);
            } catch (Exception e) {
                handleError(URLyBirdErrors.UNEXPECTED_EXCEPTION, e);
            } finally {
                cancelSearchAction.setEnabled(false);
                this.setEnabled(true);
                searchThread = null;
            }
        }
    }

    /**
     * Action to cancel the current search. This action should be disabled
     * unless there is a search in progress.
     */
    @SuppressWarnings("serial")
    private final class CancelSearchAction extends AbstractAction {

        private CancelSearchAction() {
            putValue(Action.NAME, Localization
                    .getString("client.cancelSearch.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("client.cancelSearch.description"));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                    .getChar("client.cancelSearch.mnemonic")));

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_N, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(final ActionEvent e) {
            searchAction.setCancelled(true);

            searchAction.setEnabled(true);
            cancelSearchAction.setEnabled(false);
        }
    }

    /**
     * Action for modifying the currently selected record. We display a dialog
     * box that allows the user to enter new values for the modifiable fields.
     */
    @SuppressWarnings("serial")
    private final class ModifyRecordAction extends AbstractAction {

        private ModifyRecordAction() {
            putValue(Action.NAME, Localization
                    .getString("client.book.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("client.book.description"));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                    .getChar("client.book.mnemonic")));
        }

        public void actionPerformed(final ActionEvent e) {

            try {
                final Record recordToBook = currentRecord;
                final int recNo = recordToBook.getRecordNumber();

                /*
                 * This could be made more flexible by dynamically loading the
                 * parser classes from the server instead of using the URLyBird
                 * field parsers instance.
                 */
                modifyPanel.loadRecord(recordToBook,
                        model.getCurrentSchema().getFields(),
                        model.getFieldParsers(), view.getFieldViewers());

                final boolean okPressed = modifyPanel.showDialog(view);

                if (okPressed) {
                    view.setCursor(WidgetFactory.WAIT_CURSOR);

                    List<Field> modifiedFields =
                        modifyPanel.getModifiedFieldValues();

                    List<Field> newFields = new ArrayList<Field>();
                    List<Field> oldFields = recordToBook.getFieldList();

                    newFields.addAll(modifiedFields);
                    //Use the existing field if no new value is given
                    for (int i = 0; i < oldFields.size(); i++) {
                        if (newFields.get(i) == null) {
                            newFields.set(i, oldFields.get(i));
                        }
                    }

                    if (serverConnection == null) {
                        //Server has been shut down
                        throw new ServerNotRunningException(
                            new NullPointerException("Server has shut down"));
                    }
                    //Change record on the server
                    serverConnection.modifyRecord(connection, recNo, newFields);

                    /*
                     * If we get to here, the update worked, so update the row
                     * in the client model.
                     */
                    Record newRecord = new Record(recNo,
                            recordToBook.getTimestamp(),
                            recordToBook.isDeleted(), newFields);

                    model.modifyRecord(newRecord);

                    view.addStatusMessage(
                            Localization.getString("client.recordModified"));

                    model.deselectRows();
                }

            } catch (RemoteException ex) {
                handleError(URLyBirdErrors.NETWORK_ERROR, ex);
            } catch (RecordNotFoundException ex) {
                handleError(URLyBirdErrors.RECORD_NOT_FOUND, ex);
            } catch (StaleRecordException ex) {
                handleError(URLyBirdErrors.STALE_RECORD, ex);
            } catch (RecordNotModifiableException ex) {
                handleError(URLyBirdErrors.RECORD_NOT_MODIFIABLE, ex);
            } catch (ServerNotRunningException ex) {
                handleError(URLyBirdErrors.SERVER_NOT_RUNNING, ex);
            } catch (Exception ex) {
                handleError(URLyBirdErrors.UNEXPECTED_EXCEPTION, ex);
            } finally {
                view.setCursor(WidgetFactory.DEFAULT_CURSOR);
            }
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
     * The MVC view.
     */
    private ClientView view;

    /**
     * The MVC model.
     */
    private ClientModel model;

    /**
     * The currently selected record.
     * If there is no record selected, this will be null.
     */
    private Record currentRecord;

    /**
     * Connection for server to use to reach us.
     */
    private ServerToClientConnection connection;

    /**
     * Our connection to the server.
     */
    private ClientToServerConnection serverConnection;

    /**
     * Networking support. Only used in network client mode.
     */
    private final NetworkServer networkSupport;

    /**
     * The policy that the server uses to determine
     * whether or not a record can be modified.
     */
    private RecordModificationPolicy policy;

    /**
     * The port to use for networking.
     */
    private final int port;

    /**
     * True if we are running in standalone mode.
     * False if in network client mode.
     */
    private final boolean isStandalone;

    /**
     * Dialog box for entering server details.
     * If running in network client
     * mode, this is a dialog box for entering
     * the server network address.
     * If running in standalone mode,
     * this is a dialog box for entering the
     * filename of the database file to open.
     */
    private AbstractInputDialog connectionDialog;

    /**
     * Progress indicator to display the progress of/
     * opening the database file when in standalone mode.
     */
    private ProgressDialog progressDialog;

    /**
     * Dialog box used to display a record and allow the user to edit the
     * modifiable fields of the record.
     */
    private ModifyRecordPanel modifyPanel;

    /**
     * Action to connect to a server.
     * If running in standalone mode, the user
     * will be prompted to enter the
     * filename of the database file to open.
     */
    private final Action connectAction;

    /**
     * Action to disconnect from the server that this
     * client is currently connected to.
     * If running in network client mode, we send a disconnect
     * message to the server.
     */
    private final Action disconnectAction;

    /**
     * Action to select all the rows in the search results table.
     */
    private final Action selectAllRowsAction = new SelectAllRowsAction();

    /**
     * Action to exit the program.
     */
    private final Action exitAction = new ExitAction();

    /**
     * Action to search for records that match the criteria entered by the user
     * in the search fields. This action can take some time, so we allow the
     * user to cancel it while it is running.
     */
    private final CancellableAction searchAction = new SearchAction();

    /**
     * Action to cancel the current search.
     * This action is only enabled if there is a search running.
     */
    private final Action cancelSearchAction = new CancelSearchAction();

    /**
     * Action to modify the currently selected record.
     */
    private final Action modifyRecordAction = new ModifyRecordAction();

    /**
     * Action to display the About dialog box.
     */
    private final Action helpAboutAction = new HelpAboutAction();

    /**
     * Action to display the help dialog.
     */
    private final Action helpContentsAction = new HelpAction();

    /**
     * The difference, in milliseconds, between the
     * server's date and our date, i.e. server date minus our date.
     */
    private long dateDifference;

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify.client");

    /**
     * Create a new controller running in network mode.
     *
     * @param port the port to use for networking.
     */
    public ClientController(final int port) {
        this(port, null);
    }

    /**
     * Create a new controller using a standalone connection.
     *
     * @param serverConnection the existing connection to the server.
     */
    public ClientController(final ClientToServerConnection serverConnection) {
        this(0, serverConnection);
    }

    /**
     * Create a new client controller.
     *
     * @param port the port to use for networking.
     *        Ignored if running in standalone mode.
     * @param serverConnection connection to the server.
     *        If null, the controller will be in network client mode.
     *        If not null, the controller will be in standalone mode.
     */
    private ClientController(final int port,
            final ClientToServerConnection serverConnection) {
        this.port = port;
        this.isStandalone = (serverConnection != null);

        connectAction = new ConnectAction();
        disconnectAction = new DisconnectAction();

        view = new ClientView(isStandalone);
        model = new ClientModel();

        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                handleExit();
            }
        });

        if (isStandalone) {
            this.serverConnection = serverConnection;
            connection = new StandaloneServerToClientConnection();
            networkSupport = null;
            createAndShowGUI();
        } else {
            networkSupport = new RMINetworkServer();
            createAndShowGUI();
            try {
                connection = new RMIServerToClientConnection(this);
            } catch (RemoteException e) {
                handleError(URLyBirdErrors.NETWORK_ERROR, e);
            }
        }
    }

    /** {@inheritDoc} */
    public void showWarning(final String message) {
        view.showWarning(message);
    }

    /** {@inheritDoc} */
    public void serverStopping() {
        disconnect(true);
    }

    /** {@inheritDoc} */
    public void rowSelected(final Integer recordNumber) {
        final boolean enableBookAction;

        if (recordNumber != null) {
            currentRecord = model.getRecord(recordNumber);

            final TableSchema schema = model.getCurrentSchema();

            if (schema == null || policy == null) {
                enableBookAction = true;
            } else {
                final Date now = new Date();
                final Date estimatedServerDate = new Date(now.getTime()
                        + dateDifference);

                enableBookAction = policy.isRecordModifiable(
                        currentRecord.getFieldList(), estimatedServerDate);
            }

        } else {
            currentRecord = null;
            enableBookAction = false;
        }

        //Enable or disable book action accordingly
        modifyRecordAction.setEnabled(enableBookAction);
    }

    /**
     * Disconnect from the server.
     *
     * @param isConnectionLost true if the server connection
     *        has been lost (due to the server shutting down etc.)
     */
    private void disconnect(final boolean isConnectionLost) {
        disconnectAction.setEnabled(false);
        connectAction.setEnabled(true);

        try {
            if (!isConnectionLost) {
                if (serverConnection != null) {
                    serverConnection.disconnectFromServer(connection);
                }
            }
        } catch (RemoteException e) {
            handleError(URLyBirdErrors.NETWORK_ERROR, e);
        }

        if (!isStandalone) {
            serverConnection = null;
        }

        model.setSchema(null);
        view.addStatusMessage(Localization.getString("client.disconnected"));

        if (isConnectionLost) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showWarning(
                            Localization.getString("client.serverShutDown"));
                }
            });
        }
    }

    /**
     * The user wants to exit.
     * We try to send a disconnect message to the server before exiting.
     */
    private void handleExit() {
        LOGGER.fine("Client mode exiting");

        try {
            if (serverConnection != null) {
                serverConnection.disconnectFromServer(connection);
            }
        } catch (Exception e) {
            LOGGER.warning("Could not disconnect from server while exiting, "
                    + e);
        }
        serverConnection = null;
        connection = null;

        view.dispose();
        System.exit(0);
    }

    /**
     * Handle an error by logging it and displaying an
     * error message dialog box to the user.
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
     * Create the user interface and display it.
     */
    private void createAndShowGUI() {

        progressDialog = new ProgressDialog(
                view, Localization.getString("dialog.openingFile"));
        progressDialog.setTitle(Localization.getString(
                "dialog.openingFileTitle",
                Localization.getString("programName")));

        //Listen to row selection changes
        view.addSelectionListener(this);

        if (isStandalone) {
            connectionDialog = new FileOpenDialog(
                view,
                Localization.getString("dialog.openFileTitle",
                    Localization.getString("programName")),
                    Localization.getString("databaseFileFilter.extension"));
        } else {
            connectionDialog = new RMIAddressDialog(view);
        }

        disconnectAction.setEnabled(false);
        cancelSearchAction.setEnabled(false);
        addActionHandlers();

        if (!isStandalone) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    disconnectAction.actionPerformed(null);
                }
            });
        }

        model.addPropertyChangeListener(view);
        view.showGUI();
    }

    /**
     * Associate the controller actions with the
     * command options provided by the view.
     */
    private void addActionHandlers() {
        currentRecord = null;

        //The book action gets enabled when a bookable room is selected
        modifyRecordAction.setEnabled(false);

        modifyPanel = new ModifyRecordPanel();

        view.getToolbar().setAction(ClientToolbar.Commands.CONNECT,
                connectAction);
        view.getMenu().setAction(ClientMenuBar.Commands.CONNECT,
                connectAction);

        view.getToolbar().setAction(ClientToolbar.Commands.DISCONNECT,
                disconnectAction);
        view.getMenu().setAction(ClientMenuBar.Commands.DISCONNECT,
                disconnectAction);

        view.setAction(ClientView.Commands.SEARCH,
                searchAction);

        view.setAction(ClientView.Commands.CANCEL_SEARCH,
                cancelSearchAction);

        view.setAction(ClientView.Commands.BOOK,
                modifyRecordAction);

        view.getTable().setAction(SearchResultsTable.Commands.BOOK,
                modifyRecordAction);
        view.getTable().setAction(SearchResultsTable.Commands.SELECT_ALL,
                selectAllRowsAction);

        view.getTable().setAction(SearchResultsTable.Commands.COPY,
                EditManager.getCopyAction());

        view.getMenu().setAction(ClientMenuBar.Commands.CUT,
                EditManager.getCutAction());

        view.getMenu().setAction(ClientMenuBar.Commands.COPY,
                EditManager.getCopyAction());

        view.getMenu().setAction(ClientMenuBar.Commands.PASTE,
                EditManager.getPasteAction());

        view.getMenu().setAction(ClientMenuBar.Commands.EXIT,
                exitAction);

        view.getMenu().setAction(ClientMenuBar.Commands.HELP_ABOUT,
                helpAboutAction);

        view.getToolbar().setAction(ClientToolbar.Commands.HELP,
                helpContentsAction);
        view.getMenu().setAction(ClientMenuBar.Commands.HELP,
                helpContentsAction);

    }

}
