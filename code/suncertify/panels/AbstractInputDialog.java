package suncertify.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import suncertify.PersistentConfiguration;
import suncertify.SavedParameter;
import suncertify.utils.Localization;

/**
 * A modal dialog box that dynamically enables and
 * disables the OK button depending on
 * the value entered by the user.
 * The subclass will have to do the GUI layout.
 *
 * Thread safety: not thread safe
 *
 * @author Robert Mollard
 */
public abstract class AbstractInputDialog extends JDialog {

    /**
     * The OK button.
     * In order to prevent invalid data being accepted, this is
     * dynamically enabled and disabled as the user types.
     */
    private final JButton okButton;

    /**
     * The Cancel button.
     */
    private final JButton cancelButton;

    /**
     * A label showing an error message.
     */
    private final JLabel errorLabel;

    /**
     * Editable combo box for entering the input.
     */
    private JComboBox combo;

    /**
     * Gets set to true if the user presses OK.
     */
    private boolean accepted = false;

    /**
     * The parameter associated with the input field.
     */
    private final SavedParameter parameterName;

    /**
     * The editor text when the dialog was displayed.
     */
    private String originalEditorText;

    /**
     * Get a localized description of the input.
     *
     * @return a localized string describing the input value
     */
    public abstract String getDescription();

    /**
     * Get the localized text to put on the error label.
     *
     * @return text for the error label
     */
    protected abstract String getErrorLabelText();

    /**
     * Check that the given input is valid. This method is
     * called whenever the input text changes.
     *
     * @param input the input to verify
     * @return true if the input is valid
     */
    protected abstract boolean isValidInput(String input);

    /**
     * Construct a new <code>AbstractInputDialog</code>.
     *
     * @param owner the <code>Frame</code> from which
     *        the dialog is displayed
     * @param title the <code>String</code> to display
     *        in the dialog's title bar
     * @param savedParameterName the parameter associated with
     *        the input field. The input field value will
     *        be saved when the OK button is pressed.
     */
    public AbstractInputDialog(final Frame owner, final String title,
            final SavedParameter savedParameterName) {
        super(owner, title, true);
        parameterName = savedParameterName;

        okButton = new JButton(
                Localization.getString("dialog.okButton"));
        cancelButton = new JButton(
                Localization.getString("dialog.cancelButton"));

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                accepted = true;
                saveCurrentItem();
                AbstractInputDialog.this.setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                accepted = false;
                AbstractInputDialog.this.setVisible(false);
            }
        });

        KeyListener buttonListener = new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelButton.doClick();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    okButton.doClick();
                }
            }
        };

        okButton.addKeyListener(buttonListener);
        /*
         * Note that pressing enter when the cancel button has focus
         * still selects the OK button. This is consistent
         * with the behaviour of JOptionPane dialogs.
         */
        cancelButton.addKeyListener(buttonListener);

        errorLabel = new JLabel(getErrorLabelText());
        errorLabel.setForeground(Color.RED);

        errorLabel.setMinimumSize(errorLabel.getPreferredSize());

        combo = new JComboBox();

        Component editorComponent = combo.getEditor().getEditorComponent();

        if (editorComponent instanceof JTextComponent) {
            final JTextComponent editor = (JTextComponent) editorComponent;
            editor.getDocument().addDocumentListener(new DocumentListener() {

                public void insertUpdate(final DocumentEvent e) {
                    checkInput(editor.getText());
                }

                public void removeUpdate(final DocumentEvent e) {
                    checkInput(editor.getText());
                }

                public void changedUpdate(final DocumentEvent e) {
                    checkInput(editor.getText());
                }
            });
        }
    }

    /**
     * Check the user input. We disable the OK
     * button and show the error label if the input
     * is not valid.
     *
     * @param input filename of the file to test
     */
    private void checkInput(final String input) {

        if (isValidInput(input)) {
            //Hide the label without changing layout
            errorLabel.setText(" ");
            okButton.setEnabled(true);
        } else {
            errorLabel.setText(getErrorLabelText());
            okButton.setEnabled(false);
        }
    }

    /**
     * Get the string value entered by the user.
     *
     * @return the input string
     */
    public final String getInputString() {
        return combo.getSelectedItem().toString();
    }

    /**
     * Display the dialog box and block until the user closes it.
     *
     * @param view the parent frame
     * @return true if user pressed OK, or false if cancelled
     */
    public final boolean showDialog(final JFrame view) {
        accepted = false;
        if (view != null) {
            setLocationRelativeTo(view);
        }

        if (originalEditorText != null) {
            restoreOriginalText();
        }

        setVisible(true);
        return accepted;
    }

    /**
     * Get a reference to the OK button.
     *
     * @return the OK button
     */
    protected final JButton getOkButton() {
        return okButton;
    }

    /**
     * Get a reference to the Cancel button.
     *
     * @return the Cancel button
     */
    protected final JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Get a reference to the combo box.
     *
     * @return the combo box
     */
    protected final JComboBox getCombo() {
        return combo;
    }

    /**
     * Get a reference to the error label.
     *
     * @return the error label
     */
    protected final JLabel getErrorLabel() {
        return errorLabel;
    }

    /**
     * Add the given text to the combo box and select it.
     *
     * @param text the text to enter
     */
    protected final void setInitialText(final String text) {
        combo.addItem(text);
        combo.setSelectedIndex(0);

        originalEditorText = text;
        checkInput(text);
    }

    /**
     * Set the combo box editor text to what it was when the
     * input dialog was displayed.
     */
    private void restoreOriginalText() {
        Component editorComponent = combo.getEditor().getEditorComponent();
        if (editorComponent instanceof JTextComponent) {
            JTextComponent editor = (JTextComponent) editorComponent;
            editor.setText(originalEditorText);
        }
    }

    /**
     * Save the current item to the configuration file, and add the item to the
     * combo box's menu list unless already present.
     */
    private void saveCurrentItem() {
        //Add item to history if not already present
        boolean alreadyInList = false;

        Component editorComponent = combo.getEditor().getEditorComponent();

        if (editorComponent instanceof JTextComponent) {
            final JTextComponent editor = (JTextComponent) editorComponent;
            final String item = editor.getText();

            for (int i = 0; i < combo.getItemCount(); i++) {
                if (item.equals(combo.getItemAt(i))) {
                    alreadyInList = true;
                }
            }

            if (!alreadyInList) {
                combo.addItem(item);
            }

            originalEditorText = item;

            //Save filename in the properties file.
            PersistentConfiguration.getInstance().setParameter(
                                parameterName, item);
        }
    }

}
