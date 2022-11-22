package suncertify;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.UIManager;

/**
 * Helper class for GUI widgets.
 *
 * @author Robert Mollard
 */
public final class WidgetFactory {

    /**
     * Simple key adapter to simulate a mouse click when enter is pressed.
     */
    private static final class EnterListener extends KeyAdapter {

        /**
         * The button to add the listener to.
         */
        private final JButton button;

        /**
         * Create a key adapter for the given button.
         *
         * @param theButton the button to add the listener to
         */
        private EnterListener(final JButton theButton) {
            button = theButton;
        }

        @Override
        public void keyTyped(final KeyEvent e) {
            //Pressing enter is the same as clicking
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                button.doClick();
            }
        }
    }

    /**
     * The mouse pointer to display when the program is busy.
     */
    public static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);

    /**
     * The normal mouse pointer to display.
     */
    public static final Cursor DEFAULT_CURSOR = new Cursor(
            Cursor.DEFAULT_CURSOR);

    /**
     * The foreground color to use (i.e. the text color).
     */
    public static final Color FOREGROUND = new Color(0x000000);

    /**
     * Light blue color to use for the background for panels and option panes.
     */
    public static final Color LIGHT_BACKGROUND = new Color(0xf6fbff);

    /**
     * Blue color used for menus, labels and checkboxes.
     */
    public static final Color BACKGROUND = new Color(0xccdee8);

    /**
     * The background color for tooltips (light yellow).
     */
    public static final Color TOOLTIP_COLOR = new Color(0xffffe1);

    /**
     * The singleton instance of this class.
     */
    private static WidgetFactory instance = new WidgetFactory();

    /**
     * Private constructor to prevent instantiation by clients.
     */
    private WidgetFactory() {
        //Empty
    }

    /**
     * Get the singleton instance of this class.
     *
     * @return the instance
     */
    public static WidgetFactory getInstance() {
        return instance;
    }

    /**
     * Configure foreground colors.
     */
    public void configureForeground() {
        UIManager.put("Menu.foreground", FOREGROUND);
        UIManager.put("MenuBar.foreground", FOREGROUND);
        UIManager.put("MenuBar.disabledForeground", FOREGROUND);
        UIManager.put("Menu.selectionForeground", FOREGROUND);
        UIManager.put("MenuItem.foreground", FOREGROUND);
        UIManager.put("MenuItem.selectionForeground", FOREGROUND);
        UIManager.put("Menu.opaque", Boolean.TRUE);
        UIManager.put("Button.foreground", FOREGROUND);
        UIManager.put("Panel.foreground", FOREGROUND);
        UIManager.put("Label.foreground", FOREGROUND);
        UIManager.put("OptionPane.foreground", FOREGROUND);
        UIManager.put("CheckBox.foreground", FOREGROUND);
        UIManager.put("ComboBox.foreground", FOREGROUND);
    }

    /**
     * Configure background colors.
     */
    public void configureBackground() {
        UIManager.put("MenuBar.background", BACKGROUND);
        UIManager.put("Menu.background", BACKGROUND);
        UIManager.put("MenuItem.background", BACKGROUND);
        UIManager.put("Panel.background", LIGHT_BACKGROUND);
        UIManager.put("Label.background", BACKGROUND);
        UIManager.put("OptionPane.background", LIGHT_BACKGROUND);
        UIManager.put("CheckBox.background", BACKGROUND);
        UIManager.put("ToolTip.background", TOOLTIP_COLOR);
    }

    /**
     * Creates a new button with an action listener that causes a click to be
     * simulated if "enter" is pressed when the button has focus.
     *
     * @return A new <code>JButton</code>
     */
    public JButton createButton() {
        final JButton button = new JButton();
        button.addKeyListener(new EnterListener(button));
        return button;
    }

}
