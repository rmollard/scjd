package suncertify.panels;

import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import suncertify.Criterion;
import suncertify.fields.BooleanField;
import suncertify.fields.viewers.BooleanFieldView;
import suncertify.fields.viewers.FieldView;
import suncertify.utils.Localization;

/**
 * Our standard search field widget for booleans: an uneditable combo box.
 * The user can select true, false, or either.
 *
 * @author Robert Mollard
 */
public final class BooleanEntryField extends JComboBox implements
        FieldInputComponent {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 3163324122158546848L;

    /**
     * Boolean field viewer to enable us to get
     * localized display Strings.
     */
    private static final FieldView<BooleanField> VIEWER
        = new BooleanFieldView();

    /**
     * Localized String to display for the "true" option.
     */
    private final String trueString =
        VIEWER.getDisplayString(new BooleanField(true));

    /**
     * Localized String to display for the "false" option.
     */
    private final String falseString =
        VIEWER.getDisplayString(new BooleanField(false));

    /**
     * Localized String to display for the "either" option.
     */
    private final String eitherString =
        Localization.getString("client.search.boolean.either");

    /**
     * Construct an instance containing options for the user
     * to select true, false or either.
     */
    public BooleanEntryField() {
        this.addItem(eitherString);
        this.addItem(trueString);
        this.addItem(falseString);
    }

    /** {@inheritDoc} */
    public FieldInputComponent createClone() {
        return new BooleanEntryField();
    }

    /** {@inheritDoc} */
    public JComponent getComponent() {
        return this;
    }

    /** {@inheritDoc} */
    public Criterion getCriterion() {
        Criterion result = null;

        if (this.getSelectedItem() == trueString) {
            result = new Criterion(new BooleanField(true));
        } else if (this.getSelectedItem() == falseString) {
            result = new Criterion(new BooleanField(false));
        }
        return result;
    }

    /** {@inheritDoc} */
    public void searchPerformed() {
        //Don't need to do anything
    }

    /** {@inheritDoc} */
    public void addListener(final PropertyChangeListener listener) {
        //Do nothing
    }

    /** {@inheritDoc} */
    public void removeListener(final PropertyChangeListener listener) {
        //Do nothing
    }

}
