package suncertify;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

/**
 * A simple utility class to load image icons.
 * This is a singleton class.
 *
 * @author Robert Mollard
 */
public final class IconFactory {

    /**
     * Cache for images.
     */
    private final Map<String, ImageIcon> icons;

    /**
     * A simple logger for debug etc.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify");

    /**
     * The singleton instance.
     */
    private static final IconFactory INSTANCE = new IconFactory();

    /**
     * Creates the singleton instance.
     */
    private IconFactory() {
        icons = new HashMap<String, ImageIcon>();
    }

    /**
     * Returns the icon with the given name (or path/name).
     * If the icon has already been loaded it
     * is returned immediately, otherwise it is loaded and
     * stored for reuse later.
     * A null is returned if the image cannot be found.
     *
     * @param name the name of the icon to load
     * @return the image icon
     */
    private ImageIcon getIcon(final String name) {
        ImageIcon icon = null;
        icon = icons.get(name);

        //If the icon is not in our cache yet
        if (icon == null) {

            //Load the icon if possible
            final URL resource = IconFactory.class.getResource(name);
            if (resource != null) {
                icon = new ImageIcon(resource);

                //Store the new icon in our cache
                icons.put(name, icon);
            } else {
                LOGGER.warning("Icon missing: " + name);
            }
        }
        return icon;
    }

    /**
     * Returns the icon with the given name (or path/name).
     * If the icon has
     * already been loaded, it is returned
     * immediately. Otherwise it is loaded and
     * stored for reuse later.
     *
     * @param name the name of the icon to load
     * @return the image icon
     */
    public static ImageIcon getImageIcon(final String name) {
        return INSTANCE.getIcon(name);
    }

}
