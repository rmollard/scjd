package suncertify;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import suncertify.utils.EditManager;
import suncertify.utils.Localization;

/**
 * A simple non-modal panel to display HTML files. Clicking on a hyperlink in
 * the current HTML file will load the link target in the panel. We provide Back
 * and Forward buttons for easy navigation. There is also a Home button to
 * return to the first page.
 *
 * @author Robert Mollard
 */
public final class HelpPanel extends JDialog {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 8612534628892494023L;

    /**
     * The width of the main frame, as a fraction of the screen width.
     */
    private static final double WIDTH_MULTIPLIER = 0.5;

    /**
     * The height of the main frame, as a fraction of the screen height.
     */
    private static final double HEIGHT_MULTIPLIER = 0.75;

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify");

    /**
     * The pane displaying the current help page.
     */
    private JEditorPane textArea;

    /**
     * The context menu for the help page viewer pane.
     */
    private final JPopupMenu rootMenu;

    /**
     * The navigation history list. This is traversed with the Back and Forward
     * actions.
     */
    private final List<URL> history = new ArrayList<URL>();

    /**
     * The current position in the navigation history list.
     * Index 0 is the first page visited.
     */
    private int currentHistoryPosition = 0;

    /**
     * The action we use to load the previous HTML page.
     */
    private final Action backAction = new BackAction();

    /**
     * The action we use to load the next HTML page.
     */
    private final Action forwardAction = new ForwardAction();

    /**
     * The action we use to go to the first HTML page that was displayed.
     */
    private final Action homeAction = new HomeAction();

    /**
     * The action we use to close the help dialog.
     */
    private final Action closeAction = new CloseAction();

    /**
     * Flag indicating whether or not the help page has been loaded.
     */
    private boolean helpLoaded;

    /**
     * Create a new help panel displaying the given file.
     *
     * @param parent the parent frame. Can be null.
     * @param fileName the filename of the HTML file to load. Must not be null.
     */
    public HelpPanel(final Frame parent, final String fileName) {

        super(parent, Localization.getString("helpPanelTitle",
                        Localization.getString("programName")), false);

        final Dimension screenSize =
            Toolkit.getDefaultToolkit().getScreenSize();

        setSize((int) (screenSize.width * WIDTH_MULTIPLIER),
                (int) (screenSize.height * HEIGHT_MULTIPLIER));

        rootMenu = new JPopupMenu();

        textArea = new JEditorPane();
        textArea.setEditable(false);
        textArea.setContentType("text/html");

        loadPage(fileName);

        EditManager.registerComponentForEditActions(textArea);

        init();

        if (parent != null) {
            setLocationRelativeTo(parent);
        }
    }

    /**
     * Populate the context menu.
     */
    private void createPopupMenu() {

        JMenuItem selectAllItem = new JMenuItem();
        selectAllItem.setAction(new SelectAllAction());
        rootMenu.add(selectAllItem);

        JMenuItem copyItem = new JMenuItem();
        copyItem.setAction(EditManager.getCopyAction());

        rootMenu.add(copyItem);

        textArea.add(rootMenu);
    }

