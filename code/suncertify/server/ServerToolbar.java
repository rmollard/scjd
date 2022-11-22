package suncertify.server;

import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * Toolbar for URLyBird server GUI.
 *
 * @author Robert Mollard
 */
final class ServerToolbar extends JToolBar {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -8546897961193463785L;

    /**
     * The commands provided by this toolbar.
     */
    enum Commands {

        /**
         * Start the server and enable clients to connect
         * to the server.
         */
        START_SERVER,

        /**
         * Stop the server. Any clients connected to
         * the server will be disconnected.
         */
        STOP_SERVER,

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
     * Construct a new server toolbar and add a button
     * for each command.
     */
    ServerToolbar() {
        buttons = new HashMap<Commands, JButton>();
        setLayout(new FlowLayout(FlowLayout.LEFT));

        //Show button borders on mouseover
        setRollover(true);
        setFloatable(false);

        add(createButton(Commands.START_SERVER));
        add(createButton(Commands.STOP_SERVER));

        addSeparator();

        add(createButton(Commands.HELP));
    }

    /**
     * Create a button for the given command, and add it to
     * the list of buttons.
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
