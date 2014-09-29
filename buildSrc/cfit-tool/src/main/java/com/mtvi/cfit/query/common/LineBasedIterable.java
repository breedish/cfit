package com.mtvi.cfit.query.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Specific list implementation for traversing queries from {@link #source} file.
 *
 * @param <T> - return type value.
 * Is not thread safe.
 *
 * @author zenind.
 */
public abstract class LineBasedIterable<T> implements SizeAwareIterable<T> {

    /** Max buffer size. 2.5Mb.*/
    private static final int MAX_BUFFER_SIZE = 10 * 8192;
    /** Buffered reader.*/
    private final BufferedReader reader;
    /** Source storage.*/
    private final File source;
    /** Size of list.*/
    private Integer size;

    /**
     * Constructor.
     * @param source - source.
     * @throws FileNotFoundException - if source file doesn't exist.
     */
    public LineBasedIterable(File source) throws FileNotFoundException {
        if (source == null) {
            throw new IllegalArgumentException(
                String.format("Null value has been passed in as required argument ['source'= null]"));
        }
        this.reader = new BufferedReader(new FileReader(source), MAX_BUFFER_SIZE);
        this.source = source;
    }

    @Override
    public int size() {
        if (size == null) {
            size = getLinesCount();
        }

        return size;
    }

    private int getLinesCount() {
        try {
            LineNumberReader lReader = new LineNumberReader(new FileReader(source), MAX_BUFFER_SIZE);
            lReader.skip(Long.MAX_VALUE);
            return lReader.getLineNumber();
        } catch (IOException e) {
            return 0;
        }
    }

    protected abstract T nextValue(String rawNext, int id);

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            /** Next item.*/
            private String next;

            private int id = 1;

            @Override
            public boolean hasNext() {
                if (next != null) {
                    return true;
                }

                getNext();
                return next != null;
            }

            @Override
            public T next() {
                if (next == null) {
                    getNext();
                }

                if (next == null) {
                    throw new NoSuchElementException();
                }

                id++;

                T def = nextValue(next, id);

                next = null;
                return def;
            }

            private void getNext() {
                try {
                    next = reader.readLine();
                } catch (IOException e) {
                    next = null;
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Unsupported operation");
            }
        };
    }

    /**
     * Simple string line iterable.
     *
     * @author zenind.
     */
    public static final class SimpleLineBasedIterable extends LineBasedIterable<String> {

        /**
         * Constructor.
         * @param source - source file.
         * @throws FileNotFoundException - in case of given file doesn't exist.
         */
        public SimpleLineBasedIterable(File source) throws FileNotFoundException {
            super(source);
        }

        @Override
        protected String nextValue(String rawNext, int id) {
            return rawNext;
        }
    }

}
