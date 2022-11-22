package suncertify.panels;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Simple class to prevent invalid RMI address characters
 * from being entered.
 * Note that this document filter does not
 * guarantee that the input will be a valid RMI address.
 *
 * This class is immutable.
 *
 * @author Robert Mollard
 */
final class RMIAddressDocumentFilter extends PlainDocument {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -7049753975550788103L;

    /**
     * The characters that should not be contained in a URL.
     */
    private static final char[] BAD_CHARACTERS =
        {' ', '\t', '\n', '^', '#', '?', '<', '>'
            , '{', '}', '`', '|', '\\'};

    /**
     * Determine if the given character is a "bad" character
     * that should not be contained in a URL.
     *
     * @param testCharacter the character to test
     * @return true if the character is a bad character
     */
    private boolean isBadCharacter(final char testCharacter) {
        boolean isBad = false;
        for (int i = 0; i < BAD_CHARACTERS.length && !isBad; i++) {
            if (BAD_CHARACTERS[i] == testCharacter) {
                isBad = true;
            }
        }
        return isBad;
    }

    /** {@inheritDoc} */
    @Override
    public void insertString(final int offset,
            final String str, final AttributeSet attr)
            throws BadLocationException {

        if (str != null) {
            //Just do a linear search for bad characters
            for (int i = 0; i < str.length(); i++) {
                if (isBadCharacter(str.charAt(i))) {
                    throw new BadLocationException(str, offset);
                }
            }
        }
    }

}
