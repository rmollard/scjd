package suncertify.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

/**
 * A decorator class for an editable combo box.
 * This provides a popup menu of the list items that
 * match the currently entered text, in order to reduce the amount of typing
 * that the user has to do, and to avoid having to
 * look through a large popup menu.
 *
 * The user can display the full item list by clicking
 * the combo box button in the normal way,
 * or by pressing Ctrl + down (or Meta + down).
 *
 * This class contains a workaround for Bug 5100422 - Hide Popup on focus loss.
 *
 * Thread safety: Not thread safe.
 *
 * @author Robert Mollard
 */
public class SmartComboBox implements Serializable {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -734400411320690676L;

    /**
     * Property name of property change event that is fired
     * when the user presses the escape key when neither popup
     * menu is visible.
     */
    public static final String ESCAPE_PROPERTY = "escapeProperty";

    /**
     * Property name of property change event that is fired
     * when the user presses the enter key when neither popup
     * menu is visible.
     */
    public static final String ENTER_PROPERTY = "enterProperty";

    /**
     * The background color of the list of matches (light blue).
     */
    private static final Color BACKGROUND = new Color(0xeefaff);

    /**
     * The maximum number of rows to display in the match list.
     */
    private static final int MATCH_ROW_COUNT = 12;

    /**
     * The list of items matching the current input.
     */
    private final JList matchList = new JList();

    /**
     * Popup menu displaying the current match list.
     */
    private final JPopupMenu matchMenu = new JPopupMenu();

    /**
     * Document used to restrict user input.
     */
    private Document filter;

    /**
     * If true, selected match text will be appended to the current editor text
     * and the list of popups will appear below the cursor.
     * If false, selected match text will replace the current editor text
     * and the list of popups will always appear
     * below the left of the combo box.
     */
    private boolean append = false;

    /**
     * Wrapping when pressing the up or down arrows to traverse the list
     * of matches.
     * If true, wrap around when we exceed the end or start of the list.
     * If false, just stay at the start or end (like a normal JComboBox popup).
     */
    private boolean selectionWrapping = false;

    /**
     * If true, all the text in the combo box editor will be selected when
     * the combo box gains focus. This makes it easier to delete the
     * current text, but harder to append to it.
     */
    private boolean textHighlightedOnFocusGain = false;

    /**
     * If true, the combo box editor text will be automatically completed
     * to the first match. This can save keystrokes when entering existing
     * values, but can make it harder to enter new values.
     */
    private boolean autoCompleting = false;

    /**
     * If true, only case sensitive matches
     * will be displayed in the match list.
     */
    private boolean caseSensitive = false;

    /**
     * Flag to indicate that we have just accepted an item.
     */
    private boolean acceptedListItem = false;

    /**
     * The combo box being decorated.
     */
    private final JComboBox combo;

    /**
     * The combo box editor.
     */
    private final JTextComponent editor;

    /**
     * List of key listeners that were added.
     * We forward "enter" and "escape" key presses if the
     * match menu is not visible.
     */
    private final PropertyChangeSupport changeSupport;

