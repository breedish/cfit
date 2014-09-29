package com.mtvi.cfit.query.common;

/**
 * Query iterable interface.
 *
 * @param <T> type of query.
 * @author zenind
 */
public interface SizeAwareIterable<T> extends Iterable<T> {

    /**
     * @return total size of items represented by <code>this</code> iterable.
     */
    int size();

}
