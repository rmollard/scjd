package suncertify.fields;

import java.io.Serializable;

/**
 * Meta data for a field. This class contains
 * <ul>
 * <li>The field name</li>
 * <li>The type of the field</li>
 * <li>Whether the field should be included in the search form</li>
 * <li>Whether the field should be displayed in the search results table</li>
 * <li>Whether the field is modifiable by the user</li>
 * <li>The maximum length of the field, in characters</li>
 * </ul>
 *
 * This class is immutable.
 *
 * @author Robert Mollard
 */
public final class FieldDetails implements Serializable {

    /**
     * Default generated version number for serialization.
     */
    private static final long serialVersionUID = -6750826162529664778L;

    /**
     * The name of the field (unlocalized).
     */
    private final String fieldName;

    /**
     * The field class.
     */
    private final Class<? extends Field> fieldClass;

    /**
     * True if the field should be included in the search criteria.
     * This is only a hint.
     */
    private final boolean searchable;

    /**
     * True if this field should be displayed in the results table.
     * This is only a hint.
     */
    private final boolean displayable;

    /**
     * True if clients should be able to modify this field.
     */
    private final boolean modifiable;

    /**
     * Maximum length of the field value string.
     * If length is unbounded, this will be <code>null</code>.
     */
    private final Integer maxLength;

    /**
     * Create a new <code>FieldDetails</code> instance.
     *
     * @param fieldName the name of the field
     * @param fieldClass the class of the field
     * @param searchable true if clients should search on this field
     * @param displayable true if clients should
     *        display this field in the search results
     * @param modifiable true if the field can be modified
     * @param maxLength the maximum string length for this field,
     *        or <code>null</code> if length is unbounded
     */
    public FieldDetails(final String fieldName,
            final Class<? extends Field> fieldClass, final boolean searchable,
            final boolean displayable, final boolean modifiable,
            final Integer maxLength) {
        this.fieldName = fieldName;
        this.fieldClass = fieldClass;
        this.searchable = searchable;
        this.displayable = displayable;
        this.modifiable = modifiable;
        this.maxLength = maxLength;
    }

    /**
     * Get the unlocalized name of the field.
     *
     * @return the field name
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Get the class of the field.
     *
     * @return the field class
     */
    public Class<? extends Field> getFieldClass() {
        return fieldClass;
    }

    /**
     * Determine if the field be displayed in the search results table.
     *
     * @return true if the field should be displayed
     */
    public boolean isDisplayable() {
        return displayable;
    }

    /**
     * Determine if the field be included in the search criteria.
     *
     * @return true if the field should be searchable
     */
    public boolean isSearchable() {
        return searchable;
    }

    /**
     * Determine if this field is modifiable by the user.
     *
     * @return true if the field is modifiable
     */
    public boolean isModifiable() {
        return modifiable;
    }

    /**
     * Get the maximum permittable string length for this field.
     *
     * @return the maximum field length, or <code>null</code> if length is
     *         unbounded
     */
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * Get a string describing the field details.
     * The string contains:
     * <ul>
     * <li>the field name</li>
     * <li>the field class</li>
     * <li>whether the field is searchable</li>
     * <li>whether the field is displayable</li>
     * <li>whether the field is modifiable</li>
     * <li>the maximum length of the field (in characters)</li>
     * </ul>
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return new StringBuilder().append("Field details:\n[")
            .append("Field name: ").append(fieldName).append('\n')
            .append("Field class: ").append(fieldClass).append('\n')
            .append("Searchable: ").append(searchable).append('\n')
            .append("Displayable: ").append(displayable).append('\n')
            .append("Modifiable: ").append(modifiable).append('\n')
            .append("Max length: ").append(maxLength).append("]")
            .toString();
    }

    /**
     * Defensive <code>readResolve</code> method to
     * enforce immutability.
     *
     * @return the deserialized <code>FieldDetails</code> object
     */
    private Object readResolve() {
        return new FieldDetails(fieldName, fieldClass, searchable,
                displayable, modifiable, maxLength);
    }

}
