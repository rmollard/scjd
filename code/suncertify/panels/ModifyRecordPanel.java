package suncertify.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import suncertify.WidgetFactory;
import suncertify.db.FieldParsers;
import suncertify.db.Record;
import suncertify.fields.Field;
import suncertify.fields.FieldDetails;
import suncertify.fields.IntegerField;
import suncertify.fields.parsers.FieldParser;
import suncertify.fields.viewers.FieldView;
import suncertify.utils.IntegerDocumentFilter;
import suncertify.utils.LengthDocumentFilter;
import suncertify.utils.Localization;

/**
 * A dialog box that enables the user to modify a record. Non-modifiable fields
 * are displayed but are not editable. Each modifiable field has an editable
 * text field that initially contains the current value for the field.
 *
 * The <code>loadRecord</code> method should be called first to initialize the
 * dialog box. Then <code>showDialog</code> can be used to display the dialog
 * box to the user. Finally, <code>getNewFieldValues</code> can be called to
 * get the new fields for the record.
 *
 * @author Robert Mollard
 */
public final class ModifyRecordPanel extends JPanel {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 4254830825184180077L;

    /**
     * Minimum width of each text field, in pixels.
     */
    private static final int TEXT_FIELD_WIDTH = 200;

    /**
     * The current main scroll pane in the dialog box. This is removed when a
     * new record is displayed.
     */
    private JScrollPane scroller;

    /**
     * List of input fields, one for each field in the record.
     */
    private List<JTextField> inputFields;

    /**
     * Parsers used to parse the fields in the current record.
     */
    private FieldParsers parsers;

    /**
     * The database schema for the current record.
     */
    private List<FieldDetails> schema;

    /**
     * Create a new <code>ModifyRecordPanel</code>.
     * The <code>loadRecord</code> method should be
     * called to initialize the panel or to reset its state.
     */
    public ModifyRecordPanel() {
        //Empty
    }

    /**
     * Display the given record in the panel.
     *
     * @param record the record to load
     * @param newSchema the database schema
     * @param fieldParsers the parsers used to parse the record's field values
     * @param fieldViewers the viewers used to display the field values
     *        to the user
     */
    public void loadRecord(final Record record,
            final List<FieldDetails> newSchema,
            final FieldParsers fieldParsers,
            final Map<Class<? extends Field>, FieldView> fieldViewers) {

        //Remove the panel for the previous record that was displayed
        if (scroller != null) {
            remove(scroller);
        }

        parsers = fieldParsers;
        schema = newSchema;

        inputFields = new ArrayList<JTextField>();

        JPanel currentPanel = new JPanel();
        currentPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(4, 4, 4, 4);

        //Information message to display at the top of the panel
        JEditorPane infoText = new JEditorPane("text/html", Localization
                .getString("client.modifyRecordPanelInfoText"));

        infoText.setEditable(false);
        infoText.setBorder(BorderFactory.createEmptyBorder());
        infoText.setBackground(WidgetFactory.LIGHT_BACKGROUND);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        currentPanel.add(infoText, constraints);

        constraints.gridy++;
        constraints.gridwidth = GridBagConstraints.RELATIVE;

        /*
         * Make an entry field for each field in the record.
         * We currently just use a JTextField for all field types.
         * This could be enhanced by using a custom entry
         * widget e.g. a checkbox for boolean types.
         */
        for (int i = 0; i < schema.size(); i++) {
            final FieldDetails details = schema.get(i);

            final JLabel fieldName = new JLabel(Localization
                    .getStringIfPresent(details.getFieldName()));

            constraints.gridx = 0;
            constraints.anchor = GridBagConstraints.EAST;
            currentPanel.add(fieldName, constraints);

            final String value;
            final Field currentField = record.getFieldList().get(i);

            //Use the viewer to create the display string for the field
            final FieldView viewer =
                fieldViewers.get(currentField.getClass());
            if (viewer == null || details.isModifiable()) {
                //Use the parser to display the field
                final FieldParser parser =
                    parsers.getParserForClass(details.getFieldClass());

                String parserString = parser.getString(currentField);
                value = parserString;
            } else {
                //Viewer was found, and field is not modifiable
                @SuppressWarnings("unchecked")
                String displayString = viewer.getDisplayString(currentField);
                value = displayString;
            }

            constraints.gridx++;
            constraints.anchor = GridBagConstraints.WEST;

            final JTextField textField = new JTextField();

            if (details.isModifiable()) {
                final Integer maxLength = details.getMaxLength();
                if (maxLength != null) {

                    if (details.getFieldClass() == IntegerField.class) {
                        textField.setDocument(
                                new IntegerDocumentFilter(maxLength));
                    } else {
                        textField.setDocument(
                                new LengthDocumentFilter(maxLength));
                    }
                }
                textField.setBorder(BorderFactory.createEtchedBorder());

            } else {
                textField.setBorder(BorderFactory.createEmptyBorder());
                textField.setBackground(WidgetFactory.LIGHT_BACKGROUND);
            }

            textField.setText(value);

            //Set each text field to a sensible width
            final int width = Math.max(TEXT_FIELD_WIDTH,
                    textField.getPreferredSize().width);
            final int height = textField.getPreferredSize().height;
            textField.setPreferredSize(new Dimension(width, height));

            textField.setEditable(details.isModifiable());

            currentPanel.add(textField, constraints);
            inputFields.add(textField);
            constraints.gridy++;
        }

        scroller = new JScrollPane(currentPanel);
        scroller.setBorder(null);
        add(scroller);
    }

    /**
     * Display the dialog box.
     *
     * @param parent the parent component
     * @return true if the user pressed the OK button
     */
    public boolean showDialog(final JFrame parent) {

        int result = JOptionPane.showConfirmDialog(parent, this,
                Localization.getString("client.modifyRecordPanelTitle"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Get the new field values for the record.
     * An unmodifiable field will have a null entry.
     *
     * @return a list of fields in the order specified by the database schema.
     *         A null entry means the field is not modifiable.
     */
    public List<Field> getModifiedFieldValues() {
        final List<Field> newValues = new ArrayList<Field>();

        for (int i = 0; i < schema.size(); i++) {
            final FieldDetails details = schema.get(i);

            if (details.isModifiable()) {
                String newString = inputFields.get(i).getText();
                FieldParser parser =
                    parsers.getParserForClass(details.getFieldClass());
                Field newField = parser.valueOf(newString);

                newValues.add(newField);
            } else {
                newValues.add(null);
            }
        }
        return newValues;
    }

}
