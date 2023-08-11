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
package it.unimi.dsi.fastutil.ints;

import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.chars.AbstractCharCollection;
import it.unimi.dsi.fastutil.chars.CharIterator;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;

/**
 * An abstract class providing basic methods for sorted maps implementing a type-specific interface.
 */
public abstract class AbstractInt2CharSortedMap extends AbstractInt2CharMap implements Int2CharSortedMap {
	private static final long serialVersionUID = -1773560792952436569L;

	protected AbstractInt2CharSortedMap() {
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
	public IntSortedSet keySet() {
		return new KeySet();
	}

	/** A wrapper exhibiting the keys of a map. */
	protected class KeySet extends AbstractIntSortedSet {
		@Override
		public boolean contains(final int k) {
			return containsKey(k);
		}

		@Override
		public int size() {
			return AbstractInt2CharSortedMap.this.size();
		}

		@Override
		public void clear() {
			AbstractInt2CharSortedMap.this.clear();
		}

		@Override
		public IntComparator comparator() {
			return AbstractInt2CharSortedMap.this.comparator();
		}

		@Override
		public int firstInt() {
			return firstIntKey();
		}

		@Override
		public int lastInt() {
			return lastIntKey();
		}

		@Override
		public IntSortedSet headSet(final int to) {
			return headMap(to).keySet();
		}

		@Override
		public IntSortedSet tailSet(final int from) {
			return tailMap(from).keySet();
		}

		@Override
		public IntSortedSet subSet(final int from, final int to) {
			return subMap(from, to).keySet();
		}

		@Override
		public IntBidirectionalIterator iterator(final int from) {
			return new KeySetIterator(int2CharEntrySet().iterator(new BasicEntry(from, ((char)0))));
		}

		@Override
		public IntBidirectionalIterator iterator() {
			return new KeySetIterator(Int2CharSortedMaps.fastIterator(AbstractInt2CharSortedMap.this));
		}
	}

	/**
	 * A wrapper exhibiting a map iterator as an iterator on keys.
	 *
	 * <p>
	 * To provide an iterator on keys, just create an instance of this class using the corresponding
	 * iterator on entries.
	 */
	protected static class KeySetIterator implements IntBidirectionalIterator {
		protected final ObjectBidirectionalIterator<Int2CharMap.Entry> i;

		public KeySetIterator(ObjectBidirectionalIterator<Int2CharMap.Entry> i) {
			this.i = i;
		}

		@Override
		public int nextInt() {
			return i.next().getIntKey();
		};

		@Override
		public int previousInt() {
			return i.previous().getIntKey();
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
	public CharCollection values() {
		return new ValuesCollection();
	}

	/** A wrapper exhibiting the values of a map. */
	protected class ValuesCollection extends AbstractCharCollection {
		@Override
		public CharIterator iterator() {
			return new ValuesIterator(Int2CharSortedMaps.fastIterator(AbstractInt2CharSortedMap.this));
		}

		@Override
		public boolean contains(final char k) {
			return containsValue(k);
		}

		@Override
		public int size() {
			return AbstractInt2CharSortedMap.this.size();
		}

		@Override
		public void clear() {
			AbstractInt2CharSortedMap.this.clear();
		}
	}

	/**
	 * A wrapper exhibiting a map iterator as an iterator on values.
	 *
	 * <p>
	 * To provide an iterator on values, just create an instance of this class using the corresponding
	 * iterator on entries.
	 */
	protected static class ValuesIterator implements CharIterator {
		protected final ObjectBidirectionalIterator<Int2CharMap.Entry> i;

		public ValuesIterator(ObjectBidirectionalIterator<Int2CharMap.Entry> i) {
			this.i = i;
		}

		@Override
		public char nextChar() {
			return i.next().getCharValue();
		};

		@Override
		public boolean hasNext() {
			return i.hasNext();
		}
	}
}
