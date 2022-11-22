package suncertify.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import suncertify.IconFactory;
import suncertify.ServerToClientConnection;
import suncertify.View;
import suncertify.WidgetFactory;
import suncertify.utils.EditManager;
import suncertify.utils.Localization;

/**
 * Main application GUI for URLyBird application when running in
 * "server" mode.
 *
 * Thread safety: not thread safe.
 *
 * @author Robert Mollard
 */
final class ServerView extends View {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 5789316494543762196L;

    /**
     * The commands associated with the server event log.
     */
    enum Commands {

        /**
         * Select all text in the event log.
         */
        SELECT_ALL,

        /**
         * Copy the selected text in the event log
         * to the clipboard.
         */
        COPY,
    }

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify.server");

    /**
     * The color of the status label text when the server is ready.
     */
    private static final Color READY_COLOR = new Color(0xe28106);

    /**
     * The color of the status label text when the server is running.
     */
    private static final Color RUNNING_COLOR = new Color(0x37bd03);

    /**
     * Label displaying the current server status.
     */
    private final JLabel statusLabel;

    /**
     * The width of the main frame, as a fraction of the screen width.
     */
    private static final double WIDTH_MULTIPLIER = 0.5;

    /**
     * The height of the main frame, as a fraction of the screen height.
     */
    private static final double HEIGHT_MULTIPLIER = 0.6;

    /**
     * The initial delay for displaying tool tips, in milliseconds.
     */
    private static final int TOOL_TIP_DELAY = 200;

    /**
     * Text area for displaying events of interest.
     */
    private final JTextArea textArea;

    /**
     * The context menu for the event log text area.
     */
    private final JPopupMenu contextMenu;

    /**
     * Label displaying how many clients are connected.
     */
    private final JLabel clientCount;

    /**
     * The toolbar, contains buttons for some of the commands.
     */
    private final ServerToolbar toolBar;

    /**
     * The menu bar for the program when running in server mode.
     */
    private final ServerMenuBar menuBar;

    /**
     * Date format for formatting the date displayed for
     * each event log message.
     */
    private final DateFormat format = new SimpleDateFormat();

    /**
     * A map of the Commands in the popup menu.
     */
    private final Map<Commands, AbstractButton> menuActions =
        new HashMap<Commands, AbstractButton>();

    /**
     * The number of clients that are currently connected.
     */
    private int clients = 0;

    /**
     * Create a new server view.
     */
    ServerView() {

        //Show tool tips after a short delay
        ToolTipManager.sharedInstance().setInitialDelay(TOOL_TIP_DELAY);

        WidgetFactory.getInstance().configureForeground();
        WidgetFactory.getInstance().configureBackground();

        final Dimension screenSize =
            Toolkit.getDefaultToolkit().getScreenSize();

        setSize((int) (screenSize.width * WIDTH_MULTIPLIER),
                (int) (screenSize.height * HEIGHT_MULTIPLIER));

        setTitle(Localization.getString("titleText", new String[] {
                Localization.getString("server.serverMode"),
                Localization.getString("programName") }));

        textArea = new JTextArea();
        textArea.setEditable(false);

        EditManager.registerComponentForEditActions(textArea);

        contextMenu = new JPopupMenu();
        createPopupMenu();

        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        statusLabel = new JLabel(Localization.getString("server.ready"));
        statusLabel.setForeground(READY_COLOR);

        clientCount = new JLabel();
        clientCount.setText(
                Localization.getString("server.clientsConnected", clients));

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(0, 1));