    /**
     * Initialize the help panel, adding listeners and adding components to the
     * content pane.
     */
    private void init() {

        createPopupMenu();

        KeyListener keyListener = (new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    closeAction.actionPerformed(null);
                }
            }
        });
        addKeyListener(keyListener);

        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseReleased(final MouseEvent e) {

                if (e.isPopupTrigger()) {
                    rootMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(final MouseEvent e) {

                if (e.isPopupTrigger()) {
                    rootMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };

        textArea.addKeyListener(keyListener);
        textArea.addMouseListener(mouseListener);

        textArea.addHyperlinkListener(new HyperlinkListener() {
            /** {@inheritDoc} */
            public void hyperlinkUpdate(final HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    JEditorPane pane = (JEditorPane) e.getSource();

                    if (e instanceof HTMLFrameHyperlinkEvent) {
                        HTMLFrameHyperlinkEvent evt =
                            (HTMLFrameHyperlinkEvent) e;
                        HTMLDocument doc = (HTMLDocument) pane.getDocument();
                        doc.processHTMLFrameHyperlinkEvent(evt);
                    } else {
                        goToURL(e.getURL());
                    }
                }
            }
        });

        final Border emptyBorder =
            BorderFactory.createEmptyBorder(5, 5, 5, 5);

        final JPanel mainContainer = new JPanel();

        mainContainer.setLayout(new BorderLayout());

        final JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton backButton = createToolbarButton(backAction, keyListener);
        toolbar.add(backButton);

        JButton forwardButton = createToolbarButton(forwardAction, keyListener);
        toolbar.add(forwardButton);

        toolbar.addSeparator();

        JButton homeButton = createToolbarButton(homeAction, keyListener);
        toolbar.add(homeButton);

        toolbar.addSeparator();

        JButton closeButton = createToolbarButton(closeAction, keyListener);
        toolbar.add(closeButton, BorderLayout.EAST);

        toolbar.setFloatable(false);

        refreshNavigationButtons();

        getContentPane().add(toolbar, BorderLayout.NORTH);

        JScrollPane scroller = new JScrollPane(textArea);

        JPanel logPanel = new JPanel();
        logPanel.setLayout(new GridLayout(1, 1));
        logPanel.add(scroller);

        logPanel.setBorder(emptyBorder);
        mainContainer.add(logPanel);

        getContentPane().add(mainContainer, BorderLayout.CENTER);
    }

    /**
     * Load the HTML page with the given filename.
     *
     * @param fileName the filename, relative to the suncertify package.
     */
    private void loadPage(final String fileName) {
        try {
            final URL resource = HelpPanel.class.getResource(fileName);

            if (resource == null) {
                helpLoaded = false;

                textArea.setText(
                        Localization.getString("help.couldNotLoadFile"));

                LOGGER.warning("Could not find help file: " + fileName);
            } else {
                helpLoaded = true;
                textArea.setPage(resource);
                history.add(resource);
            }

        } catch (IOException e) {
            textArea.setText(Localization.getString("help.couldNotLoadFile"));
            LOGGER.warning("Could not load " + fileName + " due to " + e);
        }
    }

    /**
     * Create a new toolbar button for the given action.
     * The given key listener will be added to the button.
     *
     * @param action the action to be performed when the button
     *        is activated
     * @param keyListener the key listener to add to the button
     * @return the new toolbar button
     */
    private JButton createToolbarButton(final Action action,
            final KeyListener keyListener) {
        JButton button = new JButton(action);
        button.addKeyListener(keyListener);
        button.setFocusable(false);

        return button;
    }



    /**
     * Action to select all text in the help page viewing pane.
     */
    private final class SelectAllAction extends AbstractAction {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = 7222382793534669274L;

        private SelectAllAction() {
            putValue(Action.NAME, Localization
                    .getString("help.selectAll.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("help.selectAll.description"));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                    .getChar("help.selectAll.mnemonic")));

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_A, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(final ActionEvent e) {
            textArea.requestFocusInWindow();
            textArea.selectAll();
        }
    }

    /**
     * Action to go back to the previously displayed help page.
     */
    private final class BackAction extends AbstractAction {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = -7960365076051355066L;

        private BackAction() {
            putValue(Action.NAME, Localization
                    .getString("help.back.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("help.back.description"));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                    .getChar("help.back.mnemonic")));
            putValue(Action.SMALL_ICON, IconFactory.getImageIcon(Localization
                    .getString("help.back.icon")));

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_B, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(final ActionEvent e) {
            goBack();
        }
    }

    /**
     * Action to go forward to the next help page in the history list.
     */
    private final class ForwardAction extends AbstractAction {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = -3003995699682287611L;

        private ForwardAction() {
            putValue(Action.NAME, Localization
                    .getString("help.forward.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("help.forward.description"));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                    .getChar("help.forward.mnemonic")));
            putValue(Action.SMALL_ICON, IconFactory.getImageIcon(Localization
                    .getString("help.forward.icon")));

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_F, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(final ActionEvent e) {
            goForward();
        }
    }

    /**
     * Action to go to the first help page that was displayed.
     */
    private final class HomeAction extends AbstractAction {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = -1215362753803252646L;

        private HomeAction() {
            putValue(Action.NAME, Localization
                    .getString("help.home.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("help.home.description"));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                    .getChar("help.home.mnemonic")));
            putValue(Action.SMALL_ICON, IconFactory.getImageIcon(Localization
                    .getString("help.home.icon")));

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_H, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(final ActionEvent e) {
            goHome();
        }
    }

    /**
     * Action to close the help dialog.
     */
    private final class CloseAction extends AbstractAction {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = -8024971074532997915L;

        private CloseAction() {
            putValue(Action.NAME, Localization
                    .getString("help.close.name"));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("help.close.description"));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                    .getChar("help.close.mnemonic")));
        }

        public void actionPerformed(final ActionEvent e) {
            HelpPanel.this.setVisible(false);
        }
    }

    /**
     * Goes to a new URL and adds it to the history list. The history position
     * gets incremented.
     *
     * @param url the URL to go to
     */
    public void goToURL(final URL url) {

        try {
            textArea.setPage(url);

            //Clear all history after current position
            final int upper = history.size();

            /*
             * Do this last-to-first because remove() moves
             * the other elements to the left.
             */
            for (int i = upper - 1; i > currentHistoryPosition; i--) {
                history.remove(i);
            }

            history.add(url);
            currentHistoryPosition++;
            refreshNavigationButtons();
        } catch (IOException e) {
            LOGGER.warning("Could not load " + url + " due to " + e);
        }

    }

    /**
     * Enable or disable the Back and Forward actions. The Back action will be
     * enabled if there is a previous page in the history list. The Forward
     * action will be enabled if there is a page after the current one in the
     * history list.
     */
    private void refreshNavigationButtons() {
        backAction.setEnabled(currentHistoryPosition > 0);
        forwardAction.setEnabled(currentHistoryPosition < history.size() - 1);
    }

    /**
     * Load the page that was originally displayed.
     * This does nothing if the
     * home page is already being displayed.
     */
    private void goHome() {
        if (helpLoaded) {
            if (!textArea.getPage().toString().equals(
                    history.get(0).toString())) {
                goToURL(history.get(0));
            }
        }
    }

    /**
     * Load the previous page in the history list.
     */
    private void goBack() {
        //If we have a position to go back to
        if (currentHistoryPosition > 0) {
            currentHistoryPosition--;

            try {
                textArea.setPage(history.get(currentHistoryPosition));
            } catch (IOException e) {
                LOGGER.warning("Could not go back due to " + e);
                currentHistoryPosition++;
            }
            refreshNavigationButtons();
        }
    }

    /**
     * Load the next page in the history list.
     */
    private void goForward() {
        //If we are not at the end of the history list
        if (currentHistoryPosition < history.size() - 1) {
            currentHistoryPosition++;

            try {
                textArea.setPage(history.get(currentHistoryPosition));
            } catch (IOException e) {
                LOGGER.warning("Could not go forward due to " + e);
                currentHistoryPosition--;
            }
            refreshNavigationButtons();
        }
    }

}
