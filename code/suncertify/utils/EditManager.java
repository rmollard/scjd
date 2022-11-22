package suncertify.utils;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

import suncertify.IconFactory;

/**
 * This class provides cut, copy and paste actions that are enabled
 * and disabled depending on the component that currently has focus.
 *
 * Call the <code>registerComponentForEditActions</code> method for each
 * GUI component that will require cut, copy or paste actions.
 *
 * @author Robert Mollard
 */
public final class EditManager {

    /**
     * Superclass for edit actions such as cut and copy.
     * The action is enabled or disabled based on which
     * component currently has focus.
     */
    private abstract static class EditAction
        extends AbstractAction implements PropertyChangeListener {

        /**
         * Simple listener class to update the actions when
         * a change of focus occurs.
         */
        private static final class ActionUpdater
            implements PropertyChangeListener {
            public void propertyChange(final PropertyChangeEvent e) {
                if (e.getPropertyName().equals("permanentFocusOwner")) {
                    updateActions((Component) e.getNewValue());
                }
            }
        }

        /**
         * List of edit actions created.
         */
        private static List<WeakReference<EditAction>> editActions;

        /**
         * The component that currently has focus.
         */
        private JComponent focusedComponent;

        /**
         * Called when the focus changes to a different component.
         *
         * @param focusedComponent the component that has focus now
         */
        private static void updateActions(final Component focusedComponent) {
            Iterator<WeakReference<EditAction>> actionIterator =
                editActions.iterator();

            while (actionIterator.hasNext()) {
                EditAction action = actionIterator.next().get();
                if (action == null) {
                    actionIterator.remove();
                } else {
                    action.setFocusedComponent(focusedComponent);
                }
            }
        }

        private EditAction() {
            if (editActions == null) {
                editActions = new ArrayList<WeakReference<EditAction>>();

                KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .addPropertyChangeListener(new ActionUpdater());
            }
            editActions.add(new WeakReference<EditAction>(this));
        }

        /**
         * Get the component that currently has focus.
         *
         * @return the focused component
         */
        protected JComponent getFocusedComponent() {
            return focusedComponent;
        }

        /**
         * This method should be called when the focus may have changed
         * to a different component.
         */
        protected void setFocusedComponent() {
            setFocusedComponent(KeyboardFocusManager
                .getCurrentKeyboardFocusManager().getPermanentFocusOwner());
        }

        /**
         * Set the focused component to the given component.
         *
         * @param component the new focus owner. May be null.
         */
        protected void setFocusedComponent(final Component component) {
            final JComponent focused;

            if (component instanceof JComponent) {
                focused = (JComponent) component;
            } else {
                focused = null;
            }
            setFocusedComponent(focused);
        }

        /**
         * Set the focused component to the given component.
         *
         * @param component the new focus owner. May be null.
         */
        protected void setFocusedComponent(final JComponent component) {
            if (focusedComponent != null) {
                focusedComponent.removePropertyChangeListener(this);
            }
            focusedComponent = component;
            if (focusedComponent == null) {
                setEnabled(false);
            } else {
                focusedComponent.addPropertyChangeListener(this);
            }
        }

    }

    /**
     * A general purpose cut action.
     */
    @SuppressWarnings("serial")
    private static final class CutAction extends EditAction {

