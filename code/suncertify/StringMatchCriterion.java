package suncertify;

import suncertify.fields.Field;
import suncertify.fields.StringField;
import suncertify.utils.Localization;

/**
 * Criterion for String matching. We deal with exact String matches and prefix
 * matches. We also provide support for case sensitivity.
 * This class is immutable.
 *
 * @author Robert Mollard
 */
public final class StringMatchCriterion extends Criterion {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -7225827630760137572L;

    /**
     * If true, try to match the other String in its entirety.
     * Otherwise, see if the other String starts with our String.
     */
    private final boolean matchWholeWord;

    /**
     * True if performing a case sensitive String comparison.
     */
    private final boolean caseSensitive;

    /**
     * The StringField to match.
     * May be null, in which case any String will
     * match if <code>matchWholeWord</code> is false,
     * and no string will match if
     * <code>matchWholeWord</code> is true.
     */
    private final StringField stringField;

    /**
     * Construct a new <code>StringMatchCriterion</code>.
     *
     * @param stringField the StringField to match.
     *        May be null, in which case any String will match
     *        if <code>matchWholeWord</code> is false, and no string will
     *        match if <code>matchWholeWord</code>  is true.
     * @param matchWholeWord if true, match the entire String.
     *        Otherwise match if this is a substring of the
     *        String to match against.
     * @param caseSensitive case sensitive String comparison
     */
    public StringMatchCriterion(final StringField stringField,
            final boolean matchWholeWord, final boolean caseSensitive) {
        super(stringField);
        //Make a defensive copy of the string field
        this.stringField = new StringField(stringField.getValue());
        this.matchWholeWord = matchWholeWord;
        this.caseSensitive = caseSensitive;
    }

    /**
     * Construct a new case-insensitive <code>StringMatchCriterion</code>.
     *
     * @param value the StringField to match
     * @param matchWholeWord if true, match the entire String.
     *        Otherwise match if this is a substring of the
     *        String to match against.
     */
    public StringMatchCriterion(final StringField value,
            final boolean matchWholeWord) {
        this(value, matchWholeWord, false);
    }

    /**
     * Construct a new case-insensitive <code>StringMatchCriterion</code>
     * that will accept partial string matches.
     *
     * @param value the StringField to match
     */
    public StringMatchCriterion(final StringField value) {
        this(value, false, false);
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(final Field other) {
        boolean matches = false;

        if (!(other instanceof StringField)) {
            throw new IllegalArgumentException("Expected a StringField");
        }

        StringField otherField = (StringField) other;

        if (this.stringField == null) {
            if (!matchWholeWord) {
                /*
                 * A null string matches anything, unless matchWholeWord
                 * is true, in which case it matches nothing.
                 */
                matches = true;
            }
        } else {
            final String ourString;
            if (caseSensitive) {
                ourString = stringField.getValue();
            } else {
                ourString = stringField.getValue().toLowerCase(
                        Localization.getLocale());
            }

            final String otherString;
            if (caseSensitive) {
                otherString = otherField.getValue();
            } else {
                otherString = otherField.getValue().toLowerCase(
                        Localization.getLocale());
            }

            if (matchWholeWord) {
                //Match entire phrase
                matches = otherString.equals(ourString);
            } else {
                //Match prefix only
                matches = otherString.startsWith(ourString);
            }
        }
        return matches;
    }

    /**
     * Get a string describing the criterion.
     * The string contains the string field to match, and
     * whether the match is case sensitive or for an exact match.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return new StringBuilder()
            .append("String Match Criterion [")
            .append(" Match whole word: ").append(matchWholeWord).append('\n')
            .append(" Case sensitive: ").append(caseSensitive).append('\n')
            .append(" Value: ").append(stringField).append('\n')
            .append(']')
            .toString();
    }

    /**
     * Defensive <code>readResolve</code> method to
     * enforce immutability.
     *
     * @return the deserialized <code>StringMatchCriterion</code>
     */
    private Object readResolve() {
        return new StringMatchCriterion(
                stringField, matchWholeWord, caseSensitive);
    }

}
