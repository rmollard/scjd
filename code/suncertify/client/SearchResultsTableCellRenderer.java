package suncertify.client;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import suncertify.fields.Field;
import suncertify.fields.viewers.FieldView;

/**
 * Renderer for the table of search results.
 * If a viewer has been configured for
 * the <code>Field</code> type, it will be used to display the
 * <code>Field</code>. Otherwise the <code>Field</code>'s
 * <code>toString</code> method will be used.
 *
 * @author Robert Mollard
 */
final class SearchResultsTableCellRenderer
    extends DefaultTableCellRenderer {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 8323180112235924648L;

    /**
     * The color of a selected row in the table (blue).
     */
    private static final Color SELECTION_COLOR = new Color(0x77afff);

    /**
     * The color of even rows in the table (light blue).
     */
    private static final Color EVEN_ROW_COLOR = new Color(0xf2f7ff);

    /**
     * The color of odd rows in the table (very light grey).
     */
    private static final Color ODD_ROW_COLOR = new Color(0xfcfcfc);

    /**
     * Map associating <code>Field</code> classes with <code>FieldView</code>
     * instances. There does not have to be an entry for every
     * <code>Field</code>.
     */
    private final Map<Class<? extends Field>, FieldView> fieldViewers;

    /**
     * Construct a <code>ResultsTableCellRenderer</code> instance.
     *
     * @param fieldViewers a map associating <code>Field</code> classes with
     *        <code>FieldView</code> instances. May be null or empty.
     *        There does not have to be an entry for every
     *        <code>Field</code>. Any <code>FieldView</code> may be null.
     */
    SearchResultsTableCellRenderer(
            final Map<Class<? extends Field>, FieldView> fieldViewers) {
        this.fieldViewers = fieldViewers;
    }

    /** {@inheritDoc} */
    @Override
    public Component getTableCellRendererComponent(
            final JTable table, final Object value,
            final boolean isSelected, final boolean hasFocus,
            final int row, final int column) {

        final Component cell = super.getTableCellRendererComponent(table,
                value, isSelected, hasFocus, row, column);

        //Color the unselected rows in alternating colors
        if (isSelected) {
            cell.setBackground(SELECTION_COLOR);
        } else if (row % 2 == 0) {
            cell.setBackground(EVEN_ROW_COLOR);
        } else {
            cell.setBackground(ODD_ROW_COLOR);
        }

        if (cell instanceof JLabel && value instanceof Field) {
            JLabel currentCell = (JLabel) cell;

            final FieldView viewer;
            if (fieldViewers == null) {
                viewer = null;
            } else {
                viewer = fieldViewers.get(value.getClass());
            }

            if (viewer == null) {
                //No viewer, just left align the text
                currentCell.setHorizontalAlignment(SwingConstants.LEFT);
            } else {
                //Viewer found, set text and alignment using the viewer.

                @SuppressWarnings("unchecked")
                final String text = viewer.getDisplayString((Field) value);
                currentCell.setText(text);

                @SuppressWarnings("unchecked")
                final int alignment = viewer.getAlignment().getDirection();
                currentCell.setHorizontalAlignment(alignment);
            }
        }
        return cell;
    }

}
