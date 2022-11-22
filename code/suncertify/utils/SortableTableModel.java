package suncertify.utils;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import suncertify.IconFactory;

/**
 * This class provides row sorting support for tables.
 *
 * The <code>setTableModel</code> method should be called to set the
 * data to be sorted.
 * Then the table to be sorted should have its model
 * set to the sorter, for example:
 * <pre>
 *  JTable table = new JTable();
 *   ...
 *
 *  SortableTableModel sorter = new SortableTableModel();
 *
 *  sorter.setTableModel(model);
 *  table.setModel(sorter);
 * </pre>
 *
 *  The table can then be sorted with the
 *  <code>setSortingStatus</code> method.
 *  Using the <code>setTableHeader</code> method will allow the user to
 *  sort the table by clicking on the table column headers.
 *  For example:
 *  <pre>
 *   sorter.setTableHeader(table.getTableHeader());
 *  </pre>
 *
 * @author Robert Mollard
 */
public final class SortableTableModel extends AbstractTableModel {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -8945624018680741248L;

    /**
     * The possible column sorting directions.
     */
    private enum Direction {

        /**
         * Sorted in descending order.
         */
        DESCENDING("client.sorting.descending"),

        /**
         * Not sorted.
         */
        NOT_SORTED("client.sorting.notSorted"),

        /**
         * Sorted in ascending order.
         */
        ASCENDING("client.sorting.ascending");

        /**
         * The next index available.
         */
        private static int nextIndex = 0;

        /**
         * The localized name of this <code>Direction</code>.
         */
        private final String name;

        /**
         * The ordinal index of this <code>Direction</code>.
         * This is used to determine the next <code>Direction</code>.
         */
        private final int index;

        /**
         * Create a <code>Direction</code> with the
         * given name, and the next available index.
         *
         * @param name the name (gets localized)
         */
        private Direction(final String name) {
            this.name = Localization.getString(name);
            index = nextIndex++;
        }

        /**
         * Get the <code>Direction</code> with the next index,
         * possibly wrapping back to the first one.
         *
         * @return the next direction
         */
        private Direction getNextStatus() {
            final Direction[] values = values();
            return values[(index + 1) % values.length];
        }

        /**
         * Get a string describing the direction.
         * The string is the localized name of the direction.
         *
         * @return a string representation of the object
         */
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Trivial class to contain a column/direction pair.
     */
    private static final class SortableColumn {

        /**
         * Column index, starts at 0.
         */
        private final int column;

        /**
         * Direction that the column is sorted in.
         * May be unsorted.
         */
        private final Direction direction;

        /**
         * Create a sortable column for the given column
         * index and direction.
         *
         * @param column the column index, starts at 0.
         * @param direction the sorting direction
         */
        private SortableColumn(final int column,
                final Direction direction) {
            this.column = column;
            this.direction = direction;
        }
    }

    /**
     * Class to compare two objects by using the first object's
     * <code>compareTo</code> method.
     */
    private static class GenericComparator
        implements Comparator<Object>, Serializable {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = -2416589723540224669L;

        public int compare(final Object o1, final Object o2) {
            @SuppressWarnings("unchecked")
            Comparable<Object> o1Comparator = (Comparable<Object>) o1;
            return o1Comparator.compareTo(o2);
        }
    }

    /**
     * Class to compare two objects alphabetically based on their
     * <code>toString</code> values.
     */
    private static class StringComparator
        implements Comparator<Object>, Serializable {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = -6998048219593827792L;

        public int compare(final Object o1, final Object o2) {
            final int comparison;
            final String string1 = o1.toString();
            final String string2 = o2.toString();

            if (string1 == null && string2 == null) {
                comparison = 0;
            } else if (string1 == null) {
                comparison = -1;
            } else if (string2 == null) {
                comparison = 1;
            } else {
                comparison = string1.compareTo(string2);
            }
            return comparison;
        }
    }

    /**
     * Helper class to model a row in the table.
     */
    private class SortableRow implements Comparable, Serializable {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = -7542993029645070517L;

