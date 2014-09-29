package com.mtvi.cfit.query.response;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.definition.QueryDefinition;
import com.mtvi.cfit.util.IOUtils;
import org.apache.http.HttpStatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * ByDefault query response manager implementation based on <code>File</code> storage type.
 *
 * @author zenind
 */
public class ToFileQueryResponseManager implements QueryResponseManager<File> {

    /** Header element delimiter.*/
    private static final String HEADER_SEPARATOR = "||";
    /** New line delimiter.*/
    private static final String NEW_LINE = System.getProperty("line.separator");

    @Override
    @SuppressWarnings("all")
    public void save(QueryResponse response, File storage) throws CfitException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(storage));
            String view = convert(response);
            writer.write(view);
        } catch (IOException e) {
            throw new CfitException(String.format("Error during saving response for %s", response.getDefinition().getId()), e);
        } finally {
            IOUtils.closeWriter(writer);
        }
    }

    protected String convert(QueryResponse response) throws CfitException {
        return  createHeader(response)
            + NEW_LINE
            + response.getContent();
    }

    protected String createHeader(QueryResponse response) {
        return Joiner.on(HEADER_SEPARATOR).join(
                Arrays.asList(response.getUrl(), response.getTime(), response.getStatus(), response.getSize()));
    }

    @Override
    public QueryResponse load(File source) throws CfitException {
        try {
            LineNumberReader contentReader = new LineNumberReader(new FileReader(source));

            List<String> headers =  Splitter.on(HEADER_SEPARATOR).splitToList(contentReader.readLine());

            String url = headers.get(0);

            return new FileQueryResponseProxy(source, new QueryDefinition(source.getName(), url), url,
                getHeaderValue(headers, 2, Integer.class, HttpStatus.SC_OK),
                getHeaderValue(headers, 1, Integer.class, 0));
        } catch (IOException e) {
            throw new CfitException(String.format("Unable to convert query from %s", source.getName()), e);
        }
    }

    private <T extends Number> T getHeaderValue(List<String> headers, int index, Class<T> clazz, T def) throws CfitException {
        if (index >= headers.size()) {
            return def;
        }

        String headerValue = headers.get(index);

        if (headerValue == null) {
            return def;
        }

        try {
            Method valueOf = clazz.getMethod("valueOf", String.class);
            return clazz.cast(valueOf.invoke(null, headerValue));
        } catch (Exception e) {
            throw new CfitException(e);
        }
    }
}
