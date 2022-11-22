package suncertify.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Simple utility class to localize strings.
 * This is a thread safe singleton class.
 *
 * @author Robert Mollard
 */
public final class Localization {

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify");

    /**
     * The locale, currently we just have English.
     */
    private static final Locale LOCALE = Locale.ENGLISH;

    /**
     * The name of the properties file containing the translations.
     * The ".properties" part of the filename is omitted.
     */
    private static final String BUNDLE_NAME =
        "suncertify.URLyBird_Translations_EN";

    /**
     * The resource bundle for <code>BUNDLE_NAME</code>.
     */
    private final ResourceBundle bundle;

    /**
     * The singleton instance of this class.
     */
    private static final Localization INSTANCE = new Localization();

    /**
     * All methods static, no point in creating more than one copy of
     * this class, so private constructor.
     */
    private Localization() {
        bundle = ResourceBundle.getBundle(BUNDLE_NAME);
    }

    /**
     * Get the locale that we are using.
     *
     * @return the current locale
     */
    public static Locale getLocale() {
        return LOCALE;
    }

    /**
     * Get a translated character. The character returned is the first
     * character in the translated string.
     *
     * @param key the character's translation key
     * @return the translated string
     */
    public static char getChar(final String key) {
        return getString(key).charAt(0);
    }

    /**
     * Get a translated version of a string.
     *
     * @param key the string to translate
     * @return the translated string
     */
    public static String getString(final String key) {
        String result;

        try {
            result = INSTANCE.bundle.getString(key);
        } catch (MissingResourceException e) {
            assert false : "Missing translation for '" + key + "'";
            LOGGER.warning("Missing translation for '" + key + "'");

            result = '!' + key + '!';
        }
        return result;
    }

    /**
     * Get a translated version of a string that has a list
     * of <code>int</code> arguments.
     *
     * @param key the string to translate
     * @param args the <code>int</code> arguments to use
     * @return the translated string
     */
    public static String getString(final String key, final int... args) {
        List<Integer> nums = new ArrayList<Integer>();
        for (Integer i : args) {
            nums.add(i);
        }
        return getString(key, nums.toArray());
    }

    /**
     * Get a translated version of a string that has a list
     * of <code>String</code> arguments.
     *
     * @param key the string to translate
     * @param args the <code>String</code> arguments to use.
     *        Note that these parameters do not get translated.
     * @return the translated string
     */
    public static String getString(final String key, final String... args) {
        List<String> strings = new ArrayList<String>();
        for (String i : args) {
            strings.add(i);
        }
        return getString(key, strings.toArray());
    }

    /**
     * Get a translated version of a string if it has a translation.
     * If there is not translation, return the original string.
     *
     * @param key the string to translate
     * @return the translated string, or the key if there is not translation.
     */
    public static String getStringIfPresent(final String key) {
        String result;
        try {
            result = INSTANCE.bundle.getString(key);
        } catch (MissingResourceException e) {
            result = key;
        }
        return result;
    }

    /**
     * Get a translated version of a string  by combining
     * a translated string with a parameter list.
     * Note that the values in the array are not translated.
     *
     * @param message the message (gets translated)
     * @param values raw parameters to <code>message</code>.
     *        Note that these parameters do not get translated.
     * @return formatted, translated string
     */
    public static String getString(
            final String message, final Object[] values) {
        final MessageFormat msgFmt = new MessageFormat(getString(message));

        return msgFmt.format(values);
    }

}
