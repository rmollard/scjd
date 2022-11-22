package suncertify.client;

import java.awt.Component;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import suncertify.fields.Field;
import suncertify.fields.viewers.FieldView;
import suncertify.utils.EditManager;
import suncertify.utils.PeekableTable;

/**
 * The table containing the client's search results. The table will show
 * tooltips for cells that are not completely visible, so the user can easily
 * see the contents of a cell without resizing columns.
 *
 * @author Robert Mollard
 */
final class SearchResultsTable extends PeekableTable {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -4828991899621905416L;

    /**
     * The commands associated with this table.
     */
    enum Commands {

        /**
         * Book the selected record.
         */
        BOOK,

        /**
         * Select all rows in the table.
         * Note that a booking can only be made if
         * one row is selected.
         */
        SELECT_ALL,

        /**
         * Copy the selected records to the clipboard.
         */
        COPY,
    }

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify.client");

    /**
     * Flag indicating <code>defaultWidth</code> is unset.
     */
    private static final int UNSET = -1;

    /**
     * The default column width. This is lazily initialized.
     */
    private int defaultWidth = UNSET;

    /**
     * Map of viewers that are used to display the field class types.
     * This is used to set the column widths.
     */
    private final Map<Class<? extends Field>, FieldView> fieldViewers;

    /**
     * A map of the commands in the popup menu.
     */
    private Map<Commands, AbstractButton> buttons =
        new HashMap<Commands, AbstractButton>();

    /**
     * Create a new search results table.
     *
     * @param fieldViewers the viewers configured to display the field types
     */
    SearchResultsTable(final Map<Class<? extends Field>,
            FieldView> fieldViewers) {
        this.fieldViewers = fieldViewers;

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setDragEnabled(false);
        setAutoscrolls(true);
        setShowHorizontalLines(false);
        setShowVerticalLines(true);
        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);

        TableCellRenderer renderer = new SearchResultsTableCellRenderer(
                fieldViewers);

        //Use the renderer to render everything
        setDefaultRenderer(Object.class, renderer);

        tableHeader.setReorderingAllowed(true);
        tableHeader.setResizingAllowed(true);

        setTransferHandler(new TransferHandler() {

            /**
             * Default generated version number for serialization.
             */
            private static final long serialVersionUID = -762409964280063341L;

            @Override
            public int getSourceActions(final JComponent c) {
                //This table only supports copy
                return TransferHandler.COPY;
            }

            @Override
            public void exportToClipboard(final JComponent comp,
                    final Clipboard clip, final int action) {

                Transferable cells = createTransferable(comp);
                try {
                    StringSelection selection = getSelection();
                    clip.setContents(selection, selection);
                    exportDone(comp, cells, action);
                } catch (IllegalStateException e) {
                    exportDone(comp, cells, NONE);
                    throw e;
                }
            }
        });

        setColumnSizes();

        addMouseListener(new MouseMenuAdapter());

        //Cut and Paste are both always disabled
        EditManager.setCutEnabled(this, false);
        EditManager.setCopyEnabled(this, true);
        EditManager.setPasteEnabled(this, false);

