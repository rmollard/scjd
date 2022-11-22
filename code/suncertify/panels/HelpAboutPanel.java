package suncertify.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import suncertify.IconFactory;
import suncertify.WidgetFactory;
import suncertify.utils.Localization;

/**
 * A simple "about" dialog box showing information about the program.
 * There is also a button to close the dialog.
 *
 * @author Robert Mollard
 */
public final class HelpAboutPanel extends JDialog {

    /**
     * Simple class to simulate a click on the close button
     * when escape or enter is pressed.
     */
    private static final class CloseButtonListener extends KeyAdapter {

        /**
         * The button to add the listener to.
         */
        private final JButton button;

        /**
         * Create a close button listener for the given button.
         *
         * @param button the button to add the listener to
         */
        private CloseButtonListener(final JButton button) {
            this.button = button;
        }

        @Override
        public void keyPressed(final KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE
                    || e.getKeyCode() == KeyEvent.VK_ENTER) {
                button.doClick();
            }
        }
    }

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -2921428371306127413L;

    /**
     * Construct an "about" panel with the given parent frame.
     *
     * @param parent the parent frame
     */
    public HelpAboutPanel(final JFrame parent) {

        super(parent, Localization.getString("helpAboutPanelTitle",
                Localization.getString("programName")), false);

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        //Text showing information about the program
        JTextArea text = new JTextArea(Localization.getString(
                "helpAboutMainText", Localization.getString("programName")));
        text.setBackground(WidgetFactory.LIGHT_BACKGROUND);
        text.setEditable(false);
        text.setFocusable(false);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(10, 10, 10, 10);
        add(text, constraints);

        //The program icon
        JLabel programIcon = new JLabel();
        ImageIcon icon = IconFactory.getImageIcon(Localization
                .getString("URLyBirdProgramIcon"));
        programIcon.setIcon(icon);

        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy++;
        add(programIcon, constraints);

        //Button to close the dialog in case we are running on
        //a platform that does not have a close button in the title bar
        final JButton closeButton =
            new JButton(Localization.getString("helpAboutCloseButtonText"));

        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setVisible(false);
            }
        });

        closeButton.addKeyListener(new CloseButtonListener(closeButton));

        constraints.gridx++;
        constraints.anchor = GridBagConstraints.LAST_LINE_END;
        add(closeButton, constraints);
        setResizable(false);
        pack();
    }

}
