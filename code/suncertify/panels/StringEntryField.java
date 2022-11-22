package suncertify.panels;

import java.awt.Component;
import java.awt.GridLayout;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import suncertify.Criterion;
import suncertify.StringMatchCriterion;
import suncertify.fields.StringField;
import suncertify.utils.EditManager;
import suncertify.utils.Localization;
import suncertify.utils.SmartComboBox;

/**
 * Our standard input widget for string search fields:
 * an editable combo box that shows a list of matching
 * items as the user types.
 * There are also checkboxes to enable the user to perform
 * a case-sensitive or whole phrase match.
 *
 * @author Robert Mollard
 */
public final class StringEntryField 
    extends JPanel implements FieldInputComponent {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -3565740952518061422L;

    /**
     * An combo box for selecting strings to search for.
     * May or may not be editable depending on the
     * <code>isEditable</code> argument to the constructor.
     */
    private final JComboBox combo;

    /**
     * Wrapper for the combo box to provide a list
     * of partial matches.
     */
    private final SmartComboBox smartCombo;

    /**
     * If checked, string match will be case sensitive.
     */
    private final JCheckBox caseSensitive;

    /**
     * If checked, string match will try to match exact phrase.
     * If unchecked, substring match will be done.
     */
    private final JCheckBox wholeWord;

    /**
     * The list of listeners for this widget.
     */
    private List<PropertyChangeListener> listeners;

    /**
     * Construct a <code>StringEntryField</code>
     * with automatic completion.
     *
     * @param isEditable true if user is allowed to enter values
     */
    public StringEntryField(final boolean isEditable) {

        combo = new JComboBox();
        listeners = new ArrayList<PropertyChangeListener>();

        //Prevent resizing
        combo.setPreferredSize(combo.getPreferredSize());
        combo.setMinimumSize(combo.getPreferredSize());
        combo.setMaximumSize(combo.getPreferredSize());

        caseSensitive = new JCheckBox(
            Localization.getString("client.search.caseSensitive"));
        wholeWord = new JCheckBox(
            Localization.getString("client.search.wholeWord"));

        combo.setEditable(isEditable);

        caseSensitive.setToolTipText(
            Localization.getString("client.search.caseSensitiveToolTipText"));

        wholeWord.setToolTipText(
            Localization.getString("client.search.wholeWordToolTipText"));

        caseSensitive.setFocusable(false);
        wholeWord.setFocusable(false);

        smartCombo = new SmartComboBox(combo);
        combo.setSelectedItem(null);

        setLayout(new GridLayout(0, 1));
        add(combo);
        add(caseSensitive);
        add(wholeWord);

        Component editor = combo.getEditor().getEditorComponent();
        if (editor instanceof JTextComponent) {
            JTextComponent textComponent = (JTextComponent) editor;

            EditManager.registerComponentForEditActions(textComponent);
        }
    }

    /** {@inheritDoc} */
    public FieldInputComponent createClone() {
        StringEntryField result = new StringEntryField(combo.isEditable());
        for (PropertyChangeListener p : listeners) {
            result.addListener(p);
        }
        return result;
    }

    /** {@inheritDoc} */
    public Component getComponent() {
        return this;
    }

    /** {@inheritDoc} */
    public Criterion getCriterion() {
        final Criterion result;
        final StringField myField;

        final String selectString;
        if (combo.getSelectedItem() == null) {
            selectString = "";
        } else {
            selectString = combo.getSelectedItem().toString();
        }

        myField = new StringField(selectString);

        result = new StringMatchCriterion(myField, wholeWord.isSelected(),
                caseSensitive.isSelected());

        return result;
    }

    /** {@inheritDoc} */
    public void searchPerformed() {
        boolean alreadyInList = false;

        //Add the item to the combo box list but don't have allow duplicates.
        if (combo.getSelectedItem() != null) {
            String item = combo.getSelectedItem().toString();
            final String trimmed = item.trim();

            if (!"".equals(trimmed)) {

                //Linear search for the item
                for (int i = 0; i < combo.getItemCount(); i++) {
                    if (item.equals(combo.getItemAt(i))) {
                        alreadyInList = true;
                    }
                }

                //Don't add whitespace to the list
                if (!alreadyInList) {
                    combo.addItem(item);
                }
            }
        }
    }

    /** {@inheritDoc} */
    public void addListener(final PropertyChangeListener listener) {
        listeners.add(listener);
        smartCombo.addPropertyChangeListener(listener);
    }

    /** {@inheritDoc} */
    public void removeListener(final PropertyChangeListener listener) {
        listeners.remove(listener);
        smartCombo.removePropertyChangeListener(listener);
    }

}
