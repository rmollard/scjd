package suncertify.client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import suncertify.db.FieldParsers;
import suncertify.db.Record;
import suncertify.db.TableSchema;
import suncertify.fields.FieldDetails;
import suncertify.utils.Localization;

/**
 * Client MVC model. This is a model of the search results table.
 *
 * Thread safety: not thread safe
 *
 * @author Robert Mollard
 */
final class ClientModel extends AbstractTableModel {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -8514157944660527664L;

    /**
     * Special flag indicating a "hidden column" used to store the record
     * number. The record number can be retrieved by calling
     * <code>getValueAt</code> with <code>HIDDEN_COLUMN</code> as the
     * columnIndex parameter.
     */
    public static final int HIDDEN_COLUMN = -1;

    /**
     * Property name to use when the schema changes.
     */
    public static final String SCHEMA_PROPERTY = "schemaProperty";

    /**
     * Property name to use when the number of rows changes.
     */
    public static final String TABLE_ROW_COUNT = "tableRowCount";

    /**
     * Property name to use when the number of matching records for a search
     * becomes known.
     */
    public static final String SEARCH_RESULTS_SIZE = "searchResultsSize";

    /**
     * Property name to use when the current number of rows read for the current
     * search changes.
     */
    public static final String SEARCH_PROGRESS_UPDATE = "searchProgressUpdate";

    /**
     * Property name to use when the user cancels the current search.
     */
    public static final String SEARCH_CANCELLED = "searchCancelled";

    /**
     * Property name to use when the table selection should be cleared.
     */
    public static final String ROWS_DESELECTED = "rowsDeselected";

    /**
     * Property change support for firing events to listeners.
     */
    private final PropertyChangeSupport changeSupport;

    /**
     * All the records in the table (always non-null). This can be changed with
     * the <code>setRowData</code> method. Each <code>Record</code> in the
     * list may be modified.
     */
    private List<Record> rowData = new ArrayList<Record>();

    /**
     * The current table schema. This can be changed with the changeSchema()
     * method.
     */
    private TableSchema currentSchema;

    /**
     * Parsers used to parse strings into field values and vice versa.
     */
    private FieldParsers fieldParsers;

    /**
     * The columns to be displayed in the table.
     */
    private List<FieldDetails> columnsDisplayed;

    /**
     * The "real" index of each displayed column. If there are undisplayable
     * columns, the real index may be larger than the display column index. The
     * index of the list is the display column, and the value of each element is
     * the corresponding real index.
     */
    private List<Integer> displayIndexToRealIndex;

    /**
     * Construct a new instance with no row data and no schema.
     */
    ClientModel() {
        changeSupport = new PropertyChangeSupport(this);
        columnsDisplayed = new ArrayList<FieldDetails>();
        displayIndexToRealIndex = new ArrayList<Integer>();
    }

