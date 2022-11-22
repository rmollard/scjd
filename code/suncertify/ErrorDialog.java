package suncertify;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import suncertify.utils.Localization;

/**
 * Utility class for displaying error dialog boxes.
 *
 * @author Robert Mollard
 */
public final class ErrorDialog {

    /**
     * Private constructor to prevent instantiation.
     */
    private ErrorDialog() {
        //Prevent instantiation
    }

    /**
     * Creates a modal dialog to show an error message. If <code>cause</code>
     * is not null, the user can click on the "Details" button to get a full
     * stack trace, including any nested errors (the throwables in the "caused
     * by" chain).
     *
     * @param parent the parent component to be
     *        used for the dialog (may be null)
     * @param title the title of the error message dialog box
     * @param message the message to be displayed in the dialog
     * @param cause the throwable that caused the error (may be null)
     */
    public static void showErrorDialog(final Component parent,
            final String title, final Object message, final Throwable cause) {
        final JButton details = new JButton(Localization.getString(
                        "errorDialog.detailsButtonText"));
        details.setEnabled(cause != null);

        //Add a listener that will display the details dialog box
        details.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                final Container ancenstor = SwingUtilities.getAncestorOfClass(
                        JDialog.class, details);
                if (ancenstor instanceof JDialog) {
                    JDialog owner = (JDialog) ancenstor;
                    ErrorDetailsDialog dialog =
                        new ErrorDetailsDialog(owner, cause);
                    dialog.setDefaultCloseOperation(
                            WindowConstants.DISPOSE_ON_CLOSE);
                    dialog.pack();
                    dialog.setLocationRelativeTo(owner);
                    dialog.setVisible(true);
                }
            }
        });

        final Object[] buttons =
            {Localization.getString("dialog.okButton"), details};
        JOptionPane.showOptionDialog(parent, message, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                buttons, buttons[0]);
    }

    /**
     * A separate dialog box to show the stack trace details. We traverse the
     * chain of throwables until we find the root cause, creating a tabbed pane
     * showing the stack trace for each throwable.
     */
    private static final class ErrorDetailsDialog extends JDialog {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = 1059907309123240007L;

        /**
         * The width of the scroll pane that contains the error details, as a
         * fraction of the screen width.
         */
        private static final double WIDTH_MULTIPLIER = 0.4;

        /**
         * The height of the scroll pane that contains the error details, as a
         * fraction of the screen width.
         */
        private static final double HEIGHT_MULTIPLIER = 0.4;

        /**
         * The button to close the details dialog. Note that this closes the
         * details dialog but not the error dialog.
         */
        private final JButton closeButton = new JButton(Localization
                .getString("dialog.closeButton")) {

            /**
             * Default generated version number for serialization.
             */
            private static final long serialVersionUID = 7678144761304293940L;

            @Override
            public void fireActionPerformed(final ActionEvent event) {
                dispose();
            }
        };

        /**
         * Simple key adapter for the close button.
         */
        private static final class CloseButtonListener extends KeyAdapter {

            /**
             * Button to add the listener to.
             */
            private final JButton button;

            /**
             * Create a close listener for the given button.
             *
             * @param button the button to add the key listener to
             */
            private CloseButtonListener(final JButton button) {
                this.button = button;
            }

            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    button.doClick();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    button.doClick();
                }
            }
        }

        /**
         * Create a dialog box to display error details.
         *
         * @param parent the parent component to be used
         *        for the dialog (may be null)
         * @param detail the <code>Throwable</code> that caused the error.
         *        Must not be null.
         */
        private ErrorDetailsDialog(
                final JDialog parent, final Throwable detail) {
            super(parent, Localization.getString("errorDialog.detailsTitle",
                    Localization.getString("programName")));

            final Dimension screenSize =
                Toolkit.getDefaultToolkit().getScreenSize();

            JTabbedPane tabs = null;
            Throwable problem = detail;

            final KeyListener escapeListener = new KeyAdapter() {
                @Override
                public void keyPressed(final KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        closeButton.doClick();
                    }
                }
            };

            /*
             * Look at all the nested error, creating a tab for each throwable.
             * If there is no cause, don't create tabs.
             */
            while (problem != null) {
                JTextArea text = new JTextArea();
                text.append(problem + "\n");
                StackTraceElement[] trace = problem.getStackTrace();
                //Display the stack trace text for the current problem
                for (int i = 0; i < trace.length; i++) {
                    text.append("    " + trace[i] + "\n");
                }
                JScrollPane scroller = new JScrollPane(text);

                text.addKeyListener(escapeListener);
                text.setCaretPosition(0);
                text.setEditable(false);

                scroller.setPreferredSize(new Dimension(
                        (int) (screenSize.width * WIDTH_MULTIPLIER),
                        (int) (screenSize.height * HEIGHT_MULTIPLIER)));

                problem = problem.getCause();

                if (tabs == null) {
                    tabs = new JTabbedPane();
                    tabs.addTab(Localization.getString(
                            "errorDialog.exceptionTabTitle"), scroller);
                } else {
                    tabs.addTab(Localization.getString(
                            "errorDialog.causedByTabTitle"), scroller);
                }
            }

            KeyListener buttonListener = new CloseButtonListener(closeButton);
            closeButton.addKeyListener(buttonListener);

            final JPanel panel = new JPanel();
            panel.add(closeButton);
            getContentPane().add(tabs, BorderLayout.CENTER);
            getContentPane().add(panel, BorderLayout.SOUTH);
        }

        /**
         * Show or hide the dialog based on the value
         * of the given parameter.
         *
         * @param visible true to set visible, or false to set invisible
         */
        @Override
        public void setVisible(final boolean visible) {
            super.setVisible(visible);
            closeButton.requestFocusInWindow();
        }
    }

}
