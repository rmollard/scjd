package suncertify.panels;

import java.awt.Component;
import java.beans.PropertyChangeListener;

import suncertify.Criterion;

/**
 * Identifies a GUI component for entering a database field value
 * for searching.
 *
 * @author Robert Mollard
 */
public interface FieldInputComponent {

    /**
     * Creates a clone of this input component.
     * Note that the clone's history list will be empty.
     *
     * @return A clone of this component
     */
    FieldInputComponent createClone();

    /**
     * Get the <code>Component</code> for this
     * widget so we can display it.
     * This could be a <code>JComboBox</code>, <code>JCheckBox</code> etc.
     * Complicated input components could return
     * a <code>JPanel</code> containing several sub-components.
     *
     * @return the Component to display
     */
    Component getComponent();

    /**
     * Get the <code>Criterion</code> for this field based on the
     * value entered by the user.
     *
     * @return a <code>Criterion</code> object encapsulating the
     *         search criterion for this field.
     * @throws IllegalArgumentException if the user's input could
     *         not be parsed
     */
    Criterion getCriterion();

    /**
     * This method is called when search has
     * just been performed.
     * It can add the current item to the input
     * component's history, for example.
     */
    void searchPerformed();

    /**
     * Add the given <code>PropertyChangeListener</code>.
     *
     * @param listener the <code>PropertyChangeListener</code> to add
     */
    void addListener(PropertyChangeListener listener);

    /**
     * Remove the given <code>PropertyChangeListener</code>.
     *
     * @param listener an existing <code>PropertyChangeListener</code>
     *        to remove
     */
    void removeListener(PropertyChangeListener listener);

}
