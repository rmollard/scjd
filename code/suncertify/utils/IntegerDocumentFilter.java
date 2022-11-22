package suncertify.utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Simple class to restrict input to non-negative integer
 * values. The length of the input is restricted to
 * the maximum set in the constructor.
 *
 * This class is immutable.
 *
 * @author Robert Mollard
 */
public final class IntegerDocumentFilter extends PlainDocument {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 1019100662135910870L;

    /**
     * The maximum input total input length allowed,
     * measured in characters.
     * Can be null, in which case length is unbounded.
     */
    private Integer maxLength;

    /**
     * Create a new <code>IntegerDocumentFilter</code>
     * with the given maximum limit.
     * Note that the limit is the maximum number of characters,
     * not the maximum integer allowed.
     *
     * @param maxLength the maximum input length
     */
    public IntegerDocumentFilter(final Integer maxLength) {
        this.maxLength = maxLength;
    }

    /** {@inheritDoc} */
    @Override
    public void insertString(final int offset,
            final String str, final AttributeSet attr)
            throws BadLocationException {

        if (str != null) {
            //Just do a linear search for bad characters
            for (int i = 0; i < str.length(); i++) {
                if (!Character.isDigit(str.charAt(i))) {
                    throw new BadLocationException(str, offset);
                }
            }

            if (maxLength != null) {
                if ((getLength() + str.length()) > maxLength) {
                    throw new BadLocationException(str, offset);
                }
            }
            super.insertString(offset, str, attr);
        }
    }

}
