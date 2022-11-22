package suncertify.panels;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import suncertify.PersistentConfiguration;
import suncertify.SavedParameter;
import suncertify.utils.Localization;
import suncertify.utils.SmartComboBox;

/**
 * A dialog box for entering an RMI
 * network address (or host name).
 * The address is entered in an editable combo box. The combo
 * box menu consists of previously entered addresses.
 * The OK button becomes disabled if the combo box editor
 * text is not a valid RMI address.
 *
 * Thread safety: not thread safe
 *
 * @author Robert Mollard
 */
public final class RMIAddressDialog extends AbstractInputDialog {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 4388734078983262896L;

    /**
     * The IP address to display if there is a problem loading
     * the saved IP address from the properties file.
     * This is the loopback address (the address of this computer).
     */
    private static final String DEFAULT_IP = "127.0.0.1";

    /**
     * Factor to increase the normal width of the combo box by.
     */
    private static final int WIDTH_MULTIPLIER = 9;

    /**
     * Autocomplete support for the combo box.
     */
    private final SmartComboBox smartCombo;

    /**
     * Construct a IP address dialog.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed
     */
    public RMIAddressDialog(final Frame owner) {
        super(owner, Localization.getString("client.addressDialogTitle"),
                SavedParameter.IP_ADDRESS);
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(1, 2, 1, 2);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;

        panel.add(getErrorLabel(), c);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;

        int oldHeight = getCombo().getPreferredSize().height;
        //Make the width wider
        int newWidth = getCombo().getPreferredSize().width * WIDTH_MULTIPLIER;
        getCombo().setPreferredSize(new Dimension(newWidth, oldHeight));

        smartCombo = new SmartComboBox(getCombo());
        smartCombo.setDocumentFilter(new RMIAddressDocumentFilter());

        smartCombo.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent evt) {
                if (SmartComboBox.ESCAPE_PROPERTY.equals(
                                evt.getPropertyName())) {
                    getCancelButton().doClick();
                } else if (SmartComboBox.ENTER_PROPERTY.equals(evt
                        .getPropertyName())) {
                    getOkButton().doClick();
                }
            }
        });

        panel.add(getCombo(), c);
        c.gridx++;

        String inputString = PersistentConfiguration.getInstance()
                .getParameter(SavedParameter.IP_ADDRESS);
        if (inputString == null) {
            inputString = DEFAULT_IP;
        }

        setInitialText(inputString);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(getOkButton());
        buttonPanel.add(getCancelButton());
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(buttonPanel, c);
        setResizable(false);

        final JOptionPane optionPane = new JOptionPane(panel,
                JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null,
                new Object[] {getOkButton(), getCancelButton()});

        setContentPane(optionPane);
        pack();

        getCombo().requestFocusInWindow();
    }

    /** {@inheritDoc} */
    @Override
    protected String getErrorLabelText() {
        return Localization.getString("dialog.connectLabel");
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return getInputString();
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isValidInput(final String input) {
        boolean isValid = true;
        try {
            URI uri = new URI("rmi://" + input);
            if (uri.getUserInfo() != null) {
                //No user info may be given in an RMI address
                isValid = false;
            }
        } catch (URISyntaxException e) {
            isValid = false;
        }
        return isValid;
    }

}