        final Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);

        //Add some padding
        statusLabel.setBorder(emptyBorder);
        statusPanel.add(statusLabel);

        String ip;
        try {
            InetAddress address = InetAddress.getLocalHost();
            ip = Localization.getString("server.ipAddress",
                    address.getHostAddress());
        } catch (UnknownHostException e) {
            ip = Localization.getString("server.unknownIpAddress");
        }

        JLabel serverIP = new JLabel(ip);

        //Add some padding
        serverIP.setBorder(emptyBorder);
        statusPanel.add(serverIP);

        //Add some padding
        clientCount.setBorder(emptyBorder);
        statusPanel.add(clientCount);

        statusPanel.setBorder(BorderFactory.createCompoundBorder(emptyBorder,
                BorderFactory.createTitledBorder(
                        Localization.getString("server.statusTitle"))));

        textArea.setRows(4);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BorderLayout());

        toolBar = new ServerToolbar();
        getContentPane().add(toolBar, BorderLayout.NORTH);

        mainContainer.add(statusPanel, BorderLayout.NORTH);

        JScrollPane scroller = new JScrollPane(textArea);

        addEventMessage(Localization.getString("server.ready"));

        JPanel logPanel = new JPanel();
        logPanel.setLayout(new GridLayout(1, 1));
        logPanel.add(scroller);

        logPanel.setBorder(BorderFactory.createCompoundBorder(emptyBorder,
                BorderFactory.createTitledBorder(
                        Localization.getString("server.eventsTitle"))));

        mainContainer.add(logPanel);

        //Set title bar icon
        ImageIcon image = IconFactory.getImageIcon(Localization
                .getString("URLyBirdProgramIcon"));
        if (image != null) {
            setIconImage(image.getImage());
        }

        menuBar = new ServerMenuBar();
        setJMenuBar(menuBar);

        JScrollPane mainScroller = new JScrollPane(mainContainer);
        getContentPane().add(mainScroller, BorderLayout.CENTER);

        //We rely on the controller to clean up.
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    public void propertyChange(final PropertyChangeEvent evt) {

        if (ServerModel.SERVER_STARTED.equals(evt.getPropertyName())) {
            statusLabel.setForeground(RUNNING_COLOR);
            statusLabel.setText(Localization.getString(
                    "server.running", evt.getNewValue().toString()));

            addEventMessage(Localization.getString("server.log.serverStarted",
                    evt.getNewValue().toString()));
        } else if (ServerModel.SERVER_STOPPED.equals(evt.getPropertyName())) {
            statusLabel.setForeground(READY_COLOR);
            statusLabel.setText(Localization.getString("server.ready"));

            addEventMessage(Localization.getString("server.log.serverStopped"));
        } else if (ServerModel.CLIENT_ADDED.equals(evt.getPropertyName())) {

            if (evt.getNewValue() instanceof ServerToClientConnection) {
                clients++;
                clientCount.setText(Localization.getString(
                        "server.clientsConnected", clients));

                ServerToClientConnection connection =
                    (ServerToClientConnection) evt.getNewValue();
                try {
                    addEventMessage(Localization.getString(
                            "server.log.clientConnected",
                                connection.getDescription()));
                } catch (RemoteException e) {
                    LOGGER.warning("Could not get client description: " + e);
                }
            }
        } else if (ServerModel.CLIENT_REMOVED.equals(evt.getPropertyName())) {

            if (evt.getNewValue() instanceof ServerToClientConnection) {
                ServerToClientConnection connection = (
                        ServerToClientConnection) evt.getNewValue();
                clients--;
                clientCount.setText(Localization.getString(
                        "server.clientsConnected", clients));

                try {
                    addEventMessage(Localization.getString(
                            "server.log.clientDisconnected", connection
                                    .getDescription()));
                } catch (RemoteException e) {
                    LOGGER.warning("Could not get client description: " + e);
                }
            }
        } else {
            LOGGER.warning("Unknown event given to server view: "
                    + evt.getPropertyName() + ", Details: " + evt);
        }
    }

    /**
     * Associate the given action with the given
     * context menu command.
     *
     * @param command the command to configure
     * @param action the action to be performed
     */
    void setAction(final Commands command, final Action action) {
        final AbstractButton button = menuActions.get(command);
        if (button != null) {
            button.setAction(action);
        } else {
            LOGGER.severe("Could not find item for command: " + command);
            assert false : "Missing search table command: " + command;
        }
    }

    /**
     * Get a reference to the server menu bar.
     *
     * @return the server menu bar.
     */
    ServerMenuBar getMenu() {
        return menuBar;
    }

    /**
     * Get a reference to the server toolbar.
     *
     * @return the server toolbar.
     */
    ServerToolbar getToolbar() {
        return toolBar;
    }

    /**
     * Select all text in the server event log text area.
     */
    void selectAll() {
        textArea.selectAll();
    }

    /**
     * Populate the context menu for the event log
     * text area.
     */
    private void createPopupMenu() {

        JMenuItem selectAllItem = new JMenuItem();
        menuActions.put(Commands.SELECT_ALL, selectAllItem);
        contextMenu.add(selectAllItem);

        contextMenu.addSeparator();

        JMenuItem copyItem = new JMenuItem();
        menuActions.put(Commands.COPY, copyItem);
        contextMenu.add(copyItem);

        textArea.add(contextMenu);
    }

    /**
     * Add a message to the event log text area.
     * The text area will scroll down to the new message.
     *
     * @param message the message to add (already localized)
     */
    private void addEventMessage(final String message) {

        textArea.append(Localization.getString("server.eventMessage",
                new Object[] {format.format(new Date()), message}));

        //Scroll to end
        textArea.setCaretPosition(textArea.getText().length());
    }

}
