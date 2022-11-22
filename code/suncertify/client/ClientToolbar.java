package suncertify.client;

import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * Toolbar for URLyBird client GUI.
 *
 * @author Robert Mollard
 */
final class ClientToolbar extends JToolBar {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 4022282034657678129L;

    /**
     * The commands provided by this toolbar.
     */
    enum Commands {

        /**
         * Connect to an existing server.
         */
        CONNECT,

        /**
         * Disconnect from the server. If the client is not connected to a
         * server, this command should be disabled.
         */
        DISCONNECT,

        /**
         * Display the help dialog.
         */
        HELP;
    }

    /**
     * A map of the Commands in the menu bar.
     */
    private Map<Commands, JButton> buttons;

    /**
     * Construct a new client toolbar and add a button for each command.
     */
    ClientToolbar() {
        buttons = new HashMap<Commands, JButton>();
        setLayout(new FlowLayout(FlowLayout.LEFT));

        //Show button borders on mouseover
        setRollover(true);

        //Don't let user drag it
        setFloatable(false);

        add(createButton(Commands.CONNECT));
        add(createButton(Commands.DISCONNECT));

        addSeparator();

        add(createButton(Commands.HELP));
    }

    /**
     * Create a button for the given command,
     * and add it to the list of buttons.
     *
     * @param command the command for the button
     * @return a new JButton
     */
    private JButton createButton(final Commands command) {
        JButton button = new JButton();
        button.setFocusable(false);
        buttons.put(command, button);
        return button;
    }

    /**
     * Associate the given action with the given command.
     *
     * @param command the command to configure
     * @param action the action to be performed
     */
    void setAction(final Commands command, final Action action) {
        final JButton button = buttons.get(command);
        if (button != null) {
            button.setAction(action);
        } else {
            assert false : "Could not set action for command: " + command;
        }
    }

}
