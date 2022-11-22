package suncertify.panels;

import java.awt.Component;
import java.awt.GridLayout;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.text.Document;

import suncertify.Criterion;
import suncertify.fields.Field;
import suncertify.fields.parsers.FieldParser;
import suncertify.utils.SmartComboBox;

/**
 * Our standard input widget for search text: an editable
 * combo box that performs some predictive
 * completion as the user types.
 *
 * @author Robert Mollard
 */
public class GenericEntryField extends JPanel implements FieldInputComponent {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 5585887516318554043L;

    /**
     * A combo box for selecting strings to search for.
     */
    private final JComboBox combo;

    /**
     * Decorator for the combo box.
     */
    private final SmartComboBox smart;

    /**
     * The parser to convert the value the user entered
     * into a <code>Field</code> value.
     */
    private final FieldParser parser;

    /**
     * Property change listener list.
     */
    private List<PropertyChangeListener> listeners;

    /**
     * Filter to restrict input characters.
     */
    private Document filter = null;

    /**
     * Construct a GenericEntryField with automatic completion.
     *
     * @param isEditable true if user is allowed to enter values
     * @param parser the parser used to interpret the user's input
     */
    public GenericEntryField(
            final boolean isEditable, final FieldParser parser) {
        this.parser = parser;
        this.combo = new JComboBox();
        this.listeners = new ArrayList<PropertyChangeListener>();

        combo.setEditable(isEditable);
        smart = new SmartComboBox(combo);
        combo.setSelectedItem(null);

        //Prevent resizing
        combo.setPreferredSize(combo.getPreferredSize());
        combo.setMinimumSize(combo.getPreferredSize());
        combo.setMaximumSize(combo.getPreferredSize());

        this.setLayout(new GridLayout(0, 1));
        this.add(combo);
    }

    /** {@inheritDoc} */
    public final FieldInputComponent createClone() {
        GenericEntryField result = new GenericEntryField(combo.isEditable(),
                this.parser);
        for (PropertyChangeListener p : listeners) {
            result.addListener(p);
        }
        if (filter != null) {
            result.setDocumentFilter(filter);
        }
        return result;
    }

    /** {@inheritDoc} */
    public final Component getComponent() {
        return this;
    }

    /** {@inheritDoc} */
    public final Criterion getCriterion() {
        final Criterion result;
        final Field myField;

        if (combo.getSelectedItem() == null
                || combo.getSelectedItem().toString() == null
                || combo.getSelectedItem().toString().equals("")) {
            result = null;
        } else {
            myField = parser.valueOf(combo.getSelectedItem().toString());
            result = new Criterion(myField);
        }
        return result;
    }

    /** {@inheritDoc} */
    public final void searchPerformed() {
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
    public final void addListener(final PropertyChangeListener listener) {
        listeners.add(listener);
        smart.addPropertyChangeListener(listener);
    }

    /** {@inheritDoc} */
    public final void removeListener(final PropertyChangeListener listener) {
        listeners.remove(listener);
        smart.removePropertyChangeListener(listener);
    }

    /**
     * Set a document filter to restrict the characters that can
     * be entered. For example, a filter could allow only digits
     * to be entered.
     *
     * @param filter the new <code>Document</code> to use as the filter.
     *        Can be null, in which case it will be ignored.
     */
    public final void setDocumentFilter(final Document filter) {
        this.filter = filter;
        smart.setDocumentFilter(filter);
    }

}