    /**
     * Add the given property change listener to the list of listeners.
     *
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(
            final PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Get the field class for the column that has
     * the given index.
     *
     * @param columnIndex the index of the column
     * @return the field class of the column.
     */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return columnsDisplayed.get(columnIndex).getFieldClass();
    }

    /**
     * Returns the number of rows in the search results table.
     *
     * @return the number of rows in the table
     */
    public int getRowCount() {
        return rowData.size();
    }

    /**
     * Returns the number of columns displayed in the
     * search results table.
     *
     * @return the number of columns displayed in the table
     */
    public int getColumnCount() {
        return columnsDisplayed.size();
    }

    /**
     * Returns a localized name for the column that has the given index.
     * If there is no translation for the column, returns the field name.
     *
     * @param columnIndex the index of the column being queried
     * @return a string containing the default name of <code>columnIndex</code>
     */
    @Override
    public String getColumnName(final int columnIndex) {
        return Localization.getStringIfPresent(columnsDisplayed
                .get(columnIndex).getFieldName());
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param rowIndex the row whose value is to be queried
     * @param columnIndex the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final Object result;
        final Record record = rowData.get(rowIndex);

        assert record != null : "Record not found at row: " + rowIndex;

        if (columnIndex == HIDDEN_COLUMN) {
            result = record.getRecordNumber();
        } else {
            result = record.getFieldList().get(
                    displayedIndexToRealIndex(columnIndex));
        }
        return result;
    }

    /**
     * Changes the row data to the specified list of records.
     * The records are expected to match the current schema.
     *
     * @param newRows the records that will form the new table rows
     * @throws IllegalArgumentException if <code>newRows</code> is null
     */
    void setRowData(final List<Record> newRows) {
        if (newRows == null) {
            throw new IllegalArgumentException("New rows must not be null");
        }

        //Always fire the event
        changeSupport.firePropertyChange(TABLE_ROW_COUNT, -1, newRows.size());
        rowData = newRows;
        fireTableDataChanged();
    }

    /**
     * Overwrites a record with the given record. The record that gets
     * overwritten will be the one that has the same record number as the given
     * record.
     *
     * Note that this does not change the record on the server.
     *
     * @param newRecord the new record
     */
    void modifyRecord(final Record newRecord) {
        int rowNum = getIndex(newRecord.getRecordNumber());
        rowData.set(rowNum, newRecord);
        fireTableRowsUpdated(rowNum, rowNum);
    }

    /**
     * Get the record with the given record number. Note that the record number
     * will probably be different from the row index.
     *
     * @param recordNumber the record number
     * @return the record with the given record number
     * @throws IllegalArgumentException if <code>recordNumber</code>
     *         does not correspond to a record in the table.
     */
    Record getRecord(final int recordNumber) {
        return rowData.get(getIndex(recordNumber));
    }

    /**
     * Change the schema of the table to the given schema.
     * This removes all rows from the table.
     *
     * @param schema the new schema to use. May be null.
     */
    void setSchema(final TableSchema schema) {

        if (schema != null) {
            columnsDisplayed = new ArrayList<FieldDetails>();
            List<FieldDetails> details = schema.getFields();

            for (int i = 0; i < details.size(); i++) {
                FieldDetails currentDetails = details.get(i);

                if (currentDetails.isDisplayable()) {
                    columnsDisplayed.add(currentDetails);
                    displayIndexToRealIndex.add(i);
                }
            }
        }

        //Delete all rows in the table
        rowData = new ArrayList<Record>();
        currentSchema = schema;
        changeSupport.firePropertyChange(SCHEMA_PROPERTY, null, this);

        fireTableStructureChanged(); //Columns have probably changed
        fireTableDataChanged(); //Rows have probably changed
    }

    /**
     * Set the total number of search results to the given value.
     *
     * @param resultCount the new total
     */
    void setSearchResultTotal(final int resultCount) {
        //Always fire the change
        changeSupport.firePropertyChange(SEARCH_RESULTS_SIZE, -1, resultCount);
    }

    /**
     * Set the current search progress to the given value.
     *
     * @param resultsRead the number of search results read
     */
    void setSearchProgress(final int resultsRead) {
        /*
         * Always fire the change, otherwise the progress
         * bar can have the incorrect value if the client disconnects
         * and then reconnects.
         */
        changeSupport.firePropertyChange(SEARCH_PROGRESS_UPDATE,
                -1, resultsRead);
    }

    /**
     * Inform all listeners that the search has been cancelled.
     */
    void cancelSearch() {
        changeSupport.firePropertyChange(SEARCH_CANCELLED, 0, 1);
    }

    /**
     * Inform all listeners that the table row selection is empty.
     */
    void deselectRows() {
        changeSupport.firePropertyChange(ROWS_DESELECTED, 0, 1);
    }

    /**
     * Get the current table schema.
     *
     * @return the current schema
     */
    TableSchema getCurrentSchema() {
        return currentSchema;
    }

    /**
     * Get the index in the table for a given record number.
     *
     * @param recordNumber the record number of the record to look up
     * @return the record's index
     * @throws IllegalArgumentException if <code>recordNumber</code>
     *         does not correspond to a record in the table.
     */
    private int getIndex(final int recordNumber) {
        int result = 0;
        boolean found = false;
        //Just do a linear search for now.
        for (int i = 0; i < rowData.size() && !found; i++) {
            Record r = rowData.get(i);
            if (r.getRecordNumber() == recordNumber) {
                result = i;
                found = true;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Record number not found: "
                    + recordNumber);
        }
        return result;
    }

    /**
     * Get the actual column index of a record, given its
     * displayed index. These may be different because there may
     * be undisplayable columns.
     *
     * @param displayedIndex the displayed index of the column
     * @return the real index of the column
     */
    private int displayedIndexToRealIndex(final int displayedIndex) {
        return displayIndexToRealIndex.get(displayedIndex);
    }

    /**
     * Get the parsers used to convert fields into strings
     * and vice versa.
     *
     * @return the field parsers
     */
    FieldParsers getFieldParsers() {
        return fieldParsers;
    }

    /**
     * Set the parsers used to convert fields into strings
     * and vice versa.
     *
     * @param newParsers the new parsers to use
     */
    void setFieldParsers(final FieldParsers newParsers) {
        fieldParsers = newParsers;
    }

}
