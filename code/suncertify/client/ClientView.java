package suncertify.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import suncertify.Criterion;
import suncertify.IconFactory;
import suncertify.View;
import suncertify.WidgetFactory;
import suncertify.db.TableSchema;
import suncertify.fields.BooleanField;
import suncertify.fields.CurrencyField;
import suncertify.fields.DateField;
import suncertify.fields.Field;
import suncertify.fields.FieldDetails;
import suncertify.fields.IntegerField;
import suncertify.fields.StringField;
import suncertify.fields.parsers.FieldParser;
import suncertify.fields.parsers.PlainIntegerParser;
import suncertify.fields.viewers.BooleanFieldView;
import suncertify.fields.viewers.CurrencyFieldView;
import suncertify.fields.viewers.DateFieldView;
import suncertify.fields.viewers.FieldView;
import suncertify.fields.viewers.IntegerFieldView;
import suncertify.fields.viewers.StringFieldView;
import suncertify.panels.BooleanEntryField;
import suncertify.panels.FieldInputComponent;
import suncertify.panels.GenericEntryField;
import suncertify.panels.StringEntryField;
import suncertify.utils.IntegerDocumentFilter;
import suncertify.utils.Localization;
import suncertify.utils.SmartComboBox;
import suncertify.utils.SortableTableModel;

/**
 * Main application GUI for URLyBird application when running in "client" or
 * "standalone" mode. In client mode, the connection dialog prompts for the
 * network address of the server. In standalone mode, the connection dialog
 * prompts for the filename of the database file to be opened.
 *
 * The search form is created when the user connects to a server.
 *
 * @author Robert Mollard
 */
final class ClientView extends View {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 4163297743842323787L;

    /**
     * The commands provided by buttons in the main client GUI panel.
     */
    public enum Commands {

        /**
         * Search for rooms that match the criteria given in the search form.
         */
        SEARCH,

        /**
         * Cancel the currently running search.
         */
        CANCEL_SEARCH,

        /**
         * Book or unbook the selected room.
         */
        BOOK,
    }

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify.client");

    /**
     * The width of the main frame, as a fraction of the screen width.
     */
    private static final double WIDTH_MULTIPLIER = 0.75;

    /**
     * The height of the main frame, as a fraction of the screen height.
     */
    private static final double HEIGHT_MULTIPLIER = 0.75;

    /**
     * The initial delay for displaying tool tips, in milliseconds.
     */
    private static final int TOOL_TIP_DELAY = 200;

    /**
     * The current main frame displayed.
     * This is generated based on the server's schema.
     */
    private JScrollPane currentFrame;

    /**
     * Button to enable user to search for matching records.
     */
    private final JButton searchButton;

    /**
     * Button to enable user to cancel the current search.
     */
    private final JButton cancelSearchButton;

    /**
     * Button to enable user to modify the selected record.
     */
    private final JButton bookButton;

    /**
     * Checkbox to indicate whether or not the search results should only
     * include records that are bookable by the client.
     */
    private final JCheckBox bookableOnly;

    /**
     * Status bar, shows current program status.
     */
    private final JLabel statusBar;

    /**
     * Scroll pane to contain the status bar.
     */
    private JScrollPane statusScroller;

    /**
     * The original size of the info bar scroll pane.
     */
    private Dimension originalStatusBarSize;

    /**
     * Panel at the bottom of the screen to contain the status label and the
     * search progress bar.
     */
    private final JPanel infoPanel;

    /**
     * A simple progress bar to indicate the current search progress.
     */
    private JProgressBar progressBar;

    /**
     * A list of all the entry fields, one entry per field.
     * If the field is not
     * searchable, there will be a null entry in the list.
     */
    private List<FieldInputComponent> inputComponentList =
        new ArrayList<FieldInputComponent>();

    /**
     * Listeners that will be notified when the selected row in the search
     * results table changes.
     */
    private List<TableSelectionListener> listenerList =
        new ArrayList<TableSelectionListener>();

    /**
     * A map of the Commands in main screen.
     */
    private Map<Commands, JButton> buttons;

    /**
     * The table containing the search results.
     */
    private SearchResultsTable table;

    /**
     * The toolbar, contains buttons for some of the commands.
     */
    private ClientToolbar toolBar;

