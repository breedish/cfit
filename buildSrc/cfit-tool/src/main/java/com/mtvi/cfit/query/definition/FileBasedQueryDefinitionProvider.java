package com.mtvi.cfit.query.definition;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.common.SizeAwareIterable;
import com.mtvi.cfit.util.CfitUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple <code>File</code> based query definition provider.
 *
 * @author zenind
 */
public class FileBasedQueryDefinitionProvider implements QueryDefinitionProvider {

    /** Logger.*/
    private static final Logger LOG = LoggerFactory.getLogger(FileBasedQueryConverter.class);
    /** Exclusion messaging. */
    private static final List<String> EXCLUSION_EXTENSIONS = ImmutableList.of("exc");
    /** Converter.*/
    private final QueryDefinitionConverter<String> converter;
    /** Cfit configuration.*/
    private final CfitConfiguration configuration;

    /**
     * Constructor.
     * @param config - config.
     */
    public FileBasedQueryDefinitionProvider(CfitConfiguration config) {
        this.configuration = config;
        this.converter = new FileBasedQueryConverter();
    }

    @Override
    public QueryDefinitionList load() {
        LOG.info("Loading queries");

        Set<SizeAwareIterable<QueryDefinition>> queries = new HashSet<SizeAwareIterable<QueryDefinition>>();

        for (File file : locateSources()) {
            try {
                queries.add(new FileBasedQueryDefinitionList(file, converter).list());
                LOG.info("\tInit of queries from {} was performed", file.getAbsolutePath());
            } catch (CfitException e) {
                LOG.error("\tUnable to transformAll queries source {}", file.getAbsolutePath());
            }
        }

        LOG.info("Queries loaded.");

        return new CompositeQueryDefinitionList(queries);
    }

    @SuppressWarnings("unchecked")
    private Collection<File> locateSources() {
        File dir = new File(configuration.getCfitHome(), configuration.getQueriesDirName());
        if (!dir.exists() || !dir.isDirectory()) {
            return Collections.emptyList();
        }

        return FileUtils.listFiles(dir, new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                String extension = Files.getFileExtension(file.getName());
                return !EXCLUSION_EXTENSIONS.contains(extension);
            }

            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        },
            DirectoryFileFilter.DIRECTORY);
    }

    /**
     * File based converter of {@link QueryDefinition}.
     *
     * @author zenind
     *
     */
    static class FileBasedQueryConverter implements QueryDefinitionConverter<String> {

        @Override
        public QueryDefinition convertFrom(String source, String id) {
            return new QueryDefinition(id, preProcessSignature(source));
        }

        protected String preProcessSignature(String rawURL) {
            return CfitUtils.substituteParameter(rawURL);
        }

    }
}