        /**
         * The real index of the row.
         */
        private int modelIndex;

        /**
         * Create a sortable table row with the given index.
         *
         * @param index the real index of the row
         */
        SortableRow(final int index) {
            this.modelIndex = index;
        }

        public int compareTo(final Object o) {
            int result = 0;
            int row1 = modelIndex;
            int row2 = ((SortableRow) o).modelIndex;

            for (Iterator it = sortingColumns.iterator();
                    it.hasNext() && result == 0;) {
                SortableColumn directive = (SortableColumn) it.next();
                int column = directive.column;
                Object o1 = tableModel.getValueAt(row1, column);
                Object o2 = tableModel.getValueAt(row2, column);

                int comparison = 0;
                // Define null less than everything, except null.
                if (o1 == null && o2 == null) {
                    comparison = 0;
                } else if (o1 == null) {
                    comparison = -1;
                } else if (o2 == null) {
                    comparison = 1;
                } else {
                    comparison = getComparator(column).compare(o1, o2);
                }
                if (comparison != 0) {
                    result = comparison;

                    //Reverse the comparison if sorting in descending order
                    if (directive.direction == Direction.DESCENDING) {
                        result = -result;
                    }
                }
            }
            return result;
        }
    }

    /**
     * Listener class to handle changes to the table.
     * We might cancel the sorting status of the table depending
     * on what table model event occurs.
     */
    private class TableModelHandler
        implements TableModelListener, Serializable {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = -6579003558547724917L;

        public void tableChanged(final TableModelEvent e) {

            int column = e.getColumn();

            if (sortingColumns.size() == 0) {
                //No columns to sort by
                resetSortingState();
                fireTableChanged(e);
            } else if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                //Table format has changed
                sortingColumns.clear();
                sortingStatusChanged();
                fireTableChanged(e);
            } else if (e.getFirstRow() == e.getLastRow()
                    && column != TableModelEvent.ALL_COLUMNS
                    && getSortingStatus(column) == Direction.NOT_SORTED
                    && modelToView != null) {
                /*
                 * Only one cell in the table has changed, and we are not
                 * sorting on the column, and we won't trigger a
                 * table sorting by forwarding the event.
                 * Convert the row into the view row and forward on the event.
                 */
                int viewIndex = getModelToView()[e.getFirstRow()];
                fireTableChanged(new TableModelEvent(SortableTableModel.this,
                                                     viewIndex, viewIndex,
                                                     column, e.getType()));
            } else {
                /*
                 * Some other table change, we clear the sorting state just in
                 * case the sorting order was invalidated.
                 */
                resetSortingState();
                fireTableDataChanged();
            }
        }
    }

    /**
     * Mouse handler for the table header.
     * We sort the column that the user clicks on.
     * The user can sort by multiple columns by holding
     * down the Ctrl (or Meta) key.
     */
    private class MouseHandler extends MouseAdapter
        implements Serializable {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = 2019070719812865476L;

        public void mouseClicked(final MouseEvent e) {
            final Object source = e.getSource();
            if (source instanceof JTableHeader) {
                final JTableHeader header = (JTableHeader) source;
                final TableColumnModel columnModel = header.getColumnModel();
                final int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                final int column =
                    columnModel.getColumn(viewColumn).getModelIndex();
                if (column != -1) {
                    Direction status = getSortingStatus(column);

                    if (!e.isControlDown()) {
                        sortingColumns.clear();
                        sortingStatusChanged();
                    }

                    status = status.getNextStatus();
                    setSortingStatus(column, status);
                }
            }
        }
    }

    /**
     * Renderer for the table header.
     * We display the column name and an icon indicating
     * the sorting status of the column.
     * We also provide a tooltip displaying the column name and its
     * sorting status.
     */
    private final class HeaderRenderer implements TableCellRenderer {

        /**
         * The original cell renderer for the table header.
         */
        private TableCellRenderer tableCellRenderer;

