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
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.bytes.ByteCollection;
import java.util.Map;
import java.util.SortedMap;
import java.util.Comparator;

/**
 * A type-specific {@link SortedMap}; provides some additional methods that use polymorphism to
 * avoid (un)boxing.
 *
 * <p>
 * Additionally, this interface strengthens {@link #entrySet()}, {@link #keySet()},
 * {@link #values()}, {@link #comparator()}, {@link SortedMap#subMap(Object,Object)},
 * {@link SortedMap#headMap(Object)} and {@link SortedMap#tailMap(Object)}.
 *
 * @see SortedMap
 */
public interface Reference2ByteSortedMap<K> extends Reference2ByteMap<K>, SortedMap<K, Byte> {
	/**
	 * Returns a view of the portion of this sorted map whose keys range from {@code fromKey},
	 * inclusive, to {@code toKey}, exclusive.
	 *
	 * @apiNote Note that this specification strengthens the one given in
	 *          {@link SortedMap#subMap(Object,Object)}.
	 *
	 * @see SortedMap#subMap(Object,Object)
	 */
	@Override
	Reference2ByteSortedMap<K> subMap(K fromKey, K toKey);

	/**
	 * Returns a view of the portion of this sorted map whose keys are strictly less than {@code toKey}.
	 *
	 * @apiNote Note that this specification strengthens the one given in
	 *          {@link SortedMap#headMap(Object)}.
	 *
	 * @see SortedMap#headMap(Object)
	 */
	@Override
	Reference2ByteSortedMap<K> headMap(K toKey);

	/**
	 * Returns a view of the portion of this sorted map whose keys are greater than or equal to
	 * {@code fromKey}.
	 *
	 * @apiNote Note that this specification strengthens the one given in
	 *          {@link SortedMap#tailMap(Object)}.
	 *
	 * @see SortedMap#tailMap(Object)
	 */
	@Override
	Reference2ByteSortedMap<K> tailMap(K fromKey);

	/**
	 * A sorted entry set providing fast iteration.
	 *
	 * <p>
	 * In some cases (e.g., hash-based classes) iteration over an entry set requires the creation of a
	 * large number of entry objects. Some {@code fastutil} maps might return {@linkplain #entrySet()
	 * entry set} objects of type {@code FastSortedEntrySet}: in this case, {@link #fastIterator()
	 * fastIterator()} will return an iterator that is guaranteed not to create a large number of
	 * objects, <em>possibly by returning always the same entry</em> (of course, mutated).
	 */
	interface FastSortedEntrySet<K> extends ObjectSortedSet<Reference2ByteMap.Entry<K>>, FastEntrySet<K> {
		/**
		 * {@inheritDoc}
		 */
		@Override
		ObjectBidirectionalIterator<Reference2ByteMap.Entry<K>> fastIterator();

		/**
		 * Returns a fast iterator over this entry set, starting from a given element of the domain
		 * (optional operation); the iterator might return always the same entry instance, suitably mutated.
		 *
		 * @param from an element to start from.
		 * @return a fast iterator over this sorted entry set starting at {@code from}; the iterator might
		 *         return always the same entry object, suitably mutated.
		 */
		ObjectBidirectionalIterator<Reference2ByteMap.Entry<K>> fastIterator(Reference2ByteMap.Entry<K> from);
	}

	/**
	 * Returns a sorted-set view of the mappings contained in this map.
	 * 
	 * @apiNote Note that this specification strengthens the one given in the corresponding
	 *          type-specific unsorted map.
	 *
	 * @return a sorted-set view of the mappings contained in this map.
	 * @see SortedMap#entrySet()
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Deprecated
	@Override
	default ObjectSortedSet<Map.Entry<K, Byte>> entrySet() {
		return (ObjectSortedSet)reference2ByteEntrySet();
	}

	/**
	 * Returns a type-specific sorted-set view of the mappings contained in this map.
	 * 
	 * @apiNote Note that this specification strengthens the one given in the corresponding
	 *          type-specific unsorted map.
	 *
	 * @return a type-specific sorted-set view of the mappings contained in this map.
	 * @see #entrySet()
	 */
	@Override
	ObjectSortedSet<Reference2ByteMap.Entry<K>> reference2ByteEntrySet();

	/**
	 * Returns a type-specific sorted-set view of the keys contained in this map.
	 * 
	 * @apiNote Note that this specification strengthens the one given in the corresponding
	 *          type-specific unsorted map.
	 *
	 * @return a sorted-set view of the keys contained in this map.
	 * @see SortedMap#keySet()
	 */
	@Override
	ReferenceSortedSet<K> keySet();

	/**
	 * Returns a type-specific set view of the values contained in this map.
	 * 
	 * @apiNote Note that this specification strengthens the one given in {@link Map#values()}, which
	 *          was already strengthened in the corresponding type-specific class, but was weakened by
	 *          the fact that this interface extends {@link SortedMap}.
	 *
	 * @return a set view of the values contained in this map.
	 * @see SortedMap#values()
	 */
	@Override
	ByteCollection values();

	/**
	 * Returns the comparator associated with this sorted set, or null if it uses its keys' natural
	 * ordering.
	 *
	 * @apiNote Note that this specification strengthens the one given in
	 *          {@link SortedMap#comparator()}.
	 *
	 * @see SortedMap#comparator()
	 */
	@Override
	Comparator<? super K> comparator();
}
