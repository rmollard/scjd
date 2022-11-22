package suncertify;

/**
 * An MVC controller.
 *
 * @author Robert Mollard
 */
public interface Controller {

    /**
     * Shows a warning message.
     *
     * @param message the message, already localized.
     */
    void showWarning(String message);

}