        /**
         * Create a new renderer.
         *
         * @param tableCellRenderer the existing renderer
         */
        private HeaderRenderer(
                final TableCellRenderer tableCellRenderer) {
            this.tableCellRenderer = tableCellRenderer;
        }

        public Component getTableCellRendererComponent(final JTable theTable,
                final Object value, final boolean isSelected,
                final boolean hasFocus, final int row, final int column) {
            Component component = tableCellRenderer
                    .getTableCellRendererComponent(theTable, value, isSelected,
                            hasFocus, row, column);

            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                label.setHorizontalTextPosition(SwingConstants.LEFT);
                final int modelColumn = theTable
                        .convertColumnIndexToModel(column);
                label.setIcon(getHeaderRendererIcon(modelColumn));

                String colName = theTable.getModel().getColumnName(modelColumn);

                String sortingStatus =
                    getSortableColumn(modelColumn).direction.toString();

                label.setToolTipText(Localization.getString(
                        "tableHeader.tooltip", colName, sortingStatus));
            }
            return component;
        }
    }

    /**
     * The factor to multiply consecutive table header sorting icons by.
     * A column's header icon size will be multiplied by
     * <code>Math.pow(ICON_SIZE_REDUCTION, order)</code>
     * where order is the sorting index of the column (starting from 0),
     * for example the secondary sorting column would have a
     * sorting index of 1.
     */
    private static final double ICON_SIZE_REDUCTION = 0.9;

    /**
     * Default column used when a column search fails.
     */
    private static final SortableColumn EMPTY_DIRECTIVE =
        new SortableColumn(-1, Direction.NOT_SORTED);

    /**
     * Comparator to compare objects using the first object's
     * <code>compareTo</code> method.
     */
    private static final Comparator<Object> COMPARABLE_COMAPRATOR =
        new GenericComparator();

    /**
     * Simple comparator to perform an alphabetical comparison.
     */
    private static final Comparator<Object> LEXICAL_COMPARATOR =
        new StringComparator();

    /**
     * The table model being sorted.
     */
    private TableModel tableModel;

    /**
     * An array containing the table rows indexed by view index.
     */
    private SortableRow[] viewToModel;

    /**
     * An array containing the displayed row indices of the
     * table rows, ordered by the real row indices.
     * The view index of a row with an index of <code>modelIndex</code>
     * is given by <code>modelToView[modelIndex]</code>
     */
    private int[] modelToView;

    /**
     * The table header of the table being sorted.
     */
    private JTableHeader tableHeader;

    /**
     * A basic mouse listener to handle the user clicking on
     * the table header to sort the table.
     */
    private MouseListener mouseListener;

    /**
     * Listener to handle table events. When the table is changed
     * the table might not be sorted any more.
     */
    private TableModelListener tableModelListener;

    /**
     * The columns being sorted. The list is ordered so that
     * index 0 is the primary sorting column, index 1 is
     * the secondary sorting column, and so on.
     */
    private List<SortableColumn> sortingColumns =
        new ArrayList<SortableColumn>();

    /**
     * Create a new sortable table model.
     */
    public SortableTableModel() {
        this.mouseListener = new MouseHandler();
        this.tableModelListener = new TableModelHandler();
    }

    /**
     * Create a new sortable table model to add sorting
     * support for the given table model.
     *
     * @param tableModel the table model to sort
     */
    public SortableTableModel(final TableModel tableModel) {
        setTableModel(tableModel);
    }

    /**
     * Clear the sorting mappings.
     */
    private void resetSortingState() {
        viewToModel = null;
        modelToView = null;
    }

    /**
     * Set the table model being sorted.
     *
     * @param newTableModel the new table model
     */
    public void setTableModel(final TableModel newTableModel) {
        if (tableModel != null) {
            tableModel.removeTableModelListener(tableModelListener);
        }

        tableModel = newTableModel;
        if (tableModel != null) {
            tableModel.addTableModelListener(tableModelListener);
        }

        resetSortingState();
        fireTableStructureChanged();
    }

    /**
     * Set the table header that will be used to allow the
     * user to sort the table contained.
     *
     * @param newTableHeader the new table header to use
     */
    public void setTableHeader(final JTableHeader newTableHeader) {
        if (tableHeader != null) {
            tableHeader.removeMouseListener(mouseListener);
            TableCellRenderer defaultRenderer =
                tableHeader.getDefaultRenderer();
            if (defaultRenderer instanceof HeaderRenderer) {
                tableHeader.setDefaultRenderer(
                    ((HeaderRenderer) defaultRenderer).tableCellRenderer);
            }
        }
        tableHeader = newTableHeader;
        if (tableHeader != null) {
            tableHeader.addMouseListener(mouseListener);
            tableHeader.setDefaultRenderer(
                    new HeaderRenderer(
                            this.tableHeader.getDefaultRenderer()));
        }
    }

    /**
     * Get the sortable column for a given column index.
     *
     * @param modelColumn the index of the column in the model
     * @return the sortable column
     */
    private SortableColumn getSortableColumn(final int modelColumn) {
        SortableColumn result = EMPTY_DIRECTIVE;
        boolean found = false;

        for (int i = 0; i < sortingColumns.size() && !found; i++) {
            SortableColumn directive = sortingColumns.get(i);
            if (directive.column == modelColumn) {
                result = directive;
                found = true;
            }
        }
        return result;
    }

    /**
     * Get the sorting direction for the column with
     * the given index.
     *
     * @param modelColumn the index of the column in the model
     * @return the sorting direction
     */
    private Direction getSortingStatus(final int modelColumn) {
        return getSortableColumn(modelColumn).direction;
    }

    /**
     * Indicate that the sorting status of the table has changed.
     * The table header is repainted to indicate the new status.
     */
    private void sortingStatusChanged() {
        resetSortingState();
        fireTableDataChanged();
        if (tableHeader != null) {
            tableHeader.repaint();
        }
    }

    /**
     * Set the sorting status for the given column.
     *
     * @param column the index of the column to set
     * @param status the sorting status
     */
    private void setSortingStatus(final int column, final Direction status) {
        SortableColumn directive = getSortableColumn(column);
        if (directive != EMPTY_DIRECTIVE) {
            sortingColumns.remove(directive);
        }
        if (status != Direction.NOT_SORTED) {
            sortingColumns.add(new SortableColumn(column, status));
        }
        sortingStatusChanged();
    }

    /**
     * Get the comparator to use for sorting the given column.
     *
     * @param column the index of the column to sort
     * @return the comparator for the column with the given index
     */
    private Comparator<Object> getComparator(final int column) {
        final Comparator<Object> result;
        Class columnType = tableModel.getColumnClass(column);

        if (Comparable.class.isAssignableFrom(columnType)) {
            result = COMPARABLE_COMAPRATOR;
        } else {
            result = LEXICAL_COMPARATOR;
        }
        return result;
    }

    /**
     * Get an array of table rows indexed by view index.
     *
     * @return array of sortable rows
     */
    private SortableRow[] getViewToModel() {
        if (viewToModel == null) {
            int tableModelRowCount = tableModel.getRowCount();
            viewToModel = new SortableRow[tableModelRowCount];
            for (int row = 0; row < tableModelRowCount; row++) {
                viewToModel[row] = new SortableRow(row);
            }

            if (sortingColumns.size() != 0) {
                Arrays.sort(viewToModel);
            }
        }
        return viewToModel;
    }

    /**
     * Get the actual index of a row, given its index
     * in the view of the table.
     *
     * @param viewIndex the index of the row in the view
     * @return the model index of the row
     */
    public int getModelIndex(final int viewIndex) {
        return getViewToModel()[viewIndex].modelIndex;
    }

    /**
     * Get an array of row numbers indexed by model index.
     *
     * @return array of row numbers
     */
    private int[] getModelToView() {
        if (modelToView == null) {        	
            final int length = getViewToModel().length;
            modelToView = new int[length];
            for (int i = 0; i < length; i++) {
                modelToView[getModelIndex(i)] = i;
            }
        }
        return modelToView;
    }

    /**
     * Returns the number of rows in the model. A
     * <code>JTable</code> uses this method to determine how many rows it
     * should display.
     *
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    public int getRowCount() {
        final int result;
        if (tableModel == null) {
            result = 0;
        } else {
            result = tableModel.getRowCount();
        }
        return result;
    }

    /**
     * Returns the number of columns in the model. A
     * <code>JTable</code> uses this method to determine how many columns it
     * should create and display by default.
     *
     * @return the number of columns in the model
     * @see #getRowCount
     */
    public int getColumnCount() {
        final int result;
        if (tableModel == null) {
            result = 0;
        } else {
            result = tableModel.getColumnCount();
        }
        return result;
    }

    /**
     * Returns a default name for the column using spreadsheet conventions:
     * A, B, C, ... Z, AA, AB, etc. If <code>column</code> cannot be found,
     * returns an empty string.
     *
     * @param column the column being queried
     * @return a string containing the default name of <code>column</code>
     */
    @Override
    public String getColumnName(final int column) {
        return tableModel.getColumnName(column);
    }

    /**
     * Returns the most specific class of the specified column.
     *
     * @param column the column being queried
     * @return the class of the column
     */
    @Override
    public Class<?> getColumnClass(final int column) {
        return tableModel.getColumnClass(column);
    }

    /**
     * Determines if the cell at the given row and column is
     * editable.
     *
     * @param row the row being queried
     * @param column the column being queried
     * @return true if the cell is editable
     */
    @Override
    public boolean isCellEditable(final int row, final int column) {
        return tableModel.isCellEditable(getModelIndex(row), column);
    }

    /**
     * Returns the value for the cell at <code>column</code> and
     * <code>row</code>.
     *
     * @param row the row whose value is to be queried
     * @param column the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    public Object getValueAt(final int row, final int column) {
        Object result = null;
        if (row >= 0) {
            result = tableModel.getValueAt(getModelIndex(row), column);
        }
        return result;
    }

    /**
     * Set a cell in the table to the given value.
     * If the table model is null, this method does nothing.
     *
     *  @param aValue the value to assign to the cell
     *  @param row row of the cell
     *  @param column column of the cell
     */
    @Override
    public void setValueAt(final Object aValue,
            final int row, final int column) {
        tableModel.setValueAt(aValue, getModelIndex(row), column);
    }

    /**
     * Get the icon to display at the top of the given column.
     * This will be an icon displaying the sorting status
     * of the column.
     *
     * @param modelColumn the actual index of the column
     * @return an icon for the column
     */
    private Icon getHeaderRendererIcon(final int modelColumn) {
        SortableColumn sorter = getSortableColumn(modelColumn);
        ImageIcon icon = null; //The sorting icon to use
        ImageIcon scaledIcon = null; //Reduced size version of icon

        if (sorter.direction == Direction.ASCENDING) {
            icon = IconFactory.getImageIcon(Localization
                    .getString("client.table.ascending"));
        } else if (sorter.direction == Direction.DESCENDING) {
            icon = IconFactory.getImageIcon(Localization
                    .getString("client.table.descending"));
        }
        if (icon != null) {
            final int order = sortingColumns.indexOf(sorter);
            /*
             * Scale the image so that the primary sorting column has
             * the largest icon, the secondary sorting column has the
             * second largest icon, and so on.
             */
            final double multiplier = Math.pow(ICON_SIZE_REDUCTION, order);

            final int newWidth = (int) (icon.getIconWidth() * multiplier);
            final int newHeight = (int) (icon.getIconHeight() * multiplier);

            Image scaled = icon.getImage().getScaledInstance(newWidth,
                    newHeight, Image.SCALE_SMOOTH);

            scaledIcon = new ImageIcon(scaled);
        }
        return scaledIcon;
    }

}
