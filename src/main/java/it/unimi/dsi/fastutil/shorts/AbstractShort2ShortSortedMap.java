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

import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;

/**
 * An abstract class providing basic methods for sorted maps implementing a type-specific interface.
 */
public abstract class AbstractShort2ShortSortedMap extends AbstractShort2ShortMap implements Short2ShortSortedMap {
	private static final long serialVersionUID = -1773560792952436569L;

	protected AbstractShort2ShortSortedMap() {
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The view is backed by the sorted set returned by {@link java.util.Map#entrySet()}. Note that
	 * <em>no attempt is made at caching the result of this method</em>, as this would require adding
	 * some attributes that lightweight implementations would not need. Subclasses may easily override
	 * this policy by calling this method and caching the result, but implementors are encouraged to
	 * write more efficient ad-hoc implementations.
	 *
	 * @return a sorted set view of the keys of this map; it may be safely cast to a type-specific
	 *         interface.
	 */
	@Override
	public ShortSortedSet keySet() {
		return new KeySet();
	}

	/** A wrapper exhibiting the keys of a map. */
	protected class KeySet extends AbstractShortSortedSet {
		@Override
		public boolean contains(final short k) {
			return containsKey(k);
		}

		@Override
		public int size() {
			return AbstractShort2ShortSortedMap.this.size();
		}

		@Override
		public void clear() {
			AbstractShort2ShortSortedMap.this.clear();
		}

		@Override
		public ShortComparator comparator() {
			return AbstractShort2ShortSortedMap.this.comparator();
		}

		@Override
		public short firstShort() {
			return firstShortKey();
		}

		@Override
		public short lastShort() {
			return lastShortKey();
		}

		@Override
		public ShortSortedSet headSet(final short to) {
			return headMap(to).keySet();
		}

		@Override
		public ShortSortedSet tailSet(final short from) {
			return tailMap(from).keySet();
		}

		@Override
		public ShortSortedSet subSet(final short from, final short to) {
			return subMap(from, to).keySet();
		}

		@Override
		public ShortBidirectionalIterator iterator(final short from) {
			return new KeySetIterator(short2ShortEntrySet().iterator(new BasicEntry(from, ((short)0))));
		}

		@Override
		public ShortBidirectionalIterator iterator() {
			return new KeySetIterator(Short2ShortSortedMaps.fastIterator(AbstractShort2ShortSortedMap.this));
		}
	}

	/**
	 * A wrapper exhibiting a map iterator as an iterator on keys.
	 *
	 * <p>
	 * To provide an iterator on keys, just create an instance of this class using the corresponding
	 * iterator on entries.
	 */
	protected static class KeySetIterator implements ShortBidirectionalIterator {
		protected final ObjectBidirectionalIterator<Short2ShortMap.Entry> i;

		public KeySetIterator(ObjectBidirectionalIterator<Short2ShortMap.Entry> i) {
			this.i = i;
		}

		@Override
		public short nextShort() {
			return i.next().getShortKey();
		};

		@Override
		public short previousShort() {
			return i.previous().getShortKey();
		};

		@Override
		public boolean hasNext() {
			return i.hasNext();
		}

		@Override
		public boolean hasPrevious() {
			return i.hasPrevious();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The view is backed by the sorted set returned by {@link java.util.Map#entrySet()}. Note that
	 * <em>no attempt is made at caching the result of this method</em>, as this would require adding
	 * some attributes that lightweight implementations would not need. Subclasses may easily override
	 * this policy by calling this method and caching the result, but implementors are encouraged to
	 * write more efficient ad-hoc implementations.
	 *
	 * @return a type-specific collection view of the values contained in this map.
	 */
	@Override
	public ShortCollection values() {
		return new ValuesCollection();
	}

	/** A wrapper exhibiting the values of a map. */
	protected class ValuesCollection extends AbstractShortCollection {
		@Override
		public ShortIterator iterator() {
			return new ValuesIterator(Short2ShortSortedMaps.fastIterator(AbstractShort2ShortSortedMap.this));
		}

		@Override
		public boolean contains(final short k) {
			return containsValue(k);
		}

		@Override
		public int size() {
			return AbstractShort2ShortSortedMap.this.size();
		}

		@Override
		public void clear() {
			AbstractShort2ShortSortedMap.this.clear();
		}
	}

	/**
	 * A wrapper exhibiting a map iterator as an iterator on values.
	 *
	 * <p>
	 * To provide an iterator on values, just create an instance of this class using the corresponding
	 * iterator on entries.
	 */
	protected static class ValuesIterator implements ShortIterator {
		protected final ObjectBidirectionalIterator<Short2ShortMap.Entry> i;

		public ValuesIterator(ObjectBidirectionalIterator<Short2ShortMap.Entry> i) {
			this.i = i;
		}

		@Override
		public short nextShort() {
			return i.next().getShortValue();
		};

		@Override
		public boolean hasNext() {
			return i.hasNext();
		}
	}
}
