package suncertify;

/**
 * A ProgressListener is used to observe a task that may take a long time.
 * The task may be cancelled before it is completed.
 *
 * @author Robert Mollard
 */
public interface ProgressListener {

    /**
     * Determine if the task has been cancelled by the user.
     *
     * @return true if the task has been cancelled
     */
    boolean isCancelled();

    /**
     * Set the maximum value for the progress indicator.
     *
     * @param max the maximum value
     */
    void setMaxValue(final int max);

    /**
     * Sets the current value for the progress indicator, this should be less
     * than or equal to the maximum value.
     *
     * @param current the current value
     */
    void setCurrentValue(final int current);
}
