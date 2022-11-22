package suncertify.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import suncertify.PersistentConfiguration;
import suncertify.SavedParameter;
import suncertify.utils.FilenameComboBox;
import suncertify.utils.Localization;
import suncertify.utils.SmartComboBox;


/**
 * A dialog box for entering a filename.
 * The filename is entered in an editable combo box. The combo
 * box menu consists of previously entered filenames.
 * We also provide a browse button so that the user can locate
 * the file using a normal file browser.
 * The combo box provides a popup menu that lists the files
 * The OK button becomes disabled if the combo box editor
 * text is not a valid filename.
 *
 * Thread safety: not thread safe
 *
 * @author Robert Mollard
 */
public final class FileOpenDialog extends AbstractInputDialog {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -8212026302277144357L;

    /**
     * Factor to increase the normal width of the combo box by.
     */
    private static final int WIDTH_MULTIPLIER = 11;

    /**
     * The file extension used by the database
     * files, for example ".db".
     */
    private final String suffix;

    /**
     * Autocomplete support for the combo box.
     * We generate a list of files that match the
     * current text in the combo box.
     */
    private final FilenameComboBox fileNameCombo;

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify");

    /**
     * Construct a new file open dialog box.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed
     * @param title the text to display in the dialog's
     *            title bar
     * @param suffix the text that filenames must end with in order to
     *        be accepted
     */
    public FileOpenDialog(final Frame owner,
            final String title, final String suffix) {
        super(owner, title, SavedParameter.SERVER_DATABASE_PATH);
        this.suffix = suffix;

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(1, 2, 1, 2);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;

        panel.add(getErrorLabel(), c);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;

        int oldHeight = getCombo().getPreferredSize().height;
        //Make the combo box wider so we can see the filename more easily
        int newWidth = getCombo().getPreferredSize().width * WIDTH_MULTIPLIER;
        getCombo().setPreferredSize(new Dimension(newWidth, oldHeight));

        fileNameCombo = new FilenameComboBox(getCombo());
        fileNameCombo.setSuffix(suffix);
        fileNameCombo.setMatchingReadOnlyFiles(false);

        fileNameCombo.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent evt) {
                if (SmartComboBox.ESCAPE_PROPERTY.equals(
                                        evt.getPropertyName())) {
                    getCancelButton().doClick();
                } else if (SmartComboBox.ENTER_PROPERTY.equals(
                                        evt.getPropertyName())) {
                    getOkButton().doClick();
                }
            }
        });

        panel.add(getCombo(), c);
        c.gridx++;

        String inputString = PersistentConfiguration.getInstance()
                .getParameter(SavedParameter.SERVER_DATABASE_PATH);
        if (inputString == null) {
            inputString = ""; //Blank if not found
        }

        setInitialText(inputString);

        final JButton browseButton = new JButton(Localization
                .getString("dialog.browseButtonText"));
        browseButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    getCancelButton().doClick();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    browseButton.doClick();
                }
            }
        });
        browseButton.addActionListener(new BrowseForDatabase());

        panel.add(browseButton, c);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(getOkButton());
        buttonPanel.add(getCancelButton());
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(buttonPanel, c);
        setResizable(false);

        final JOptionPane optionPane = new JOptionPane(panel,
                JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null,
                new Object[] {getOkButton(), getCancelButton()});

        setContentPane(optionPane);
        pack();

        getCombo().requestFocusInWindow();
    }

    /** {@inheritDoc} */
    @Override
    protected String getErrorLabelText() {
        return Localization.getString("dialog.openFileLabel");
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return Localization.getString("client.directConnection",
                getInputString());
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isValidInput(final String input) {
        boolean readWrite = false;
        File testFile = new File(input);

        if (testFile.exists()) {
            if (testFile.isFile()) {
                readWrite = testFile.canRead() && testFile.canWrite();
            }
        }
        return readWrite && input != null && input.endsWith(suffix);
    }

    /**
     * A utility class that provides the user with the ability to browse for
     * the database rather than forcing them to remember (and type in) a fully
     * qualified database location.
     */
    private class BrowseForDatabase implements ActionListener {

        /**
         * Description of the database file type.
         */
        private final String description =
            Localization.getString("databaseFileFilter.description");

        /**
         * File chooser for selecting database files.
         */
        private JFileChooser chooser =
            new JFileChooser(System.getProperty("user.dir"));

        public void actionPerformed(final ActionEvent e) {

            chooser.addChoosableFileFilter(
                    new javax.swing.filechooser.FileFilter() {

                        @Override
                        public boolean accept(final File f) {
                            /*
                             * Display files ending with the database
                             * extension or any other object
                             * (directory or other selectable device).
                             */
                            boolean accept = true;
                            if (f.isFile()) {
                                accept = f.canRead() && f.canWrite()
                                        && f.getName().endsWith(suffix);
                            }
                            return accept;
                        }

                        @Override
                        public String getDescription() {
                            return description;
                        }
                    });

            //If the user selected a file, update the file name on screen
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

                Component editorComponent =
                    getCombo().getEditor().getEditorComponent();
                if (editorComponent instanceof JTextComponent) {
                    JTextComponent editor = (JTextComponent) editorComponent;
                    try {
                        editor.setText(
                                chooser.getSelectedFile().getCanonicalPath());
                    } catch (IOException ex) {
                        LOGGER.warning("IO exception on selecting file:" + ex);
                    }
                    fileNameCombo.hideMatches();
                }
            }
        }
    }

}
