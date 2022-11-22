package suncertify;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import suncertify.panels.HelpAboutPanel;
import suncertify.utils.Localization;

/**
 * Superclass for URLyBird MVC view classes.
 *
 * @author Robert Mollard
 */
public abstract class View extends JFrame implements PropertyChangeListener {

    /**
     * Help file viewer for the program.
     */
    private HelpPanel helpPanel = null;

    /**
     * The "About" panel for the program.
     */
    private HelpAboutPanel aboutPanel = null;

    /**
     * Default public constructor.
     */
    public View() {
        //Empty
    }

    /**
     * Display the help panel.
     */
    public final void showHelpPanel() {
        if (helpPanel == null) {
            helpPanel = new HelpPanel(this,
                        Localization.getString("help.contentsFilename"));
        }
        helpPanel.setVisible(true);
    }

    /**
     * Displays the help about panel.
     */
    public final void showAboutPanel() {
        if (aboutPanel == null) {
            aboutPanel = new HelpAboutPanel(this);
        }
        aboutPanel.setLocationRelativeTo(this);
        aboutPanel.setVisible(true);
    }

    /**
     * Show a warning message.
     *
     * @param message the warning message (already localized)
     */
    public final void showWarning(final String message) {
        JOptionPane.showMessageDialog(this, message,
                Localization.getString("warning",
                        Localization.getString("programName")),
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Show an error message.
     *
     * @param errorNumber the error number
     * @param message the warning message (already localized)
     * @param cause the exception (can be null)
     */
    public final void showError(final int errorNumber,
            final String message, final Throwable cause) {

        final String titleText = Localization.getString("errorTitle",
                "" + errorNumber, Localization.getString("programName"));
        ErrorDialog.showErrorDialog(this, titleText, message, cause);
    }

    /**
     * Displays the GUI in the middle of the screen.
     */
    public final void showGUI() {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                //Horizontally and vertically center on screen
                final Dimension screenSize =
                    Toolkit.getDefaultToolkit().getScreenSize();

                int x = (int) ((screenSize.getWidth() - getWidth()) / 2);
                int y = (int) ((screenSize.getHeight() - getHeight()) / 2);
                setLocation(x, y);

                setVisible(true);
            }
        });
    }

}
