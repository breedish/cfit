package com.mtvi.cfit.query.definition;

/**
 * Abstract query definition list.
 *
 * @param <QT> - query definition source type.
 * @param <QR> - query definition representation type.
 * @author zenind.
 */
public abstract class AbstractQueryDefinitionList<QT, QR> implements QueryDefinitionList {

    /** Queries source storage.*/
    private final QT source;
    /** Query definition converter for given source.*/
    private final QueryDefinitionConverter<QR> converter;

    protected AbstractQueryDefinitionList(QT source, QueryDefinitionConverter<QR> converter) {
        if (source == null || converter == null) {
            throw new IllegalArgumentException(
                String.format("Null value has been passed in as required argument ['source'=%s, 'converter'=%s]", source, converter));
        }
        this.source = source;
        this.converter = converter;
    }

    /**
     * @return source storage.
     */
    public QT getSource() {
        return source;
    }

    /**
     * @return converter.
     */
    protected QueryDefinitionConverter<QR> getConverter() {
        return converter;
    }
}
