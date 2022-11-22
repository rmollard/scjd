package suncertify.utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Simple class to limit the input length to a given value.
 *
 * This class is immutable.
 *
 * @author Robert Mollard
 */
public final class LengthDocumentFilter extends PlainDocument {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = 5428518034568240698L;

    /**
     * The maximum input total input length allowed,
     * measured in characters.
     */
    private int maxLength;

    /**
     * Create a <code>LengthDocumentFilter</code>
     * with the given maximum limit.
     *
     * @param maxLength the maximum input length
     */
    public LengthDocumentFilter(final int maxLength) {
        this.maxLength = maxLength;
    }

    /** {@inheritDoc} */
    @Override
    public void insertString(final int offset,
            final String str, final AttributeSet attr)
            throws BadLocationException {
        if (str != null) {
            if ((getLength() + str.length()) <= maxLength) {
                super.insertString(offset, str, attr);
            }
        }
    }

}
