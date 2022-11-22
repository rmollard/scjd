package suncertify.client;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import suncertify.AbstractMenuBar;

/**
 * Menu bar for URLyBird client GUI.
 *
 * @author Robert Mollard
 */
class ClientMenuBar extends AbstractMenuBar<ClientMenuBar.Commands> {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -671428257262016688L;

    /**
     * The commands contained by this menu bar.
     */
    public enum Commands {

        /**
         * Connect to a server.
         */
        CONNECT,

        /**
         * Disconnect from the server that the client is currently connected to.
         */
        DISCONNECT,

        /**
         * Exit the program. If the client is connected to a server, the client
         * will disconnect first.
         */
        EXIT,

        /**
         * Cut the selected text.
         */
        CUT,

        /**
         * Copy the selected text.
         */
        COPY,

        /**
         * Paste text from the clipboard.
         */
        PASTE,

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
     * Construct a menu bar with a file menu and a help menu.
     */
    ClientMenuBar() {
        addFileMenu();
        addEditMenu();
        addHelpMenu();
    }

    /**
     * Creates the File menu and adds it to the menu bar.
     */
    private void addFileMenu() {
        JMenu menu = createMenu("menu.fileMenu", "menu.fileMenuMnemonic");
        menu.add(createMenuItem(Commands.CONNECT));
        menu.add(createMenuItem(Commands.DISCONNECT));
        menu.addSeparator();
        menu.add(createMenuItem(Commands.EXIT));
        add(menu);
    }

    /**
     * Creates the Edit menu and adds it to the menu bar.
     */
    private void addEditMenu() {
        JMenu menu = createMenu("menu.editMenu", "menu.editMenuMnemonic");

        JMenuItem cutItem = createMenuItem(Commands.CUT);
        menu.add(cutItem);

        JMenuItem copyItem = createMenuItem(Commands.COPY);
        menu.add(copyItem);

        JMenuItem pasteItem = createMenuItem(Commands.PASTE);
        menu.add(pasteItem);

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
