package suncertify;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import suncertify.utils.Localization;

/**
 * Menu bar superclass for URLyBird GUI.
 *
 * @author Robert Mollard
 * @param <E> the command type associated with each menu bar button
 */
public abstract class AbstractMenuBar<E> extends JMenuBar {

    /**
     * A map of the Commands in the menu bar.
     */
    private Map<E, JMenuItem> menuItems;

    /**
     * Create a new menu bar.
     */
    public AbstractMenuBar() {
        menuItems = new HashMap<E, JMenuItem>();
    }

    /**
     * Creates a menu with a localized name.
     *
     * @param name the name of the menu (gets translated)
     * @param mnemonic the name of the mnemonic
     *        key for the menu (gets translated)
     * @return a new menu
     */
    protected final JMenu createMenu(
            final String name, final String mnemonic) {
        String translated = Localization.getString(name);
        JMenu menu = new JMenu(translated);

        String mnemonicString = Localization.getString(mnemonic);

        if (mnemonicString != null && mnemonicString.length() > 0) {
            menu.setMnemonic(mnemonicString.charAt(0));
        }
        return menu;
    }

    /**
     * Creates a menu item.
     * The <code>setAction</code> method should be called
     * to set up the item with a corresponding action.
     *
     * @param command the command to invoke
     * @return a menu item for the given command
     */
    protected final JMenuItem createMenuItem(final E command) {
        JMenuItem item = new JMenuItem("unset");
        menuItems.put(command, item);
        return item;
    }

    /**
     * Associate the given action with the given command.
     *
     * @param command the command to configure
     * @param action the action to be performed
     */
    public final void setAction(final E command, final Action action) {
        final JMenuItem item = menuItems.get(command);
        if (item != null) {
            item.setAction(action);
        } else {
            assert false
                : "Could not find menu item for command: " + command;
        }
    }

}
