package com.mtvi.cfit.query.definition;

import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.common.LineBasedIterable;
import com.mtvi.cfit.query.common.SizeAwareIterable;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Query Definition List implementation based on files.
 *
 * @author zenind
 */
public class FileBasedQueryDefinitionList extends AbstractQueryDefinitionList<File, String> {

    /** Query Id format.*/
    private static final String QUERY_ID_FORMAT = "%s_%06d";

    /**
     * Constructor.
     *
     * @param source - source storage.
     * @param converter - converter.
     */
    public FileBasedQueryDefinitionList(File source, QueryDefinitionConverter<String> converter) {
        super(source, converter);
    }

    @Override
    public SizeAwareIterable<QueryDefinition> list() throws CfitException {
        try {
            return new QueryDefinitionAwareIterable(getSource());
        } catch (FileNotFoundException e) {
            throw new CfitException(
                String.format("Source file wasn't found [source=%s]", getSource().getAbsoluteFile()), e
            );
        }
    }

    /**
     * Iterable for query definitions from given source file.
     */
    private final class QueryDefinitionAwareIterable extends LineBasedIterable<QueryDefinition> {

        private QueryDefinitionAwareIterable(File source) throws FileNotFoundException {
            super(source);
        }

        @Override
        protected QueryDefinition nextValue(String rawNext, int id) {
            return getConverter().convertFrom(
                rawNext,
                String.format(FileBasedQueryDefinitionList.QUERY_ID_FORMAT, getSource().getName(), id)
            );
        }
    }

}
