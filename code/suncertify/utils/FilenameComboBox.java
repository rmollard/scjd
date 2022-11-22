package suncertify.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

/**
 * Combo box decorator for an editable combo box that will
 * be used to enter filenames.
 * We generate a list of possible matching filenames (names
 * of files and folders) as the user types.
 * Folders are indicated by forward slashes or backslashes.
 * The list of matches for the current folder is displayed neatly
 * underneath the slash.
 * The <code>setSuffix</code> method provides some control over
 * which filenames will be included in the list of matches.
 *
 * @author Robert Mollard
 */
public final class FilenameComboBox extends SmartComboBox {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -7400401435465505480L;

    /**
     * The suffix that filenames must end with in order to be included
     * in the list of matches. By default we match all files.
     */
    private String suffix = "";

    /**
     * If true, read only files will be included in the autocomplete list.
     * If false, only directories and writable files will be included.
     */
    private boolean matchingReadOnlyFiles = true;

    /**
     * Decorates the given combo box with a popup menu of filenames that
     * begin with the current combo box editor text.
     *
     * @param combo the combo box to decorate
     */
    public FilenameComboBox(final JComboBox combo) {
        super(combo);

        //Append match text to the editor text so the full path is displayed
        this.setAppendingMatches(true);
    }

    /**
     * Determine if read-only files are included in the match list.
     *
     * @return true if read-only files are included
     */
    public boolean isMatchingReadOnlyFiles() {
        return matchingReadOnlyFiles;
    }

    /**
     * Set whether read-only files are included in the match list.
     *
     * @param matchingReadOnlyFiles the value to set
     */
    public void setMatchingReadOnlyFiles(final boolean matchingReadOnlyFiles) {
        this.matchingReadOnlyFiles = matchingReadOnlyFiles;
    }

    /**
     * Set the suffix that each filename must end with in order
     * to be included in the match list.
     *
     * @param suffix the new suffix, for example ".txt"
     */
    public void setSuffix(final String suffix) {
        if (suffix == null) {
            this.suffix = "";
        } else {
            this.suffix = suffix;
        }
    }

    /**
     * Get the suffix that each filename must end with in order
     * to be included in the match list.
     *
     * @return the current suffix
     */
    public String getSuffix() {
        return suffix;
    }

    /** {@inheritDoc} */
    @Override
    protected int getOffsetForMatchList(final String editorText) {
        final String value = editorText;

        //Add 1 to the index so it is never negative
        final int lastBackslash = value.lastIndexOf('\\') + 1;
        final int lastForwardSlash = value.lastIndexOf('/') + 1;
        final int position = Math.max(lastBackslash, lastForwardSlash);

        return position;
    }

    /**
     * File matcher to match files that have the prefix given
     * to the constructor.
     */
    private class FileMatcher implements FilenameFilter {

        /**
         * Prefix that matching files must begin with.
         */
        private final String prefix;

        /**
         * True for case sensitive match.
         */
        private final boolean isCaseSensitive;

        /**
         * Create a new file matcher to match filenames that
         * start with the given prefix.
         *
         * @param prefix the prefix that files must begin with
         *        in order to be considered a match
         * @param isCaseSensitive true for case sensitive match
         */
        public FileMatcher(final String prefix,
                final boolean isCaseSensitive) {
            if (prefix == null) {
                this.prefix = "";
            } else {
                this.prefix = prefix;
            }
            this.isCaseSensitive = isCaseSensitive;
        }

        public boolean accept(final File dir, final String name) {
            boolean match;
            File file = new File(dir, name);

            final String nameMatch; //The filename to match
            final String prefixMatch; //The prefix that the filename must have
            final String suffixMatch; //The suffix that the filename must have

            if (isCaseSensitive) {
                nameMatch = name;
                prefixMatch = prefix;
                suffixMatch = suffix;
            } else {
                nameMatch = name.toLowerCase(Localization.getLocale());
                prefixMatch = prefix.toLowerCase(Localization.getLocale());
                suffixMatch = suffix.toLowerCase(Localization.getLocale());
            }

            //Check that the file actually exists
            match = file.exists();

            //Check prefix is ok
            match = match && nameMatch.startsWith(prefixMatch);

            //Ignore hidden files and folders
            match = match && !file.isHidden();

            //Only match normal files and directories
            match = match && (file.isFile() || file.isDirectory());

            if (match && file.isFile()) {
                //Check that the suffix (file extension) matches
                match = match && (nameMatch.endsWith(suffixMatch));

                /*
                 * If we are only matching writeable files, check
                 * that the file can be written to
                 */
                if (!matchingReadOnlyFiles) {
                    match = match && file.canWrite();
                }
            }
            return match;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected Object[] getMatchingItems(final String stringToMatch) {
        /*
         * Return all the filenames in the current directory that match
         * the current combo box editor text and match the suffix.
         */
        final Object[] result;
        final List<String> matches = new ArrayList<String>();

        final String value = stringToMatch;
        final int lastBackslash = value.lastIndexOf('\\');
        final int lastForwardslash = value.lastIndexOf('/');
        final int lastSlashIndex = Math.max(lastBackslash, lastForwardslash);

        final String lastDirectory;
        if (lastSlashIndex == -1) {
            //No slashes seen, return empty array.
            result = matches.toArray();
        } else {
            //The current directory
            lastDirectory = value.substring(0, lastSlashIndex + 1);

            //Current partial filename text
            final String prefix;

            //If the last character is a slash
            if (lastSlashIndex == value.length() - 1) {
                prefix = "";
            } else {
                if (isCaseSensitive()) {
                    prefix = value.substring(lastSlashIndex + 1);
                } else {
                    prefix = value.substring(lastSlashIndex + 1).toLowerCase(
                        Localization.getLocale());
                }
            }

            final FilenameFilter filter =
                new FileMatcher(prefix, isCaseSensitive());
            final File lister = new File(lastDirectory);
            final String[] files = lister.list(filter);

            if (files != null) {
                result = files;
            } else {
                result = matches.toArray();
            }
        }
        return result;
    }

}
