package suncertify.db;

import java.io.Serializable;

import suncertify.fields.Field;
import suncertify.fields.parsers.FieldParser;

/**
 * A group of field parsers. We provide a method to get the
 * appropriate parser for a given <code>Field</code> class.
 *
 * @author Robert Mollard
 */
public interface FieldParsers extends Serializable {

    /**
     * Get the parser to use to read and write values for the given
     * <code>Field</code> class.
     *
     * @param fieldClass the class of the <code>Field</code> required
     * @return the parser for the class specified
     */
    FieldParser getParserForClass(
            Class<? extends Field> fieldClass);

}
