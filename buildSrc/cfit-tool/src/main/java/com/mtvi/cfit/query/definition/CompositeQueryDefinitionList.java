package com.mtvi.cfit.query.definition;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.common.SizeAwareIterable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Composite query list.
 *
 * @author zenind
 */
public final class CompositeQueryDefinitionList implements QueryDefinitionList {

    /** Composite lists.*/
    private final Set<SizeAwareIterable<QueryDefinition>> lists = new HashSet<SizeAwareIterable<QueryDefinition>>();

    /**
     * Constructor.
     *
     * @param queryCollections - queries collections.
     */
    public CompositeQueryDefinitionList(Set<SizeAwareIterable<QueryDefinition>> queryCollections) {
        this.lists.addAll(queryCollections);
    }

    @Override
    public SizeAwareIterable<QueryDefinition> list() throws CfitException {
        return new SizeAwareIterable<QueryDefinition>() {

            @Override
            public int size() {
                int sum = 0;
                for (SizeAwareIterable iterable : lists) {
                    sum += iterable.size();
                }
                return sum;
            }

            @Override
            public Iterator<QueryDefinition> iterator() {
                Collection<Iterator<QueryDefinition>> iterators = Collections2.transform(
                    lists,
                    new Function<SizeAwareIterable<QueryDefinition>, Iterator<QueryDefinition>>() {
                        @Override
                        public Iterator<QueryDefinition> apply(SizeAwareIterable<QueryDefinition> input) {
                            return input.iterator();
                        }
                    }
                );

                return Iterators.concat(iterators.iterator());
            }
        };
    }

    public Set<SizeAwareIterable<QueryDefinition>> getLists() {
        return ImmutableSet.copyOf(lists);
    }
}
