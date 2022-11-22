package suncertify.utils;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * A <code>JTable</code> that provides tooltips for
 * items that are too wide to display fully.
 * The tooltip is the item, so the user can "peek" at the
 * item by hovering the mouse over the table cell.
 *
 * @author Robert Mollard
 */
public class PeekableTable extends JTable {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 3760745248878669658L;

    /**
     * Default public constructor.
     */
    public PeekableTable() {
        //Empty
    }

    /** {@inheritDoc} */
    @Override
    public final String getToolTipText(final MouseEvent event) {
        /*
         * We provide a tooltip if the width of the cell is too
         * narrow to display the component at its preferred size.
         */
        String tip = null; //The tooltip text
        boolean shouldDisplayTip = true; //If we should show a tooltip
        final Point p = event.getPoint(); //Mouse pointer location
        int col = columnAtPoint(p); //Column index
        int row = rowAtPoint(p); //Row index

        if (col != -1 && row != -1) {
            TableCellRenderer renderer = getCellRenderer(row, col);
            Component component = prepareRenderer(renderer, row, col);

            if (component instanceof JComponent) {
                //Convert the event to the renderer's coordinate system
                Rectangle cellRect = getCellRect(row, col, false);
                if (cellRect.width >= component.getPreferredSize().width) {
                    shouldDisplayTip = false; //Size is ok, don't show tooltip
                } else {
                    p.translate(-cellRect.x, -cellRect.y);

                    if (component instanceof JLabel) {
                        tip = ((JLabel) component).getText();
                    } else {
                        //Make a new mouse event so we can get the tip text
                        MouseEvent newEvent = new MouseEvent(component,
                                event.getID(), event.getWhen(),
                                event.getModifiers(), p.x, p.y,
                                event.getClickCount(), event.isPopupTrigger());

                        tip = ((JComponent) component).getToolTipText(newEvent);
                    }
                }
            }
        }

        if (shouldDisplayTip) {

            if (tip == null) {
                //No tip from the renderer, maybe the JTable has one
                tip = getToolTipText();
            }
            //Still no tip, calculate tip from cell value
            if (tip == null) {
                Object value = getValueAt(row, col);
                if (value != null) {
                    tip = value.toString();
                }
            }
        }
        return tip;
    }

    /** {@inheritDoc} */
    @Override
    public final Point getToolTipLocation(final MouseEvent event) {
        /*
         * Set the tooltip location to the cell where the
         * mouse pointer is. This will cause the tooltip
         * to be aligned with the top left corner of the cell.
         */
        Point result = null;
        int row = rowAtPoint(event.getPoint());
        int col = columnAtPoint(event.getPoint());

        //Ensure row and column are valid
        if (row != -1 && col != -1) {
            /*
             * If there is a tooltip, get the location of the cell.
             * The tooltip text will appear over the cell.
             */
            if (getToolTipText() != null || getToolTipText(event) != null) {
                result = getCellRect(row, col, false).getLocation();
            }
        }
        return result;
    }

}
