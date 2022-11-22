package suncertify.panels;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import suncertify.ProgressListener;
import suncertify.utils.Localization;

/**
 * This class provides a simple dialog box that contains a
 * progress bar and a cancel button. The cancel button
 * is provided to allow the operation to be cancelled when pressed.
 *
 * @author Robert Mollard
 */
public final class ProgressDialog extends JDialog implements ProgressListener {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -792262176996381548L;

    /**
     * The progress bar.
     */
    private JProgressBar progressBar = null;

    /**
     * The text message displayed in the dialog.
     */
    private JLabel textLabel = null;

    /**
     * Button used to cancel the operation.
     */
    private JButton cancelButton = null;

    /**
     * Flag indicating that the user cancelled the operation.
     */
    private boolean cancelled;

    /**
     * Creates a non-modal dialog without a title with the
     * specified <code>Frame</code> as its owner.  If <code>owner</code>
     * is <code>null</code>, a shared, hidden frame will be set as the
     * owner of the dialog.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed
     * @param text the text to display
     * @throws java.awt.HeadlessException if
     *         GraphicsEnvironment.isHeadless() returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see javax.swing.JComponent#getDefaultLocale
     */
    public ProgressDialog(final Frame owner, final String text) {
        super(owner);
        initComponents(text);
        setLocationRelativeTo(owner);
        setResizable(false);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
    }

    /**
     * Show or hide the dialog based on the value
     * of the given parameter.
     *
     * @param visible true to set visible, or false to set invisible
     */
    @Override
    public void setVisible(final boolean visible) {
        if (visible) {
            cancelled = false;
            setCurrentValue(0);
        }
        super.setVisible(visible);

        cancelButton.requestFocusInWindow();
    }

    /** {@inheritDoc} */
    public void setMaxValue(final int max) {
        progressBar.setMaximum(max);
    }

    /** {@inheritDoc} */
    public void setCurrentValue(final int current) {
        progressBar.setValue(current);
        refreshProgressBarText();
    }

    /**
     * Redraws the text on the progress bar. This will need
     * to be called when the progress changes.
     */
    private void refreshProgressBarText() {
        final int current = progressBar.getValue();
        final int max = progressBar.getMaximum();

        final int percent;
        if (max == 0) {
            //Avoid dividing by 0
            percent = 0;
        } else {
            percent = (100 * current) / max;
        }
        progressBar.setString(percent + "%");
    }

    /** {@inheritDoc} */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the text to be displayed in the dialog.
     *
     * @param text the text to display
     */
    public void setText(final String text) {
        textLabel.setText(text);
    }

    /**
     * Create the dialog interface components.
     *
     * @param text the text to display
     */
    private void initComponents(final String text) {
        final GridBagConstraints gridBagConstraints;

        textLabel = new JLabel(text);
        progressBar = new JProgressBar();

        final Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        contentPane.add(textLabel, gridBagConstraints);

        gridBagConstraints.gridwidth = 1;

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        contentPane.add(progressBar, gridBagConstraints);

        cancelButton = new JButton(Localization
                .getString("dialog.cancelButton"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                cancelled = true;
                setVisible(false);
            }
        });

        cancelButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                /*
                 * Allow user to cancel dialog by pressing
                 * enter or escape.
                 */
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE
                        || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    cancelButton.doClick();
                }
            }
        });

        gridBagConstraints.gridx++;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        contentPane.add(cancelButton, gridBagConstraints);

        pack();
    }

}