        //Enable or disable the "copy" action based on the current selection
        getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    public void valueChanged(final ListSelectionEvent e) {

                        if (!e.getValueIsAdjusting()) {
                            ListSelectionModel lsm =
                                (ListSelectionModel) e.getSource();

                            EditManager.setCopyEnabled(SearchResultsTable.this,
                                    !lsm.isSelectionEmpty());
                        }
                    }
                });

        EditManager.registerComponentForEditActions(this);
    }

    /**
     * Get a <code>StringSelection</code> of the selected rows. For
     * <code>JLabel</code> cells, we use the cell renderer to get the display
     * text, instead of the text returned by the cell's <code>toString</code>
     * method.
     *
     * @return a <code>StringSelection</code> of the selected rows
     */
    private StringSelection getSelection() {
        StringSelection selection = null;
        StringBuffer buff = new StringBuffer();

        int columnCount = getSelectedColumnCount();
        int rowCount = getSelectedRowCount();

        final boolean singleSelection =
            (getSelectionModel().getSelectionMode()
                    == ListSelectionModel.SINGLE_SELECTION);

        //Select all columns if using single row selection
        if (singleSelection) {
            columnCount = getColumnCount();
        }

        int[] rowsSelected = getSelectedRows();
        int[] columnsSelected = getSelectedColumns();

        if (rowsSelected.length >= 1 && columnsSelected.length >= 1) {

            for (int r = 0; r < rowCount; r++) {
                for (int c = 0; c < columnCount; c++) {

                    final int row = rowsSelected[r];
                    final int col;

                    if (singleSelection) {
                        col = c;
                    } else {
                        col = columnsSelected[c];
                    }

                    //Try to use renderer if possible
                    Component component =
                        prepareRenderer(getCellRenderer(row, col), row, col);

                    if (component instanceof JLabel) {
                        buff.append(((JLabel) component).getText());
                    } else {
                        buff.append(getValueAt(row, col));
                    }

                    //If not last column, add a tab
                    if (c < columnCount - 1) {
                        buff.append('\t');
                    }
                }
                //Add a newline between each row
                buff.append('\n');
            }
            selection = new StringSelection(buff.toString());
        }
        return selection;
    }

    /**
     * Associate the given action with the given command.
     *
     * @param command the command to configure
     * @param action the action to be performed
     */
    void setAction(final Commands command, final Action action) {
        final AbstractButton button = buttons.get(command);
        if (button != null) {
            button.setAction(action);
        } else {
            LOGGER.severe("Could not find item for command: " + command);
            assert false : "Missing search table command: " + command;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void createDefaultColumnsFromModel() {
        super.createDefaultColumnsFromModel();
        //Need to set the columns to their custom sizes again
        setColumnSizes();
    }

    /**
     * Popup menu displayer class. We override the <code>mousePressed</code>
     * and <code>mouseReleased</code> methods so that the popup menu will be
     * displayed on all platforms.
     */
    private final class MouseMenuAdapter extends MouseAdapter {

        /**
         * The popup menu to display.
         */
        private JPopupMenu menu = new JPopupMenu();

        /**
         * Create a popup menu to enable the user to book a record, select all
         * records and copy any selected records to the clipboard.
         */
        private MouseMenuAdapter() {
            JMenuItem bookItem = new JMenuItem();
            buttons.put(Commands.BOOK, bookItem);
            menu.add(bookItem);

            menu.addSeparator();

            JMenuItem copyItem = new JMenuItem();
            buttons.put(Commands.COPY, copyItem);
            menu.add(copyItem);

            JMenuItem selectAllItem = new JMenuItem();
            buttons.put(Commands.SELECT_ALL, selectAllItem);
            menu.add(selectAllItem);
        }

        /** {@inheritDoc} */
        @Override
        public void mousePressed(final MouseEvent e) {
            tryToShowPopup(e);
        }

        /** {@inheritDoc} */
        @Override
        public void mouseReleased(final MouseEvent e) {
            tryToShowPopup(e);
        }

        /**
         * Shows the popup menu if the given mouse event
         * is the popup trigger on this platform.
         * Otherwise does nothing.
         *
         * @param e the mouse event
         */
        private void tryToShowPopup(final MouseEvent e) {
            if (e.isPopupTrigger()) {
                menu.show(SearchResultsTable.this, e.getX(), e.getY());
            }
        }
    }

    /**
     * Sets the column widths by consulting the field viewer for the column
     * class type.
     */
    private void setColumnSizes() {
        for (int col = 0; col < getColumnModel().getColumnCount(); col++) {
            final TableColumn column = getColumnModel().getColumn(col);
            final Class columnClass = getModel().getColumnClass(col);
            final FieldView viewer = fieldViewers.get(columnClass);

            if (viewer != null) {
                if (defaultWidth == UNSET) {
                    defaultWidth = column.getPreferredWidth();
                }

                @SuppressWarnings("unchecked")
                final double multiplier =
                    viewer.getFieldWidth().getWidthMultiplier();
                column.setPreferredWidth((int) (defaultWidth * multiplier));
            }
        }
    }

}
