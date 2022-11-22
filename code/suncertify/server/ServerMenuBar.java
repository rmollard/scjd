package suncertify.server;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import suncertify.AbstractMenuBar;

/**
 * Menu bar for URLyBird server GUI.
 *
 * @author Robert Mollard
 */
class ServerMenuBar extends AbstractMenuBar<ServerMenuBar.Commands> {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -7620333132078177045L;

    /**
     * The commands contained by this menu bar.
     */
    public enum Commands {

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
         * Exit the program and disconnect and clients that
         * are connected.
         */
        EXIT,

        /**
         * Select all text in the event log text area.
         */
        SELECT_ALL,

        /**
         * Copy the selected text in the event log text area.
         */
        COPY,

        /**
         * Show help.
         */
        HELP,

        /**
         * Display the "about" dialog.
         */
        HELP_ABOUT,
    }

    /**
     * Construct a menu bar with file, edit and help menus.
     */
    ServerMenuBar() {
        addFileMenu();
        addEditMenu();
        addHelpMenu();
    }

    /**
     * Creates the File menu and adds it to the menu bar.
     */
    private void addFileMenu() {
        JMenu menu = createMenu("menu.fileMenu", "menu.fileMenuMnemonic");
        menu.add(createMenuItem(Commands.START_SERVER));
        menu.add(createMenuItem(Commands.STOP_SERVER));
        menu.addSeparator();
        menu.add(createMenuItem(Commands.EXIT));
        add(menu);
    }

    /**
     * Creates the Edit menu and adds it to the menu bar.
     */
    private void addEditMenu() {
        JMenu menu = createMenu("menu.editMenu", "menu.editMenuMnemonic");

        JMenuItem copyItem = createMenuItem(Commands.COPY);
        menu.add(copyItem);

        menu.addSeparator();

        JMenuItem selectAllItem = createMenuItem(Commands.SELECT_ALL);
        menu.add(selectAllItem);

        add(menu);
    }

    /**
     * Creates the Help menu and adds it to the menu bar.
     */
    private void addHelpMenu() {
        JMenu menu = createMenu("menu.helpMenu", "menu.helpMenuMnemonic");
        menu.add(createMenuItem(Commands.HELP));
        menu.addSeparator();
        menu.add(createMenuItem(Commands.HELP_ABOUT));
        add(menu);
    }

}
