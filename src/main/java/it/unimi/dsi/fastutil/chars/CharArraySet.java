/*
	* Copyright (C) 2007-2022 Sebastiano Vigna
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
package it.unimi.dsi.fastutil.chars;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A simple, brute-force implementation of a set based on a backing array.
 *
 * <p>
 * The main purpose of this implementation is that of wrapping cleanly the brute-force approach to
 * the storage of a very small number of items: just put them into an array and scan linearly to
 * find an item.
 */
public class CharArraySet extends AbstractCharSet implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	/** The backing array (valid up to {@link #size}, excluded). */
	private transient char[] a;
	/** The number of valid entries in {@link #a}. */
	private int size;

	/**
	 * Creates a new array set using the given backing array. The resulting set will have as many
	 * elements as the array.
	 *
	 * <p>
	 * It is the responsibility of the caller to ensure that the elements of {@code a} are distinct.
	 *
	 * @param a the backing array.
	 */
	public CharArraySet(final char[] a) {
		this.a = a;
		size = a.length;
	}

	/**
	 * Creates a new empty array set.
	 */
	public CharArraySet() {
		this.a = CharArrays.EMPTY_ARRAY;
	}

	/**
	 * Creates a new empty array set of given initial capacity.
	 *
	 * @param capacity the initial capacity.
	 */
	public CharArraySet(final int capacity) {
		this.a = new char[capacity];
	}

	/**
	 * Creates a new array set copying the contents of a given collection.
	 * 
	 * @param c a collection.
	 */
	public CharArraySet(CharCollection c) {
		this(c.size());
		addAll(c);
	}

	/**
	 * Creates a new array set copying the contents of a given set.
	 * 
	 * @param c a collection.
	 */
	public CharArraySet(final Collection<? extends Character> c) {
		this(c.size());
		addAll(c);
	}

	/**
	 * Creates a new array set copying the contents of a given collection.
	 * 
	 * @param c a collection.
	 */
	public CharArraySet(CharSet c) {
		this(c.size());
		int i = 0;
		for (char x : c) {
			a[i] = x;
			i++;
		}
		size = i;
	}

	/**
	 * Creates a new array set copying the contents of a given set.
	 * 
	 * @param c a collection.
	 */
	public CharArraySet(final Set<? extends Character> c) {
		this(c.size());
		int i = 0;
		for (Character x : c) {
			a[i] = (x).charValue();
			i++;
		}
		size = i;
	}

	/**
	 * Creates a new array set using the given backing array and the given number of elements of the
	 * array.
	 *
	 * <p>
	 * It is the responsibility of the caller to ensure that the first {@code size} elements of
	 * {@code a} are distinct.
	 *
	 * @param a the backing array.
	 * @param size the number of valid elements in {@code a}.
	 */
	public CharArraySet(final char[] a, final int size) {
		this.a = a;
		this.size = size;
		if (size > a.length) throw new IllegalArgumentException("The provided size (" + size + ") is larger than or equal to the array size (" + a.length + ")");
	}

	// The 0 and 1 arg overloads allow us to skip the temporary hash set creation.
	/**
	 * Creates a new empty array set.
	 *
	 * @return a new empty array set.
	 */
	public static CharArraySet of() {
		return ofUnchecked();
	}

	/**
	 * Creates a new array set using the element given.
	 *
	 * @param e the element that the returned set will contain.
	 * @return a new array set containing {@code e}.
	 */
	public static CharArraySet of(final char e) {
		return ofUnchecked(e);
	}

	/**
	 * Creates a new array set using an array of elements.
	 *
	 * <p>
	 * Unlike the array accepting constructors, this method will throw if duplicate elements are
	 * encountered. This adds a non-trivial validation burden. Use {@link #ofUnchecked} if you know your
	 * input has no duplicates, which will skip this validation.
	 *
	 * @param a the backing array of the returned array set.
	 * @throws IllegalArgumentException if there were duplicate entries.
	 * @return a new array set containing the elements in {@code a}.
	 */

	public static CharArraySet of(final char... a) {
		if (a.length == 2) {
			if (((a[0]) == (a[1]))) {
				throw new IllegalArgumentException("Duplicate element: " + a[1]);
			}
		} else if (a.length > 2) {
			// Will throw on a duplicate entry for us.
			CharOpenHashSet.of(a);
		}
		return ofUnchecked(a);
	}

	/**
	 * Creates a new empty array set.
	 * 
	 * @return a new empty array set.
	 */
	public static CharArraySet ofUnchecked() {
		return new CharArraySet();
	}

	// No 1 element overload; we want the temporary array constructed for us in the varargs overload
	/**
	 * Creates a new array set using an array of elements.
	 *
	 * <p>
	 * It is the responsibility of the caller to ensure that the elements of {@code a} are distinct.
	 *
	 * @param a the backing array of the returned array set.
	 * @return a new array set containing the elements in {@code a}.
	 */

	public static CharArraySet ofUnchecked(final char... a) {
		return new CharArraySet(a);
	}

	private int findKey(final char o) {
		for (int i = size; i-- != 0;) if (((a[i]) == (o))) return i;
		return -1;
	}

	// TODO Maybe make this return a list-iterator like the LinkedXHashSets do?
	@Override

	public CharIterator iterator() {
		return new CharIterator() {
			int next = 0;

			@Override
			public boolean hasNext() {
				return next < size;
			}

			@Override
			public char nextChar() {
				if (!hasNext()) throw new NoSuchElementException();
				return a[next++];
			}

			@Override
			public void remove() {
				final int tail = size-- - next--;
				System.arraycopy(a, next + 1, a, next, tail);
			}

			@Override
			public int skip(int n) {
				if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
				final int remaining = size - next;
				if (n < remaining) {
					next += n;
					return n;
				}
				n = remaining;
				next = size;
				return n;
			}
		};
	}

	// If you update this, you will probably want to update ArrayList as well
	private final class Spliterator implements CharSpliterator {
		// Until we split, we will track the size of the set
		// Once we split, then we stop updating on structural modifications.
		// Aka, size is late-binding.
		boolean hasSplit = false;
		int pos, max;

		public Spliterator() {
			this(0, CharArraySet.this.size, false);
		}

		private Spliterator(int pos, int max, boolean hasSplit) {
			assert pos <= max : "pos " + pos + " must be <= max " + max;
			this.pos = pos;
			this.max = max;
			this.hasSplit = hasSplit;
		}

		private int getWorkingMax() {
			return hasSplit ? max : CharArraySet.this.size;
		}

		@Override
		public int characteristics() {
			return CharSpliterators.SET_SPLITERATOR_CHARACTERISTICS | java.util.Spliterator.SUBSIZED | java.util.Spliterator.ORDERED;
		}

		@Override
		public long estimateSize() {
			return getWorkingMax() - pos;
		}

		@Override
		public boolean tryAdvance(final CharConsumer action) {
			if (pos >= getWorkingMax()) return false;
			action.accept(a[pos++]);
			return true;
		}

		@Override
		public void forEachRemaining(final CharConsumer action) {
			for (final int max = getWorkingMax(); pos < max; ++pos) {
				action.accept(a[pos]);
			}
		}

		@Override
		public long skip(long n) {
			if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
			final int max = getWorkingMax();
			if (pos >= max) return 0;
			final int remaining = max - pos;
			if (n < remaining) {
				pos = it.unimi.dsi.fastutil.SafeMath.safeLongToInt(pos + n);
				return n;
			}
			n = remaining;
			pos = max;
			return n;
		}

		@Override
		public CharSpliterator trySplit() {
			final int max = getWorkingMax();
			int retLen = (max - pos) >> 1;
			if (retLen <= 1) return null;
			// Update instance max with the last seen list size (if needed) before continuing
			this.max = max;
			int myNewPos = pos + retLen;
			int retMax = myNewPos;
			int oldPos = pos;
			this.pos = myNewPos;
			this.hasSplit = true;
			return new Spliterator(oldPos, retMax, true);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * In addition to the usual trait of {@link java.util.Spliterator#DISTINCT DISTINCT} for sets, the
	 * returned spliterator will also {@linkplain java.util.Spliterator#characteristics() report} the
	 * trait {@link java.util.Spliterator#ORDERED ORDERED}.
	 *
	 * <p>
	 * The returned spliterator is late-binding; it will track structural changes after the current
	 * item, up until the first {@link java.util.Spliterator#trySplit() trySplit()}, at which point the
	 * maximum index will be fixed. <br>
	 * Structural changes before the current item or after the first
	 * {@link java.util.Spliterator#trySplit() trySplit()} will result in unspecified behavior.
	 */
	@Override
	public CharSpliterator spliterator() {
		return new Spliterator();
	}

	@Override
	public boolean contains(final char k) {
		return findKey(k) != -1;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean remove(final char k) {
		final int pos = findKey(k);
		if (pos == -1) return false;
		final int tail = size - pos - 1;
		for (int i = 0; i < tail; i++) a[pos + i] = a[pos + i + 1];
		size--;
		return true;
	}

	@Override
	public boolean add(final char k) {
		final int pos = findKey(k);
		if (pos != -1) return false;
		if (size == a.length) {
			final char[] b = new char[size == 0 ? 2 : size * 2];
			for (int i = size; i-- != 0;) b[i] = a[i];
			a = b;
		}
		a[size++] = k;
		return true;
	}

	@Override
	public void clear() {
		size = 0;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public char[] toCharArray() {
		return java.util.Arrays.copyOf(a, size);
	}

	@Override
	public char[] toArray(char a[]) {
		if (a == null || a.length < size) a = new char[size];
		System.arraycopy(this.a, 0, a, 0, size);
		return a;
	}

	/**
	 * Returns a deep copy of this set.
	 *
	 * <p>
	 * This method performs a deep copy of this array set; the data stored in the set, however, is not
	 * cloned. Note that this makes a difference only for object keys.
	 *
	 * @return a deep copy of this set.
	 */
	@Override

	public CharArraySet clone() {
		CharArraySet c;
		try {
			c = (CharArraySet)super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new InternalError();
		}
		c.a = a.clone();
		return c;
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		s.defaultWriteObject();
		for (int i = 0; i < size; i++) s.writeChar(a[i]);
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		a = new char[size];
		for (int i = 0; i < size; i++) a[i] = s.readChar();
	}
}