    /**
     * The menu bar for the program when running in client mode.
     */
    private final ClientMenuBar menuBar;

    /**
     * Table sorting support.
     */
    private final SortableTableModel sorter = new SortableTableModel();

    /**
     * Map associating Field classes with their
     * corresponding input components.
     */
    private final Map<Class<? extends Field>,
                    FieldInputComponent> entryTypeForField;

    /**
     * Map associating <code>Field</code> classes with the classes used to
     * view them.
     */
    private final Map<Class<? extends Field>, FieldView> fieldViewers;

    /**
     * Create a new client view. The main frame is added when the user connects
     * to a server if in network mode, or opens a file if in standalone mode.
     *
     * @param isStandalone true if running in standalone mode,
     *        or false if in network client mode.
     */
    ClientView(final boolean isStandalone) {

        final Dimension screenSize =
            Toolkit.getDefaultToolkit().getScreenSize();

        setSize((int) (screenSize.width * WIDTH_MULTIPLIER),
                (int) (screenSize.height * HEIGHT_MULTIPLIER));

        //Show tool tips after a short delay
        ToolTipManager.sharedInstance().setInitialDelay(TOOL_TIP_DELAY);

        WidgetFactory.getInstance().configureForeground();
        WidgetFactory.getInstance().configureBackground();

        bookableOnly = new JCheckBox(
                Localization.getString("client.showBookableRecordsOnly"));

        entryTypeForField =
            new HashMap<Class<? extends Field>, FieldInputComponent>();

        fieldViewers = new HashMap<Class<? extends Field>, FieldView>();

        //Set up custom entry fields
        final BooleanEntryField booleanEntry = new BooleanEntryField();
        booleanEntry.addListener(this);
        final StringEntryField stringEntry = new StringEntryField(true);
        stringEntry.addListener(this);
        final GenericEntryField integerEntry = new GenericEntryField(true,
                new PlainIntegerParser());
        integerEntry.addListener(this);
        integerEntry.setDocumentFilter(new IntegerDocumentFilter(null));
        entryTypeForField.put(BooleanField.class, booleanEntry);
        entryTypeForField.put(StringField.class, stringEntry);
        entryTypeForField.put(IntegerField.class, integerEntry);

        //Configure viewers for results table
        fieldViewers.put(BooleanField.class, new BooleanFieldView());
        fieldViewers.put(DateField.class, new DateFieldView());
        fieldViewers.put(IntegerField.class, new IntegerFieldView());
        fieldViewers.put(CurrencyField.class, new CurrencyFieldView());
        fieldViewers.put(StringField.class, new StringFieldView());

        //Set title text
        final String mode;
        if (isStandalone) {
            mode = Localization.getString("client.standaloneMode");
        } else {
            mode = Localization.getString("client.clientMode");
        }
        setTitle(Localization.getString("titleText", new String[] {
                mode, Localization.getString("programName") }));

        buttons = new HashMap<Commands, JButton>();
        table = new SearchResultsTable(fieldViewers);

        addTableListener();

        searchButton = createButton(Commands.SEARCH);
        cancelSearchButton = createButton(Commands.CANCEL_SEARCH);
        bookButton = createButton(Commands.BOOK);

        toolBar = new ClientToolbar();
        getContentPane().add(toolBar, BorderLayout.NORTH);

        statusBar = new JLabel();
        statusBar.setText(Localization.getString("client.ready"));

        statusScroller = new JScrollPane(statusBar);
        originalStatusBarSize = statusScroller.getPreferredSize();

        infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());

        JPanel statusContainer = new JPanel();
        statusContainer.setLayout(new BorderLayout());
        statusContainer.add(statusScroller);
        infoPanel.add(statusContainer);

