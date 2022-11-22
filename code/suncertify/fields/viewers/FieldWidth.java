package suncertify.fields.viewers;

/**
 * How wide a field should be displayed.
 *
 * @author Robert Mollard
 */
public enum FieldWidth {

    /**
     * Slightly narrower than normal.
     */
    NARROW(0.8),

    /**
     * Normal width.
     */
    NORMAL(1.0),

    /**
     * Wider than normal. Suitable for fields
     * that contain long display strings.
     */
    WIDE(1.9);

    /**
     * The multiple of the "normal" width.
     */
    private double widthMultiplier;

    /**
     * Construct a new instance with the given
     * width multiplier.
     *
     * @param widthMultiplier the width multiplier to use
     */
    private FieldWidth(final double widthMultiplier) {
        this.widthMultiplier = widthMultiplier;
    }

    /**
     * Get this FieldWidth's field width multiplier.
     * For example, a value of 2.0 indicates that
     * the field should be displayed
     * twice as wide as a normal field.
     *
     * @return the field width multiplier
     */
    public double getWidthMultiplier() {
        return widthMultiplier;
    }

}
