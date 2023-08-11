/*
	* Copyright (C) 2002-2022 Sebastiano Vigna
	*
	* Licensed under the Apache License, Version 2.0 (the "License");
	* you may not use this file except in compliance with the License.
	* You may obtain a copy of the License at
	*
	*     http://www.apache.org/licenses/LICENSE-2.0
	*
	* Unless required by applicable law or agreed to in writing, software
	* distributed under the License is distributed on an "AS IS" BASIS,
	* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	* See the License for the specific language governing permissions and
	* limitations under the License.
	*/
package it.unimi.dsi.fastutil.shorts;

import java.util.Spliterator;
import java.util.SortedSet;
import java.util.Collection;
import static it.unimi.dsi.fastutil.Size64.sizeOf;

/**
 * A type-specific {@link SortedSet}; provides some additional methods that use polymorphism to
 * avoid (un)boxing.
 *
 * <p>
 * Additionally, this interface strengthens {@link #iterator()}, {@link #comparator()} (for
 * primitive types), {@link SortedSet#subSet(Object,Object)}, {@link SortedSet#headSet(Object)} and
 * {@link SortedSet#tailSet(Object)}.
 *
 * @see SortedSet
 */
public interface ShortSortedSet extends ShortSet, SortedSet<Short>, ShortBidirectionalIterable {
	/**
	 * Returns a type-specific {@link it.unimi.dsi.fastutil.BidirectionalIterator} on the elements in
	 * this set, starting from a given element of the domain (optional operation).
	 *
	 * <p>
	 * This method returns a type-specific bidirectional iterator with given starting point. The
	 * starting point is any element comparable to the elements of this set (even if it does not
	 * actually belong to the set). The next element of the returned iterator is the least element of
	 * the set that is greater than the starting point (if there are no elements greater than the
	 * starting point, {@link it.unimi.dsi.fastutil.BidirectionalIterator#hasNext() hasNext()} will
	 * return {@code false}). The previous element of the returned iterator is the greatest element of
	 * the set that is smaller than or equal to the starting point (if there are no elements smaller
	 * than or equal to the starting point,
	 * {@link it.unimi.dsi.fastutil.BidirectionalIterator#hasPrevious() hasPrevious()} will return
	 * {@code false}).
	 *
	 * <p>
	 * Note that passing the last element of the set as starting point and calling
	 * {@link it.unimi.dsi.fastutil.BidirectionalIterator#previous() previous()} you can traverse the
	 * entire set in reverse order.
	 *
	 * @param fromElement an element to start from.
	 * @return a bidirectional iterator on the element in this set, starting at the given element.
	 * @throws UnsupportedOperationException if this set does not support iterators with a starting
	 *             point.
	 */
	ShortBidirectionalIterator iterator(short fromElement);

	/**
	 * Returns a type-specific {@link it.unimi.dsi.fastutil.BidirectionalIterator} on the elements in
	 * this set.
	 *
	 * <p>
	 * This method returns a parameterised bidirectional iterator. The iterator can be moreover safely
	 * cast to a type-specific iterator.
	 *
	 * @apiNote This specification strengthens the one given in the corresponding type-specific
	 *          {@link Collection}.
	 *
	 * @return a bidirectional iterator on the element in this set.
	 */
	@Override
	ShortBidirectionalIterator iterator();

	/**
	 * Returns a type-specific spliterator on the elements of this sorted-set.
	 *
	 * <p>
	 * SortedSet spliterators must report at least {@link Spliterator#DISTINCT},
	 * {@link Spliterator#ORDERED}, and {@link Spliterator#SORTED}. The returned spliterator's
	 * {@link Spliterator#getComparator() getComparator()} must be the same (or at the very least,
	 * consistent with) this instance's {@link #comparator()}.
	 *
	 * <p>
	 * See {@link java.util.SortedSet#spliterator()} for more documentation on the requirements of the
	 * returned spliterator.
	 *
	 * @apiNote This specification strengthens the one given in
	 *          {@link java.util.Collection#spliterator()}, which was already strengthened in the
	 *          corresponding type-specific class, but was weakened by the fact that this interface
	 *          extends {@link SortedSet}.
	 *          <p>
	 *          Also, this is generally the only {@code spliterator} method subclasses should override.
	 *
	 * @implSpec The default implementation returns a late-binding spliterator (see {@link Spliterator}
	 *           for documentation on what binding policies mean) that wraps this instance's type
	 *           specific {@link #iterator}.
	 *           <p>
	 *           Additionally, it reports {@link Spliterator#SIZED}, {@link Spliterator#DISTINCT},
	 *           {@link Spliterator#SORTED}, and {@link Spliterator#ORDERED}. The reported
	 *           {@link java.util.Comparator} from {@link Spliterator#getComparator()} will be the one
	 *           reported by this instance's {@link #comparator()}.
	 *
	 * @implNote As this default implementation wraps the iterator, and {@link java.util.Iterator} is an
	 *           inherently linear API, the returned spliterator will yield limited performance gains
	 *           when run in parallel contexts, as the returned spliterator's
	 *           {@link Spliterator#trySplit() trySplit()} will have linear runtime.
	 *
	 * @return {@inheritDoc}
	 * @since 8.5.0
	 */
	@Override
	default ShortSpliterator spliterator() {
		return ShortSpliterators.asSpliteratorFromSorted(iterator(), sizeOf(this), ShortSpliterators.SORTED_SET_SPLITERATOR_CHARACTERISTICS, comparator());
	}

	/**
	 * Returns a view of the portion of this sorted set whose elements range from {@code fromElement},
	 * inclusive, to {@code toElement}, exclusive.
	 * 
	 * @apiNote This specification strengthens the one given in {@link SortedSet#subSet(Object,Object)}.
	 * @see SortedSet#subSet(Object,Object)
	 */
	ShortSortedSet subSet(short fromElement, short toElement);

	/**
	 * Returns a view of the portion of this sorted set whose elements are strictly less than
	 * {@code toElement}.
	 * 
	 * @apiNote This specification strengthens the one given in {@link SortedSet#headSet(Object)}.
	 * @see SortedSet#headSet(Object)
	 */
	ShortSortedSet headSet(short toElement);

	/**
	 * Returns a view of the portion of this sorted set whose elements are greater than or equal to
	 * {@code fromElement}.
	 * 
	 * @apiNote This specification strengthens the one given in {@link SortedSet#headSet(Object)}.
	 * @see SortedSet#tailSet(Object)
	 */
	ShortSortedSet tailSet(short fromElement);

	/**
	 * {@inheritDoc}
	 * 
	 * @apiNote This specification strengthens the one given in {@link SortedSet#comparator()}.
	 */
	@Override
	ShortComparator comparator();

	/**
	 * Returns the first (lowest) element currently in this set.
	 * 
	 * @see SortedSet#first()
	 */
	short firstShort();

	/**
	 * Returns the last (highest) element currently in this set.
	 * 
	 * @see SortedSet#last()
	 */
	short lastShort();

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default ShortSortedSet subSet(final Short from, final Short to) {
		return subSet(from.shortValue(), to.shortValue());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default ShortSortedSet headSet(final Short to) {
		return headSet(to.shortValue());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default ShortSortedSet tailSet(final Short from) {
		return tailSet(from.shortValue());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Short first() {
		return Short.valueOf(firstShort());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Short last() {
		return Short.valueOf(lastShort());
	}
}