    /**
     * Basic class to render a tooltip for each combo box item.
     */
    private static class TipRenderer extends BasicComboBoxRenderer {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = 7475804965079516387L;

        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value, final int index,
                final boolean isSelected, final boolean cellHasFocus) {

            setToolTipText(value.toString());

            return super.getListCellRendererComponent(list, value, index,
                    isSelected, cellHasFocus);
        }
    }

    /**
     * Simple mouse listener to select items from the match list.
     */
    private class SmartMouseListener extends MouseAdapter implements
            Serializable {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = 3389649743486990041L;

        @Override
        public void mouseReleased(final MouseEvent e) {
            /*
             * We use mouseReleased so that the user can
             * drag-release on an item, in the same manner
             * as a normal JComboBox.
             */
            acceptedListItem(matchList.getSelectedValue().toString());
            hideMatches();
        }
    }

    /**
     * Auto complete document. The editor text can be
     * automatically completed to the first matching item
     * in the match list.
     */
    private class AutoCompleteDocument extends PlainDocument {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = 8209183090181551515L;

        @Override
        public void insertString(final int offs,
                final String str, final AttributeSet a)
                throws BadLocationException {

            //The string to insert
            String insertString = str;

            //True if we modify the text to insert due to autocompletion
            boolean autocompleted = false;

            final int caretPosition = offs + str.length();

            final String currentText = editor.getText();

            //Work out what the full text will be if str is inserted
            String prefix = "";
            if (offs > 0) {
                prefix = currentText.substring(0, offs);
            }

            //The text after the insertion point
            String suffix = "";
            if (offs < currentText.length()) {
                suffix = currentText.substring(offs, currentText.length());
            }

            //The string we would insert in the absence of autocompletion
            final String editedString = prefix + str + suffix;

            //Update matches
            Object[] matchingItems = getMatchingItems(editedString);

            //If we want to autocomplete and have a match and no suffix
            if (autoCompleting && matchingItems.length > 0
                    && suffix.length() == 0) {

                //Don't autocomplete list items that are only one character
                if (matchingItems[0].toString().length() > 1) {

                    //The first match
                    final String matchString = matchingItems[0].toString();

                    //Where we would insert the entire match
                    final int insertionPoint =
                        getOffsetForMatchList(prefix + str);

                    //How many characters of the match string to skip
                    final int skipLength =
                        offs + str.length() - insertionPoint;

                    String stringToAppend = matchString;

                    if (skipLength > 0) {
                        stringToAppend = matchString.substring(skipLength);
                    }

                    insertString = str + stringToAppend;
                    autocompleted = true;
                }
            }

            if (filter != null) {
                filter.insertString(offs, editedString, null);
            }

            super.insertString(offs, insertString, a);

            /*
             * If an exact match for an existing item was entered, the user
             * probably doesn't want to see the list of matches.
             */
            if (offs == 0 && exactMatchInserted(str)) {
                hideMatches();
            } else {
                if (autocompleted) {
                    //Highlight the autocompleted text
                    editor.setCaretPosition(insertString.length() + offs);
                    editor.moveCaretPosition(caretPosition);
                }
                //Show matches but not the full list
                showMatchesOrFullList(editedString, false);
            }
        }
    }

    /**
     * Focus listener that highlights the editor text on
     * focus gain if appropriate,
     * and hides the combo box list and the match list when focus
     * is lost.
     */
    private class SmartFocusListener extends FocusAdapter implements
            Serializable {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = -1368706187320461155L;

        @Override
        public void focusGained(final FocusEvent e) {

            if (textHighlightedOnFocusGain
                    && e.getOppositeComponent() != combo) {

                editor.setCaretPosition(0);
                editor.moveCaretPosition(editor.getText().length());
            }
        }

        @Override
        public void focusLost(final FocusEvent e) {
            /*
             * Hide our list of matches if we have lost focus, but not if we
             * lost focus to our own combo box.
             */
            if (e.getOppositeComponent() != combo) {
                hideMatches();
            }

            //Workaround for Bug 5100422 - Hide Popup on focus loss
            combo.setPopupVisible(false);
        }
    }

    /**
     * Key listener, provides some special functionality to show
     * and hide the list of matches, and autocomplete text.
     */
    private class SmartKeyListener extends KeyAdapter implements Serializable {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = -1460368107786564048L;

        @Override
        public void keyPressed(final KeyEvent e) {
            /*
             * We override keyPressed so that we can consume
             * special keys such as the down
             * arrow before the normal combo box sees them.
             */
            switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                //Escape: hide the matches
                if (!combo.isPopupVisible() && !matchMenu.isVisible()) {
                    changeSupport.firePropertyChange(ESCAPE_PROPERTY, 0, 1);
                }

                if (!combo.isPopupVisible()) {
                    hideMatches();
                }
                break;

            case KeyEvent.VK_BACK_SPACE:
            case KeyEvent.VK_DELETE:
                if (!matchMenu.isVisible()) {
                    showMatchesOrFullList(false);
                }
                break;

            case KeyEvent.VK_ENTER:
                if (!combo.isPopupVisible() && !matchMenu.isVisible()) {
                    changeSupport.firePropertyChange(ENTER_PROPERTY, 0, 1);
                } else {
                    //Enter: accept the currently selected item (if any)
                    if (!combo.isPopupVisible()) {
                        if (matchList.getSelectedIndex() != -1) {
                            acceptedListItem(matchList.getSelectedValue()
                                    .toString());
                        }
                        hideMatches();
                    }
                }
                editor.setCaretPosition(editor.getText().length());

                break;

            case KeyEvent.VK_DOWN:
                //Ctrl + down : show whole list
                if (e.isControlDown() || e.isMetaDown()) {
                    combo.setPopupVisible(true);
                } else {
                    /*
                     * Show the appropriate popup if there isn't already one
                     * being displayed, and select the next item.
                     */
                    if (!combo.isPopupVisible()) {
                        //Consume the key to prevent the full popup appearing
                        e.consume();

                        if (!matchMenu.isVisible()) {
                            showMatchesOrFullList(true);
                        }
                        selectNextValue();
                    }
                }
                break;

            case KeyEvent.VK_UP:
                //Select previous match list item if possible
                if (!combo.isPopupVisible()) {
                    //Consume the key to prevent the full popup appearing
                    e.consume();
                    selectPreviousValue();
                }
                break;

            case KeyEvent.VK_SPACE:
                //Ctrl + space : autocomplete word
                if (e.isControlDown() || e.isMetaDown()) {

                    //Regenerate the list of matches
                    Object[] newData = getMatchingItems(editor.getText());
                    matchList.setListData(newData);

                    //Autocomplete to first match
                    final int matchCount = matchList.getModel().getSize();
                    if (matchCount > 0) {
                        acceptedListItem(matchList.getModel()
                                .getElementAt(0).toString());

                        hideMatches();
                    }
                }
                break;

            default:
                //Ignore any other keystrokes
                break;
            }
        }
    }

    /**
     * DocumentListener for the combo box.
     * We detect removed text
     * and show the (possibly updated) match list accordingly.
     */
    private class SmartDocumentListener implements DocumentListener,
            Serializable {

        /**
         * Default generated version number for serialization.
         */
        private static final long serialVersionUID = 5715628273126360492L;

        public void insertUpdate(final DocumentEvent e) {
            //Do nothing
        }

        public void changedUpdate(final DocumentEvent e) {
            //Do nothing
        }

        public void removeUpdate(final DocumentEvent e) {

            if (!acceptedListItem) {
                if (!combo.isPopupVisible()) {
                    showMatchesOrFullList(false);
                }
            }
            acceptedListItem = false; //Reset the flag
        }
    }

    /**
     * Decorates the given combo box with a popup menu of items that match
     * the currently entered text.
     *
     * @param combo the combo box to decorate
     * @throws IllegalArgumentException if the combo box's editor is
     *         not an instance of JTextComponent
     */
    public SmartComboBox(final JComboBox combo) {
        this.combo = combo;
        changeSupport = new PropertyChangeSupport(this);

        combo.setEditable(true);
        JScrollPane scroll = new JScrollPane(matchList);
        scroll.setBorder(null);

        //Add a mouse listener to the list of matches so we can click on them
        matchList.addMouseListener(new SmartMouseListener());
        matchList.setBackground(BACKGROUND);

        scroll.getVerticalScrollBar().setFocusable(false);
        scroll.getHorizontalScrollBar().setFocusable(false);

        matchMenu.setBorder(BorderFactory.createLineBorder(Color.black));
        matchMenu.add(scroll);

        matchMenu.setFocusable(false);
        matchList.setFocusable(false);

        Component editorComponent = combo.getEditor().getEditorComponent();
        if (!(editorComponent instanceof JTextComponent)) {
            throw new IllegalArgumentException(
                    "Combo box editor must be an instance of JTextComponent");
        }

        editor = (JTextComponent) editorComponent;

        Document oldDocument = editor.getDocument();
        editor.setDocument(new AutoCompleteDocument());

        //Copy over any existing document listeners
        if (oldDocument instanceof AbstractDocument) {
            AbstractDocument ad = (AbstractDocument) oldDocument;
            for (DocumentListener d : ad.getDocumentListeners()) {
                editor.getDocument().addDocumentListener(d);
            }
        }

        editor.getDocument().addDocumentListener(new SmartDocumentListener());
        editor.addKeyListener(new SmartKeyListener());
        editor.addFocusListener(new SmartFocusListener());

        combo.setRenderer(new TipRenderer());
    }

    /**
     * Set a document filter to only allow valid strings to
     * be entered by the user.
     * The filter's <code>insertString</code>
     * method is used to determine if
     * the input should be allowed.
     *
     * @param filter the new filter to use
     */
    public final void setDocumentFilter(final Document filter) {
        this.filter = filter;
    }

    /**
     * Determine whether matching is case sensitive.
     *
     * @return true if case sensitive matching
     */
    public final boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Set whether matching is case sensitive.
     *
     * @param caseSensitive true for case sensitive matching
     */
    public final void setCaseSensitive(final boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * Determine if item autocompleting is on or off.
     * If item autocompleting is enabled,
     * the editor text will autocomplete to the first matching item, and
     * the appended text will be highlighted.
     *
     * @return true if editor will automatically insert the first item in
     *         the list that matches the current text
     */
    public final boolean isAutoCompleting() {
        return autoCompleting;
    }

    /**
     * Set item autocompleting on or off. If item autocompleting is enabled,
     * the editor text will autocomplete to the first matching item, and
     * the appended text will be highlighted.
     *
     * @param autoCompleting true to autocomplete word with first match
     */
    public final void setAutoCompleting(final boolean autoCompleting) {
        this.autoCompleting = autoCompleting;
    }

    /**
     * Determine whether we wrap around when we exceed the start/end
     * of the list of possible matches.
     * If true, we will wrap back to the first item when
     * we exceed the end, and wrap to the last item when we go before the
     * first item.
     *
     * @return true if selection will wrap around
     */
    public final boolean isSelectionWrapping() {
        return selectionWrapping;
    }

    /**
     * Set whether to wrap around when we exceed the start/end of the list
     * of possible matches. If true, we will wrap back to the first item when
     * we exceed the end, and wrap to the last item when we go before the
     * first item.
     *
     * @param selectionWrapping true to wrap around
     */
    public final void setSelectionWrapping(final boolean selectionWrapping) {
        this.selectionWrapping = selectionWrapping;
    }

    /**
     * Determine whether to append selected list matches to the existing text
     * in the combo box editor.
     *
     * @return true if matches are appended to the existing editor text
     */
    public final boolean isAppendingMatches() {
        return append;
    }

    /**
     * Set whether to append selected list matches to the existing text
     * in the combo box editor.
     *
     * @param append true to append matches to existing editor text
     */
    public final void setAppendingMatches(final boolean append) {
        this.append = append;
    }

    /**
     * Determine whether to highlight all the editor text when the
     * combo box gains focus.
     *
     * @return true if editor text highlights when the combo box gains focus
     */
    public final boolean isTextHighlightedOnFocusGain() {
        return textHighlightedOnFocusGain;
    }

    /**
     * Set whether to highlight all the editor
     * text when the combo box gains focus.
     *
     * @param textHighlightedOnFocusGain true to highlight editor text
     *        when focus is gained
     */
    public final void setTextHighlightedOnFocusGain(
            final boolean textHighlightedOnFocusGain) {
        this.textHighlightedOnFocusGain = textHighlightedOnFocusGain;
    }

    /**
     * Add an observer (listener).
     *
     * @param listener the listener to add
     */
    public final void addPropertyChangeListener(
            final PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove an observer (listener).
     *
     * @param listener the listener to remove
     */
    public final void removePropertyChangeListener(
            final PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Convenience method to hide the match list.
     */
    public final void hideMatches() {
        matchMenu.setVisible(false);
    }

    /**
     * Convenience method to show the match list.
     */
    public final void showMatches() {
        matchMenu.setVisible(true);
    }

    /**
     * Get the horizontal position where the list of
     * matches should appear, measured
     * in characters. For example, returning
     * <code>editorText.length()</code> would make
     * the match list appear immediately after the editor text.
     *
     * Subclasses should override this to display the list
     * in the correct position.
     *
     * @param editorText the current editor text
     * @return the spacing from the left of the combo box to the left of the
     *         match list (measured in characters)
     */
    protected int getOffsetForMatchList(final String editorText) {
        //By default, return 0 (the start of the editor text)
        return 0;
    }

    /**
     * Get a list of items that match the current text.
     * By default we just check for items that <em>begin with</em> the
     * string to match.
     * A subclass might override this method to do something more complex,
     * such as looking for items that <em>contain</em> the string to match.
     *
     * @param stringToMatch the string to find matches for
     * @return list of combo box items that match stringToMatch
     */
    protected Object[] getMatchingItems(final String stringToMatch) {
        final int totalItems = combo.getItemCount();
        final List<String> matchingItems = new ArrayList<String>();

        final String editorText;
        if (caseSensitive) {
            editorText = stringToMatch;
        } else {
            editorText = stringToMatch.toLowerCase(Localization.getLocale());
        }

        //Do a linear search of all the items
        for (int i = 0; i < totalItems; i++) {
            Object item = combo.getItemAt(i);
            String name = item.toString(); //Current item string
            if (name != null) {

                final String itemText;
                if (caseSensitive) {
                    itemText = name;
                } else {
                    itemText = name.toLowerCase(Localization.getLocale());
                }

                //Check if the current item's text starts with the value text
                if (itemText.startsWith(editorText)) {
                    matchingItems.add(name);
                }
            }
        }
        return matchingItems.toArray();
    }

    /**
     * Does a case sensitive string comparison to see if the user
     * has entered a string that exactly matches any list item.
     *
     * @param newString the string that has just been inserted
     * @return true if an exact match was found
     */
    private boolean exactMatchInserted(final String newString) {
        boolean result = false;

        for (int i = 0; i < combo.getItemCount() && !result; i++) {
            //Check for string equality
            final String currentString = combo.getItemAt(i).toString();

            if (currentString.equals(newString)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Show the appropriate popup. If there are matches, the list of matches
     * will appear. Otherwise, the full list will appear if
     * <code>showFullListIfNoMatches</code> is true.
     *
     * @param stringToMatch the text to show the matches for.
     * @param showFullListIfNoMatches
     *        If true, the full list will be shown if there are no matches.
     *        If false, no popup will appear if there are no matches.
     */
    private void showMatchesOrFullList(final String stringToMatch,
            final boolean showFullListIfNoMatches) {

        Object[] newData = getMatchingItems(stringToMatch);

        //Detect if the match list has changed
        boolean same = isIdentical(newData, matchList.getModel());

        //If matches have changed, or match list is not showing
        if (!same || !matchMenu.isVisible()) {

            //Update the match list now.
            matchList.setListData(newData);

            //If no matches
            if (matchList.getModel().getSize() == 0) {
                hideMatches();

                if (showFullListIfNoMatches) {
                    combo.setPopupVisible(true); //Show the normal popup
                }
            } else {
                int size = matchList.getModel().getSize();
                matchList.setVisibleRowCount(Math.min(size, MATCH_ROW_COUNT));

                //X position of popup relative to start of combo box
                int xOffset = 0;

                //Try to work out where to put the match list
                if (append) {
                    try {
                        //Position, in characters from the left
                        int pos = getOffsetForMatchList(stringToMatch);

                        xOffset = editor.getUI().modelToView(editor, pos).x;

                    } catch (BadLocationException e) {
                        xOffset = 0;
                    }
                }

                if (combo.isShowing()) {
                    hideMatches();
                    matchMenu.show(combo, xOffset, combo.getHeight());
                    combo.requestFocusInWindow();
                }
            }
        }
    }

    /**
     * Shows the list of matches. If there are no matches and
     * <code>showFullListIfNoMatches</code> is true, the normal
     * popup will be shown instead.
     *
     * @param showFullListIfNoMatches if true and there
     *            are no matches, the full list will be shown.
     */
    private void showMatchesOrFullList(final boolean showFullListIfNoMatches) {
        showMatchesOrFullList(editor.getText(), showFullListIfNoMatches);
    }

    /**
     * Determine if two sets have the same toString() values
     * for all elements (and in the right order).
     *
     * @param newData the array of objects to test
     * @param model the current list data for the combo box
     * @return true if every value in newData has a corresponding value in
     *         model with the same toString() value
     */
    private boolean isIdentical(
            final Object[] newData, final ListModel model) {

        //Assume true until a difference is found
        boolean result = true;

        if (newData.length != model.getSize()) {
            result = false;
        }

        //Check each element's string value
        for (int i = 0; i < model.getSize() && result; i++) {
            if (!newData[i].toString().equals(model.getElementAt(i))) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Selects the next item in the list, possibly wrapping back to
     * the first item.
     */
    private void selectNextValue() {
        int oldIndex = matchList.getSelectedIndex();
        int newIndex; //The new index to select
        final int matchCount = matchList.getModel().getSize();

        if (matchCount > 0) {

            if (selectionWrapping) {
                //Select next item, possibly wrapping back to first item.
                //This still works if oldIndex is -1.
                newIndex = (oldIndex + 1) % matchCount;
            } else {
                //No wrapping: go forward 1 but don't go past the end
                newIndex = Math.min(matchCount - 1, oldIndex + 1);
            }

            matchList.setSelectedIndex(newIndex);
            matchList.ensureIndexIsVisible(newIndex);
        }
    }

    /**
     * Selects the previous item in the list, possibly wrapping back to
     * the last item.
     */
    private void selectPreviousValue() {
        int oldIndex = matchList.getSelectedIndex();
        int newIndex; //The new index to select
        final int matchCount = matchList.getModel().getSize();

        if (matchCount > 0) {

            if (selectionWrapping) {

                if (oldIndex == -1) {
                    newIndex = 0; //No previous selection, select first item
                } else if (oldIndex == 0) {
                    newIndex = matchCount - 1; //Wrap to last item
                } else {
                    newIndex = oldIndex - 1; //Select previous
                }

            } else {
                //No wrapping: go back 1 but don't go past the start
                newIndex = Math.max(0, oldIndex - 1);
            }

            matchList.setSelectedIndex(newIndex);
            matchList.ensureIndexIsVisible(newIndex);
        }
    }

    /**
     * The user has entered a value. Update the editor text appropriately
     * and remove any highlighting.
     *
     * @param selected the string entered
     */
    private void acceptedListItem(final String selected) {

        //Set the editor text to the selected value
        if (selected != null) {
            final String currentText = editor.getText();

            acceptedListItem = true;

            if (append) {

                final int insertionPoint = getOffsetForMatchList(currentText);

                /*
                 * How many characters to skip at the start
                 * of the selected string.
                 * For example:
                 * c:/foo/ba          current editor text
                 *        bar.txt     matches popup
                 *          r.txt  <- text to append
                 *        --       <- skip length = 2
                 */
                final int skipLength = currentText.length() - insertionPoint;

                String stringToAppend = "";

                assert skipLength >= 0;

                if (skipLength >= 0 && skipLength < selected.length()) {
                    stringToAppend = selected.substring(skipLength);
                }
                editor.setText(currentText + stringToAppend);
            } else {
                //Not appending, just replace text.
                editor.setText(selected);
            }

            //Get rid of any highlighting
            editor.setCaretPosition(editor.getText().length());
            editor.moveCaretPosition(editor.getText().length());
        }
    }

}
