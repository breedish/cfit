package com.mtvi.cfit.query.definition;

/**
 * Converter interface for {@link QueryDefinition}.
 *
 * @param <T> type of query source representation.
 * @author zenind.
 */
public interface QueryDefinitionConverter<T> {

    /**
     * Does conversion of query definition from given source of type <code>T</code>.
     * @param source - query definition source.
     * @param id - query id..
     * @return - constructed QueryDefinition.
     */
    QueryDefinition convertFrom(T source, String id);

}
