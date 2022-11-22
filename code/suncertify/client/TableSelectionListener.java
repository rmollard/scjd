package suncertify.client;

/**
 * A listener that listens to table row selection changes.
 *
 * @author Robert Mollard
 */
public interface TableSelectionListener {

    /**
     * This method is called when a row selection changes.
     *
     * @param row the row number.
     *        If there is no row selected, <code>row</code>
     *        will be null.
     */
    void rowSelected(Integer row);

}