        private CutAction() {
            putValue(Action.NAME, Localization
                    .getString("menu.cut.name"));
            putValue(Action.SMALL_ICON, IconFactory.getImageIcon(Localization
                    .getString("menu.cut.icon")));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("menu.cut.description"));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                    .getChar("menu.cut.mnemonic")));

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_X,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

            setFocusedComponent();
        }

        public void actionPerformed(final ActionEvent e) {
            JComponent component = getFocusedComponent();

            component.getTransferHandler().exportToClipboard(component,
                    CLIPBOARD, TransferHandler.MOVE);
        }

        @Override
        protected void setFocusedComponent(final JComponent component) {
            boolean isValidTarget = false;

            if (component != null) {
                TransferHandler handler = component.getTransferHandler();
                if (handler != null) {
                    int actions = component.getTransferHandler()
                            .getSourceActions(component);
                    if ((actions & TransferHandler.MOVE) != 0) {
                        super.setFocusedComponent(component);
                        updateEnabledStatus();
                        isValidTarget = true;
                    }
                }
            }
            if (!isValidTarget) {
                super.setFocusedComponent(null);
            }
        }

        /**
         * Enable or disable the action depending on whether
         * the focused component can cut the current selection.
         */
        private void updateEnabledStatus() {
            setEnabled(isCutEnabled(getFocusedComponent()));
        }

        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(CUT_ENABLED_PROPERTY)) {
                updateEnabledStatus();
            }
        }
    }

    /**
     * A general purpose copy action.
     */
    @SuppressWarnings("serial")
    private static final class CopyAction extends EditAction {

        private CopyAction() {

            putValue(Action.NAME, Localization
                    .getString("menu.copy.name"));
            putValue(Action.SMALL_ICON, IconFactory.getImageIcon(Localization
                    .getString("menu.copy.icon")));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("menu.copy.description"));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                    .getChar("menu.copy.mnemonic")));

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_C,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

            setFocusedComponent();
        }

        public void actionPerformed(final ActionEvent e) {
            JComponent component = getFocusedComponent();
            component.getTransferHandler().exportToClipboard(component,
                    CLIPBOARD, TransferHandler.COPY);
        }

        @Override
        protected void setFocusedComponent(final JComponent component) {
            boolean isValidTarget = false;

            if (component != null) {
                TransferHandler handler = component.getTransferHandler();
                if (handler != null) {
                    int actions = component.getTransferHandler()
                            .getSourceActions(component);

                    if ((actions & TransferHandler.COPY) != 0) {
                        super.setFocusedComponent(component);
                        updateEnabledStatus();
                        isValidTarget = true;
                    }
                }
            }
            if (!isValidTarget) {
                super.setFocusedComponent(null);
            }
        }

        /**
         * Enable or disable the action depending on whether
         * the focused component can copy the current selection.
         */
        private void updateEnabledStatus() {
            setEnabled(isCopyEnabled(getFocusedComponent()));
        }

        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(COPY_ENABLED_PROPERTY)) {
                updateEnabledStatus();
            }
        }
    }

    /**
     * A general purpose paste action.
     */
    @SuppressWarnings("serial")
    private static final class PasteAction extends EditAction {

        private PasteAction() {
            putValue(Action.NAME, Localization
                    .getString("menu.paste.name"));
            putValue(Action.SMALL_ICON, IconFactory.getImageIcon(Localization
                    .getString("menu.paste.icon")));
            putValue(Action.SHORT_DESCRIPTION, Localization
                    .getString("menu.paste.description"));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(Localization
                    .getChar("menu.paste.mnemonic")));

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_V,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

            CLIPBOARD.addFlavorListener(new FlavorListener() {
                public void flavorsChanged(final FlavorEvent e) {
                    if (getFocusedComponent() != null) {
                        updateEnabledStatus();
                    }
                }
            });
            setFocusedComponent();
        }

        @Override
        protected void setFocusedComponent(final JComponent component) {
            if (component != null && getDataFlavors(component) != null) {
                super.setFocusedComponent(component);
                updateEnabledStatus();
            } else {
                super.setFocusedComponent(null);
            }
        }

        /**
         * Enable or disable the action depending on whether
         * the focused component can accept any of
         * the data flavors in the clipboard.
         */
        private void updateEnabledStatus() {
            boolean enable = false;
            final JComponent focused = getFocusedComponent();

            if (isPasteEnabled(focused)) {
                Clipboard clipboard = CLIPBOARD;
                try {
                    final DataFlavor[] flavors = getDataFlavors(focused);
                    if (flavors != null) {
                        for (int i = 0; i < flavors.length && !enable; i++) {
                            if (clipboard.isDataFlavorAvailable(flavors[i])) {
                                enable = true;
                            }
                        }
                    }
                } catch (IllegalStateException e) {
                    LOGGER.warning("Clipboard unavailable");
                }
            }
            setEnabled(enable);
        }

        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(PASTE_ENABLED_PROPERTY)) {
                updateEnabledStatus();
            }
        }

        public void actionPerformed(final ActionEvent e) {
            JComponent target = getFocusedComponent();
            target.getTransferHandler().importData(target,
                    CLIPBOARD.getContents(null));
        }
    }

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify");

    /**
     * General purpose cut action. This can be shared among many
     * different components.
     */
    private static final Action CUT_ACTION;

    /**
     * General purpose copy action. This can be shared among many
     * different components.
     */
    private static final Action COPY_ACTION;

    /**
     * General purpose paste action. This can be shared among many
     * different components.
     */
    private static final Action PASTE_ACTION;

    /**
     * Property change string used when the Cut action is
     * enabled or disabled.
     */
    private static final String CUT_ENABLED_PROPERTY = "cutProperty";

    /**
     * Property change string used when the Copy action is
     * enabled or disabled.
     */
    private static final String COPY_ENABLED_PROPERTY = "copyProperty";

    /**
     * Property change string used when the Paste action is
     * enabled or disabled.
     */
    private static final String PASTE_ENABLED_PROPERTY = "pasteProperty";

    /**
     * Component property key for the data flavors that
     * can be pasted.
     */
    private static final Object PASTE_FLAVORS_PROPERTY = "flavorsProperty";

    /**
     * Clipboard used for cut, copy and paste.
     * We use the system clipboard if it is available.
     */
    private static final Clipboard CLIPBOARD;

    static {
        Clipboard clipboardToUse;
        try {
            clipboardToUse = Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (SecurityException e) {
            //Don't have access to the clipboard, create a new one
            clipboardToUse = new Clipboard("EditManager Clipboard");
        }
        CLIPBOARD = clipboardToUse;
        CUT_ACTION = new CutAction();
        COPY_ACTION = new CopyAction();
        PASTE_ACTION = new PasteAction();
    }

    /**
     * The plain string data flavors.
     */
    private static final DataFlavor[] PLAIN_TEXT;

    static {
        try {
            PLAIN_TEXT = new DataFlavor[] {
                    DataFlavor.stringFlavor,
                    new DataFlavor("text/plain;class=java.lang.String"),
                    new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
                            + ";class=java.lang.String") };
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error creating text options: ", e);
        }
    }

    /**
     * Private constructor to prevent the class from being instantiated.
     */
    private EditManager() {
        //Prevent instantiation
    }

    /**
     * Registers edit actions for the given component.
     *
     * @param component the component to register
     */
    public static void registerComponentForEditActions(
            final JComponent component) {
        component.putClientProperty(PASTE_FLAVORS_PROPERTY, PLAIN_TEXT);
        EditManager.registerKeyBindings(component);

        /*
         * If the component is a text component,
         * add a caret listener so that we can dynamically enable the
         * edit actions depending on text selection.
         */
        if (component instanceof JTextComponent) {
            JTextComponent comp = (JTextComponent) component;

            comp.addCaretListener(new CaretListener() {
                public void caretUpdate(final CaretEvent e) {
                    final Object source = e.getSource();
                    if (source instanceof JTextComponent) {
                        final JTextComponent text =
                            (JTextComponent) e.getSource();
                        final Caret caret = text.getCaret();
                        final boolean editable = text.isEditable();
                        final boolean hasSelection =
                            (caret.getDot() != caret.getMark());

                        EditManager.setCopyEnabled(text, hasSelection);
                        EditManager.setPasteEnabled(text, editable);
                        EditManager.setCutEnabled(text,
                                editable && hasSelection);
                    }
                }
            });
        }
    }

    /**
     * Returns an action to perform a cut operation.
     *
     * @return the cut action
     */
    public static Action getCutAction() {
        return CUT_ACTION;
    }

    /**
     * Returns an action to perform a copy operation.
     *
     * @return the copy action
     */
    public static Action getCopyAction() {
        return COPY_ACTION;
    }

    /**
     * Returns an action to perform a paste operation.
     *
     * @return the paste action
     */
    public static Action getPasteAction() {
        return PASTE_ACTION;
    }

    /**
     * Specifies whether the component can perform a cut operation.
     *
     * @param component the Component to set the enabled state for
     * @param enable true if component supports cut
     */
    public static void setCutEnabled(
            final JComponent component, final boolean enable) {
        component.putClientProperty(CUT_ENABLED_PROPERTY, enable);
    }

    /**
     * Returns whether the component can perform a cut operation.
     *
     * @param component the component to test
     * @return true if the component can cut
     */
    public static boolean isCutEnabled(final JComponent component) {
        return getBoolean(component, CUT_ENABLED_PROPERTY);
    }

    /**
     * Specifies whether the component can perform a copy operation.
     *
     * @param component the Component to set the enabled state for
     * @param enable true if component supports copy
     */
    public static void setCopyEnabled(
            final JComponent component, final boolean enable) {
        component.putClientProperty(COPY_ENABLED_PROPERTY, enable);
    }

    /**
     * Returns whether the component can perform a copy operation.
     *
     * @param component the component to test
     * @return true if the component can copy
     */
    public static boolean isCopyEnabled(final JComponent component) {
        return getBoolean(component, COPY_ENABLED_PROPERTY);
    }

    /**
     * Specifies whether the component can perform a paste operation.
     *
     * @param component the Component to set the enabled state for
     * @param enable true if component supports paste
     */
    public static void setPasteEnabled(
            final JComponent component, final boolean enable) {
        component.putClientProperty(PASTE_ENABLED_PROPERTY, enable);
    }

    /**
     * Returns whether the component can perform a paste operation.
     *
     * @param component the component to test
     * @return true if the component can paste
     */
    public static boolean isPasteEnabled(final JComponent component) {
        return getBoolean(component, PASTE_ENABLED_PROPERTY);
    }

    /**
     * Get a boolean client property for the given component.
     *
     * @param component the component to get the property for
     * @param property the property to get
     * @return the client property, or false if the property
     *         could not be found
     */
    private static boolean getBoolean(
            final JComponent component, final Object property) {
        Boolean value = (Boolean) component.getClientProperty(property);
        boolean result = false;
        if (value != null) {
            result = value;
        }
        return result;
    }

    /**
     * Get the data flavors that the given component can accept.
     *
     * @param component the component
     * @return an array of the accepted data flavors
     */
    private static DataFlavor[] getDataFlavors(final JComponent component) {
        return (DataFlavor[]) component
                .getClientProperty(PASTE_FLAVORS_PROPERTY);
    }

    /**
     * Registers the appropriate key bindings for cut, copy, and paste on the
     * specified component. Registered bindings target the actions provided by
     * this class.
     *
     * @param component the component to register bindings for
     */
    private static void registerKeyBindings(final JComponent component) {
        InputMap inputMap = component.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()), COPY_ACTION);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()), CUT_ACTION);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()), PASTE_ACTION);

        //Sun systems have special keyboard buttons for copy, cut and paste
        inputMap.put(KeyStroke.getKeyStroke("COPY"), COPY_ACTION);
        inputMap.put(KeyStroke.getKeyStroke("CUT"), CUT_ACTION);
        inputMap.put(KeyStroke.getKeyStroke("PASTE"), PASTE_ACTION);

        inputMap.put(KeyStroke.getKeyStroke("ctrl INSERT"), COPY_ACTION);
        inputMap.put(KeyStroke.getKeyStroke("shift DELETE"), CUT_ACTION);
        inputMap.put(KeyStroke.getKeyStroke("shift INSERT"), PASTE_ACTION);

        ActionMap actionMap = component.getActionMap();
        actionMap.put(CUT_ACTION, CUT_ACTION);
        actionMap.put(COPY_ACTION, COPY_ACTION);
        actionMap.put(PASTE_ACTION, PASTE_ACTION);
        actionMap.put(CUT_ACTION, CUT_ACTION);
    }

}
