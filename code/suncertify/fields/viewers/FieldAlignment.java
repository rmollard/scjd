package suncertify.fields.viewers;

import javax.swing.SwingConstants;

/**
 * An enumeration of the supported horizontal text alignment
 * options for fields displayed in the results table,
 * basically just a wrapper for some SwingConstants values.
 * This is slightly safer than using the
 * SwingConstants values directly,
 * since an invalid number would still compile.
 *
 * @author Robert Mollard
 */
public enum FieldAlignment {

    /**
     * Left justified.
     */
    LEFT(SwingConstants.LEFT),

    /**
     * Horizontally centered.
     */
    CENTER(SwingConstants.CENTER),

    /**
     * Right justified.
     */
    RIGHT(SwingConstants.RIGHT);

    /**
     * The alignment direction.
     */
    private final int direction;

    /**
     * Construct an instance with the given alignment.
     *
     * @param direction the alignment direction
     */
    private FieldAlignment(final int direction) {
        this.direction = direction;
    }

    /**
     * Get the direction, as specified in SwingConstants.
     *
     * @return the direction
     */
    public int getDirection() {
        return direction;
    }
}