        progressBar = new JProgressBar();
        resetProgressBar();
        progressBar.setStringPainted(true);

        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BorderLayout());
        progressPanel.add(progressBar);

        infoPanel.add(progressPanel, BorderLayout.EAST);
        getContentPane().add(infoPanel, BorderLayout.SOUTH);

        JPanel emptyPanel = new JPanel();
        currentFrame = new JScrollPane(emptyPanel);

        getContentPane().add(currentFrame, BorderLayout.CENTER);

        //Set title bar icon
        ImageIcon image = IconFactory.getImageIcon(
                Localization.getString("URLyBirdProgramIcon"));
        if (image != null) {
            setIconImage(image.getImage());
        }

        menuBar = new ClientMenuBar();
        setJMenuBar(menuBar);

        //We rely on the controller to clean up
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    /** {@inheritDoc} */
    public void propertyChange(final PropertyChangeEvent evt) {

        if (SmartComboBox.ENTER_PROPERTY.equals(evt.getPropertyName())) {
            searchButton.doClick();
        } else if (ClientModel.SEARCH_RESULTS_SIZE
                .equals(evt.getPropertyName())) {
            Integer rows = (Integer) evt.getNewValue();
            progressBar.setMaximum(rows);

            refreshProgressBar();

        } else if (ClientModel.SEARCH_PROGRESS_UPDATE.equals(evt
                .getPropertyName())) {

            Integer rows = (Integer) evt.getNewValue();
            progressBar.setValue(rows);

            refreshProgressBar();

        } else if (ClientModel.SEARCH_CANCELLED.equals(evt.getPropertyName())) {
            progressBar.setValue(0);
            progressBar.setString(Localization
                    .getString("client.searchCancelled"));
            progressBar.setToolTipText(Localization
                    .getString("client.searchCancelledTooltip"));

        } else if (ClientModel.TABLE_ROW_COUNT.equals(evt.getPropertyName())) {

            Integer rows = (Integer) evt.getNewValue();
            final String message;
            if (rows == 1) {
                message = Localization.getString("client.rowCountChangedTo1");
            } else {
                message = Localization
                        .getString("client.rowCountChanged", rows);
            }
            addStatusMessage(message);

        } else if (ClientModel.SCHEMA_PROPERTY.equals(evt.getPropertyName())) {

            Object newValue = evt.getNewValue();
            if (newValue instanceof ClientModel) {
                refreshGUI((ClientModel) newValue);
            } else {
                assert false
                 : "Expected a ClientModel for new schema property value";
            }
        } else if (ClientModel.ROWS_DESELECTED.equals(evt.getPropertyName())) {
            table.clearSelection();
        } else if (!(SmartComboBox.ESCAPE_PROPERTY.equals(evt
                .getPropertyName()))) {
            LOGGER.warning("Unknown event given to client view: "
                    + evt.getPropertyName() + ", Details: " + evt);
        }
    }

    /**
     * Get a reference to the table containing the search results.
     *
     * @return the search results table
     */
    SearchResultsTable getTable() {
        return table;
    }

    /**
     * Add a selection listener that will be notified when the user's search
     * results table selection changes.
     *
     * @param listener the selection listener to add
     */
    void addSelectionListener(final TableSelectionListener listener) {
        listenerList.add(listener);
    }

    /**
     * Notify all input components that a search has just been performed.
     */
    void searchPerformed() {
        for (FieldInputComponent input : inputComponentList) {
            if (input != null) {
                input.searchPerformed();
            }
        }
    }

    /**
     * Get the search field criterion for a particular field.
     *
     * @param index the index of the field
     * @return the search criterion for the field
     * @throws IllegalArgumentException if the user's
     *         input could not be parsed
     */
    Criterion getFieldCriterion(final int index) {
        final Criterion result;
        final FieldInputComponent input = inputComponentList.get(index);

        if (input == null) {
            result = null;
        } else {
            result = input.getCriterion();
        }
        return result;
    }

    /**
     * Associate the given action with the given command.
     *
     * @param command the command to configure
     * @param action the action to be performed
     */
    void setAction(final Commands command, final Action action) {
        final JButton button = buttons.get(command);
        if (button != null) {
            button.setAction(action);
        } else {
            assert false : "Could not find button for command: " + command;
        }
    }

    /**
     * Add a message to the client's status bar.
     *
     * @param message the message to add (already localized)
     */
    void addStatusMessage(final String message) {
        statusBar.setText(message);
    }

    /**
     * Get the field viewers that are used to display the field values. There
     * might not necessarily be an entry for every field.
     *
     * @return the field viewers
     */
    Map<Class<? extends Field>, FieldView> getFieldViewers() {
        return fieldViewers;
    }

    /**
     * Get a reference to the client menu bar.
     *
     * @return the client menu bar.
     */
    ClientMenuBar getMenu() {
        return menuBar;
    }

    /**
     * Get a reference to the client toolbar.
     *
     * @return the client toolbar.
     */
    ClientToolbar getToolbar() {
        return toolBar;
    }

    /**
     * Determine if the checkbox for only showing bookable records is checked.
     *
     * @return true if the checkbox is checked
     */
    boolean isOnlyBookableChecked() {
        return bookableOnly.isSelected();
    }

    /**
     * Add a selection listener that tells all listeners in the listener list
     * that the selected row has changed.
     */
    private void addTableListener() {
        ListSelectionListener listener = new ListSelectionListener() {

            public void valueChanged(final ListSelectionEvent e) {

                Integer row = null; //No row selection
                if (!e.getValueIsAdjusting()) {

                    ListSelectionModel lsm =
                        (ListSelectionModel) e.getSource();
                    if (!lsm.isSelectionEmpty()) {
                        int selectedRow = lsm.getMinSelectionIndex();

                        //Select the whole row
                        table.addColumnSelectionInterval(0, table
                                .getColumnCount() - 1);

                        Integer recno = (Integer) table.getModel().getValueAt(
                                selectedRow, ClientModel.HIDDEN_COLUMN);

                        //Only select if one row is selected
                        if (selectedRow == lsm.getMaxSelectionIndex()) {
                            row = recno;
                        }
                    }
                }
                //Forward event to listeners
                for (TableSelectionListener l : listenerList) {
                    l.rowSelected(row);
                }
            }
        };

        //Ask to be notified of selection changes.
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(listener);
    }

    /**
     * Refresh the main panel containing the search form, the results table and
     * the booking button. If there is an existing main panel, it will be
     * removed along with its listeners. A new main panel will be added if
     * required.
     *
     * @param model the client model to create the panel for
     */
    private void refreshGUI(final ClientModel model) {
        resetProgressBar();

        //Tidy up any old fields
        for (FieldInputComponent input : inputComponentList) {
            if (input != null) {
                input.removeListener(this);
            }
        }
        inputComponentList = new ArrayList<FieldInputComponent>();

        TableSchema schema = model.getCurrentSchema();

        List<FieldDetails> newSchema;
        if (schema == null) {
            newSchema = new ArrayList<FieldDetails>();
        } else {
            newSchema = model.getCurrentSchema().getFields();
        }

        //Completely remove the old frame and make a new one
        getContentPane().remove(currentFrame);

        if (newSchema.size() > 0) {
            createGUI(newSchema, model);
        } else {
            //Add a blank panel
            JPanel mainContainer = new JPanel();
            JScrollPane mainScroller = new JScrollPane(mainContainer);
            getContentPane().add(mainScroller, BorderLayout.CENTER);
            currentFrame = mainScroller;
            //Set status bar size back
            statusScroller.setPreferredSize(originalStatusBarSize);
        }
        validate();
    }

    /**
     * Create the main panel containing the search form,
     * the results table and the booking button.
     *
     * @param newSchema the list of details for each field
     * @param model the client model to create the panel for
     */
    private void createGUI(
            final List<FieldDetails> newSchema, final ClientModel model) {

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JPanel mainContainer = new JPanel();

        //Add an input component for each searchable field
        for (FieldDetails details : newSchema) {
            FieldInputComponent newComponent = null;
            if (details.isSearchable()) {
                constraints.gridx = 0;
                constraints.anchor = GridBagConstraints.EAST;

                searchPanel.add(new JLabel(
                    Localization.getStringIfPresent(details.getFieldName())),
                    constraints);

                constraints.anchor = GridBagConstraints.WEST;
                constraints.gridx++;

                FieldInputComponent inputPrototype =
                    entryTypeForField.get(details.getFieldClass());

                //If no custom entry type found, use the default
                if (inputPrototype == null) {
                    FieldParser parser = model.getFieldParsers()
                            .getParserForClass(details.getFieldClass());

                    inputPrototype = new GenericEntryField(true, parser);
                    inputPrototype.addListener(this);
                }

                newComponent = inputPrototype.createClone();
                inputComponentList.add(newComponent);

                searchPanel.add(newComponent.getComponent(), constraints);
                constraints.gridy++;
            } else {
                //This field is not searchable
                inputComponentList.add(null);
            }
        }

        //Add buttons to the search panel
        JPanel searchButtonsPanel = new JPanel();
        searchButtonsPanel.setLayout(new FlowLayout());
        searchButtonsPanel.add(searchButton);
        searchButtonsPanel.add(cancelSearchButton);

        constraints.gridx = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        searchPanel.add(bookableOnly, constraints);
        constraints.gridy++;

        searchPanel.add(searchButtonsPanel, constraints);

        searchPanel.setBorder(BorderFactory.createTitledBorder(
                Localization.getString("client.searchTitle")));

        //Panel to contain the button used to modify records
        JPanel bookingPanel = new JPanel();
        bookingPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(2, 3, 2, 3),
            BorderFactory.createTitledBorder(
                Localization.getString("client.bookSelectedRecordTitle"))));

        bookingPanel.setLayout(new GridBagLayout());

        GridBagConstraints bookButtonConstraints = new GridBagConstraints();
        bookButtonConstraints.insets = new Insets(4, 4, 4, 40);
        bookButtonConstraints.gridy = 0;
        bookButtonConstraints.gridx = 0;
        bookingPanel.add(bookButton, bookButtonConstraints);

        mainContainer.setLayout(new BorderLayout());

        //The left hand panel, contains the search form and buttons.
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());

        GridBagConstraints leftPanelConstraints = new GridBagConstraints();
        leftPanelConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        leftPanelConstraints.fill = GridBagConstraints.VERTICAL;
        leftPanelConstraints.gridx = 0;
        leftPanelConstraints.gridy = 0;
        leftPanel.add(searchPanel, leftPanelConstraints);

        sorter.setTableModel(model);
        table.setModel(sorter);
        sorter.setTableHeader(table.getTableHeader());

        JScrollPane tableScrollPane = new JScrollPane(table);

        tableScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 5, 1, 5,
                        WidgetFactory.LIGHT_BACKGROUND),
                BorderFactory.createLineBorder(
                        WidgetFactory.BACKGROUND, 2)));

        table.setPreferredScrollableViewportSize(new Dimension(1, 1));

        //The right hand panel, contains the table and the booking panel
        JPanel rightPanel = new JPanel();

        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 0, 5, 5),
                BorderFactory.createTitledBorder(
                        Localization.getString("client.searchResultsTitle"))));

        rightPanel.setLayout(new BorderLayout());

        rightPanel.add(tableScrollPane);
        rightPanel.add(bookingPanel, BorderLayout.SOUTH);

        JPanel leftContainer = new JPanel();
        leftContainer.setBorder(null);
        leftContainer.add(leftPanel);

        mainContainer.add(leftContainer, BorderLayout.LINE_START);
        mainContainer.add(rightPanel, BorderLayout.CENTER);

        JScrollPane mainScroller = new JScrollPane(mainContainer);
        getContentPane().add(mainScroller, BorderLayout.CENTER);
        currentFrame = mainScroller;
        //Set status bar size back
        statusScroller.setPreferredSize(originalStatusBarSize);
    }

    /**
     * Clears the progress bar and sets its value to 0.
     * Also clears its tooltip.
     */
    private void resetProgressBar() {
        progressBar.setValue(0);
        progressBar.setMaximum(0);
        progressBar.setString("");
        progressBar.setToolTipText(null);
    }

    /**
     * Refresh the progress bar text and tooltip.
     */
    private void refreshProgressBar() {
        final int current = progressBar.getValue();
        final int max = progressBar.getMaximum();

        final int percentage;
        if (max == 0) {
            //Avoid dividing by zero
            percentage = 0;
        } else {
            percentage = (100 * current) / max;
        }

        progressBar.setString(Localization.getString("client.progressBar.text",
                new Object[] {current, max, percentage}));

        progressBar.setToolTipText(Localization.getString(
                "client.progressBar.tooltip", percentage));
    }

    /**
     * Creates a button and adds a key listener
     * to it so that the user can press
     * enter to activate the button.
     *
     * @param command the command to invoke
     * @return a new button
     */
    private JButton createButton(final Commands command) {
        final JButton item = WidgetFactory.getInstance().createButton();

        buttons.put(command, item);
        return item;
    }

}
