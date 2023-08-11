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
package it.unimi.dsi.fastutil.floats;

import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;
import java.util.NoSuchElementException;
import it.unimi.dsi.fastutil.BigArrays;
import static it.unimi.dsi.fastutil.BigArrays.length;
import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.Size64;

/**
 * A type-specific big list based on a big array; provides some additional methods that use
 * polymorphism to avoid (un)boxing.
 *
 * <p>
 * This class implements a lightweight, fast, open, optimized, reuse-oriented version of
 * big-array-based big lists. Instances of this class represent a big list with a big array that is
 * enlarged as needed when new entries are created (by increasing its current length by 50%), but is
 * <em>never</em> made smaller (even on a {@link #clear()}). A family of {@linkplain #trim()
 * trimming methods} lets you control the size of the backing big array; this is particularly useful
 * if you reuse instances of this class. Range checks are equivalent to those of {@link java.util}'s
 * classes, but they are delayed as much as possible. The backing big array is exposed by the
 * {@link #elements()} method.
 *
 * <p>
 * This class implements the bulk methods {@code removeElements()}, {@code addElements()} and
 * {@code getElements()} using high-performance system calls (e.g.,
 * {@link System#arraycopy(Object,int,Object,int,int) System.arraycopy()}) instead of expensive
 * loops.
 *
 * @see java.util.ArrayList
 */
public class FloatBigArrayBigList extends AbstractFloatBigList implements RandomAccess, Cloneable, java.io.Serializable {
	private static final long serialVersionUID = -7046029254386353130L;
	/** The initial default capacity of a big-array big list. */
	public static final int DEFAULT_INITIAL_CAPACITY = 10;
	/** The backing big array. */
	protected transient float a[][];
	/** The current actual size of the big list (never greater than the backing-array length). */
	protected long size;

	/**
	 * Creates a new big-array big list using a given array.
	 *
	 * <p>
	 * This constructor is only meant to be used by the wrapping methods.
	 *
	 * @param a the big array that will be used to back this big-array big list.
	 */
	protected FloatBigArrayBigList(final float a[][], @SuppressWarnings("unused") boolean dummy) {
		this.a = a;
	}

	/**
	 * Creates a new big-array big list with given capacity.
	 *
	 * @param capacity the initial capacity of the array list (may be 0).
	 */

	public FloatBigArrayBigList(final long capacity) {
		if (capacity < 0) throw new IllegalArgumentException("Initial capacity (" + capacity + ") is negative");
		if (capacity == 0) a = FloatBigArrays.EMPTY_BIG_ARRAY;
		else a = FloatBigArrays.newBigArray(capacity);
	}

	/** Creates a new big-array big list with {@link #DEFAULT_INITIAL_CAPACITY} capacity. */

	public FloatBigArrayBigList() {
		a = FloatBigArrays.DEFAULT_EMPTY_BIG_ARRAY; // We delay allocation
	}

	/**
	 * Creates a new big-array big list and fills it with a given type-specific collection.
	 *
	 * @param c a type-specific collection that will be used to fill the array list.
	 */
	public FloatBigArrayBigList(final FloatCollection c) {
		this(Size64.sizeOf(c));
		if (c instanceof FloatBigList) {
			((FloatBigList)c).getElements(0, a, 0, size = Size64.sizeOf(c));
		} else {
			for (FloatIterator i = c.iterator(); i.hasNext();) add(i.nextFloat());
		}
	}

	/**
	 * Creates a new big-array big list and fills it with a given type-specific list.
	 *
	 * @param l a type-specific list that will be used to fill the array list.
	 */
	public FloatBigArrayBigList(final FloatBigList l) {
		this(l.size64());
		l.getElements(0, a, 0, size = l.size64());
	}

	/**
	 * Creates a new big-array big list and fills it with the elements of a given big array.
	 *
	 * @param a a big array whose elements will be used to fill the array list.
	 */
	public FloatBigArrayBigList(final float a[][]) {
		this(a, 0, length(a));
	}

	/**
	 * Creates a new big-array big list and fills it with the elements of a given big array.
	 *
	 * @param a a big array whose elements will be used to fill the array list.
	 * @param offset the first element to use.
	 * @param length the number of elements to use.
	 */
	public FloatBigArrayBigList(final float a[][], final long offset, final long length) {
		this(length);
		BigArrays.copy(a, offset, this.a, 0, length);
		size = length;
	}

	/**
	 * Creates a new big-array big list and fills it with the elements returned by an iterator..
	 *
	 * @param i an iterator whose returned elements will fill the array list.
	 */
	public FloatBigArrayBigList(final Iterator<? extends Float> i) {
		this();
		while (i.hasNext()) this.add((i.next()).floatValue());
	}

	/**
	 * Creates a new big-array big list and fills it with the elements returned by a type-specific
	 * iterator..
	 *
	 * @param i a type-specific iterator whose returned elements will fill the array list.
	 */
	public FloatBigArrayBigList(final FloatIterator i) {
		this();
		while (i.hasNext()) this.add(i.nextFloat());
	}

	/**
	 * Returns the backing big array of this big list.
	 *
	 * @return the backing big array.
	 */
	public float[][] elements() {
		return a;
	}

	/**
	 * Wraps a given big array into a big-array list of given size.
	 *
	 * @param a a big array to wrap.
	 * @param length the length of the resulting big-array list.
	 * @return a new big-array list of the given size, wrapping the given big array.
	 */
	public static FloatBigArrayBigList wrap(final float a[][], final long length) {
		if (length > length(a)) throw new IllegalArgumentException("The specified length (" + length + ") is greater than the array size (" + length(a) + ")");
		final FloatBigArrayBigList l = new FloatBigArrayBigList(a, false);
		l.size = length;
		return l;
	}

	/**
	 * Wraps a given big array into a big-array big list.
	 *
	 * @param a a big array to wrap.
	 * @return a new big-array big list wrapping the given array.
	 */
	public static FloatBigArrayBigList wrap(final float a[][]) {
		return wrap(a, length(a));
	}

	/**
	 * Creates a new empty big array list.
	 *
	 * @return a new empty big-array big list.
	 */
	public static FloatBigArrayBigList of() {
		return new FloatBigArrayBigList();
	}

	/**
	 * Creates a big array list using a list of elements.
	 *
	 * @param init a list of elements that will be used to initialize the big list. It is possible (but
	 *            not assured) that the returned big-array big list will be backed by the given array in
	 *            one of its segments.
	 * @return a new big-array big list containing the given elements.
	 * @see BigArrays#wrap
	 */

	public static FloatBigArrayBigList of(final float... init) {
		return wrap(BigArrays.wrap(init));
	}

	/**
	 * Ensures that this big-array big list can contain the given number of entries without resizing.
	 *
	 * @param capacity the new minimum capacity for this big-array big list.
	 */

	public void ensureCapacity(final long capacity) {
		if (capacity <= length(a) || a == FloatBigArrays.DEFAULT_EMPTY_BIG_ARRAY) return;
		a = BigArrays.forceCapacity(a, capacity, size);
		assert size <= length(a);
	}

	/**
	 * Grows this big-array big list, ensuring that it can contain the given number of entries without
	 * resizing, and in case increasing current capacity at least by a factor of 50%.
	 *
	 * @param capacity the new minimum capacity for this big-array big list.
	 */

	private void grow(long capacity) {
		final long oldLength = length(a);
		if (capacity <= oldLength) return;
		if (a != FloatBigArrays.DEFAULT_EMPTY_BIG_ARRAY) capacity = Math.max(oldLength + (oldLength >> 1), capacity);
		else if (capacity < DEFAULT_INITIAL_CAPACITY) capacity = DEFAULT_INITIAL_CAPACITY;
		a = BigArrays.forceCapacity(a, capacity, size);
		assert size <= length(a);
	}

	@Override
	public void add(final long index, final float k) {
		ensureIndex(index);
		grow(size + 1);
		if (index != size) BigArrays.copy(a, index, a, index + 1, size - index);
		BigArrays.set(a, index, k);
		size++;
		assert size <= length(a);
	}

	@Override
	public boolean add(final float k) {
		grow(size + 1);
		BigArrays.set(a, size++, k);
		assert size <= length(a);
		return true;
	}

	@Override
	public float getFloat(final long index) {
		if (index >= size) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
		return BigArrays.get(a, index);
	}

	@Override
	public long indexOf(final float k) {
		for (long i = 0; i < size; i++) if ((Float.floatToIntBits(k) == Float.floatToIntBits(BigArrays.get(a, i)))) return i;
		return -1;
	}

	@Override
	public long lastIndexOf(final float k) {
		for (long i = size; i-- != 0;) if ((Float.floatToIntBits(k) == Float.floatToIntBits(BigArrays.get(a, i)))) return i;
		return -1;
	}

	@Override
	public float removeFloat(final long index) {
		if (index >= size) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
		final float old = BigArrays.get(a, index);
		size--;
		if (index != size) BigArrays.copy(a, index + 1, a, index, size - index);
		assert size <= length(a);
		return old;
	}

	@Override
	public boolean rem(final float k) {
		final long index = indexOf(k);
		if (index == -1) return false;
		removeFloat(index);
		assert size <= length(a);
		return true;
	}

	@Override
	public float set(final long index, final float k) {
		if (index >= size) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
		float old = BigArrays.get(a, index);
		BigArrays.set(a, index, k);
		return old;
	}

	@Override
	public boolean removeAll(final FloatCollection c) {
		float[] s = null, d = null;
		int ss = -1, sd = BigArrays.SEGMENT_SIZE, ds = -1, dd = BigArrays.SEGMENT_SIZE;
		for (long i = 0; i < size; i++) {
			if (sd == BigArrays.SEGMENT_SIZE) {
				sd = 0;
				s = a[++ss];
			}
			if (!c.contains(s[sd])) {
				if (dd == BigArrays.SEGMENT_SIZE) {
					d = a[++ds];
					dd = 0;
				}
				d[dd++] = s[sd];
			}
			sd++;
		}
		final long j = BigArrays.index(ds, dd);
		final boolean modified = size != j;
		size = j;
		return modified;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		float[] s = null, d = null;
		int ss = -1, sd = BigArrays.SEGMENT_SIZE, ds = -1, dd = BigArrays.SEGMENT_SIZE;
		for (long i = 0; i < size; i++) {
			if (sd == BigArrays.SEGMENT_SIZE) {
				sd = 0;
				s = a[++ss];
			}
			if (!c.contains(Float.valueOf(s[sd]))) {
				if (dd == BigArrays.SEGMENT_SIZE) {
					d = a[++ds];
					dd = 0;
				}
				d[dd++] = s[sd];
			}
			sd++;
		}
		final long j = BigArrays.index(ds, dd);
		final boolean modified = size != j;
		size = j;
		return modified;
	}

	@Override
	public boolean addAll(long index, final FloatCollection c) {
		if (c instanceof FloatList) {
			return addAll(index, (FloatList)c);
		}
		if (c instanceof FloatBigList) {
			return addAll(index, (FloatBigList)c);
		}
		ensureIndex(index);
		int n = c.size();
		if (n == 0) return false;
		grow(size + n);
		BigArrays.copy(a, index, a, index + n, size - index);
		final FloatIterator i = c.iterator();
		size += n;
		assert size <= length(a);
		while (n-- != 0) BigArrays.set(a, index++, i.nextFloat());
		return true;
	}

	@Override
	public boolean addAll(final long index, final FloatBigList list) {
		ensureIndex(index);
		final long n = list.size64();
		if (n == 0) return false;
		grow(size + n);
		BigArrays.copy(a, index, a, index + n, size - index);
		list.getElements(0, a, index, n);
		size += n;
		assert size <= length(a);
		return true;
	}

	@Override
	public boolean addAll(final long index, final FloatList list) {
		ensureIndex(index);
		int n = list.size();
		if (n == 0) return false;
		grow(size + n);
		BigArrays.copy(a, index, a, index + n, size - index);
		size += n;
		assert size <= length(a);
		int segment = BigArrays.segment(index);
		int displ = BigArrays.displacement(index);
		int pos = 0;
		while (n > 0) {
			final int l = Math.min(a[segment].length - displ, n);
			list.getElements(pos, a[segment], displ, l);
			if ((displ += l) == BigArrays.SEGMENT_SIZE) {
				displ = 0;
				segment++;
			}
			pos += l;
			n -= l;
		}
		return true;
	}

	@Override
	public void clear() {
		size = 0;
		assert size <= length(a);
	}

	@Override
	public long size64() {
		return size;
	}

	@Override
	public void size(final long size) {
		if (size > length(a)) a = BigArrays.forceCapacity(a, size, this.size);
		if (size > this.size) BigArrays.fill(a, this.size, size, (0));
		this.size = size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Trims this big-array big list so that the capacity is equal to the size.
	 *
	 * @see java.util.ArrayList#trimToSize()
	 */
	public void trim() {
		trim(0);
	}

	/**
	 * Trims the backing big array if it is too large.
	 *
	 * If the current big array length is smaller than or equal to {@code n}, this method does nothing.
	 * Otherwise, it trims the big-array length to the maximum between {@code n} and {@link #size64()}.
	 *
	 * <p>
	 * This method is useful when reusing big lists. {@linkplain #clear() Clearing a big list} leaves
	 * the big-array length untouched. If you are reusing a big list many times, you can call this
	 * method with a typical size to avoid keeping around a very large big array just because of a few
	 * large transient big lists.
	 *
	 * @param n the threshold for the trimming.
	 */
	public void trim(final long n) {
		final long arrayLength = length(a);
		if (n >= arrayLength || size == arrayLength) return;
		a = BigArrays.trim(a, Math.max(n, size));
		assert size <= length(a);
	}

	private class SubList extends AbstractFloatBigList.FloatRandomAccessSubList {
		private static final long serialVersionUID = -3185226345314976296L;

		protected SubList(long from, long to) {
			super(FloatBigArrayBigList.this, from, to);
		}

		// Needed because we can't access the parent class' instance variables directly in a different
		// instance of SubList.
		private float[][] getParentArray() {
			return a;
		}

		// Most of the inherited methods should be fine, but we can override a few of them for performance.
		@Override
		public float getFloat(long i) {
			ensureRestrictedIndex(i);
			return BigArrays.get(a, i + from);
		}

		private final class SubListIterator extends FloatBigListIterators.AbstractIndexBasedBigListIterator {
			// We are using pos == 0 to be 0 relative to SubList.from (meaning you need to do a[from + i] when
			// accessing array).
			SubListIterator(long index) {
				super(0, index);
			}

			@Override
			protected final float get(long i) {
				return BigArrays.get(a, from + i);
			}

			@Override
			protected final void add(long i, float k) {
				SubList.this.add(i, k);
			}

			@Override
			protected final void set(long i, float k) {
				SubList.this.set(i, k);
			}

			@Override
			protected final void remove(long i) {
				SubList.this.removeFloat(i);
			}

			@Override
			protected final long getMaxPos() {
				return to - from;
			}

			@Override
			public float nextFloat() {
				if (!hasNext()) throw new NoSuchElementException();
				return BigArrays.get(a, from + (lastReturned = pos++));
			}

			@Override
			public float previousFloat() {
				if (!hasPrevious()) throw new NoSuchElementException();
				return BigArrays.get(a, from + (lastReturned = --pos));
			}

			@Override
			public void forEachRemaining(final FloatConsumer action) {
				final long max = to - from;
				while (pos < max) {
					action.accept(BigArrays.get(a, from + (lastReturned = pos++)));
				}
			}
		}

		@Override
		public FloatBigListIterator listIterator(long index) {
			return new SubListIterator(index);
		}

		private final class SubListSpliterator extends FloatBigSpliterators.LateBindingSizeIndexBasedSpliterator {
			// We are using pos == 0 to be 0 relative to real array 0
			SubListSpliterator() {
				super(from);
			}

			private SubListSpliterator(long pos, long maxPos) {
				super(pos, maxPos);
			}

			@Override
			protected final long getMaxPosFromBackingStore() {
				return to;
			}

			@Override
			protected final float get(long i) {
				return BigArrays.get(a, i);
			}

			@Override
			protected final SubListSpliterator makeForSplit(long pos, long maxPos) {
				return new SubListSpliterator(pos, maxPos);
			}

			@Override
			protected final long computeSplitPoint() {
				long defaultSplit = super.computeSplitPoint();
				// Align to outer array starting point if possible.
				// We add/subtract one to the bounds to ensure the new pos will always shrink the range
				return BigArrays.nearestSegmentStart(defaultSplit, pos + 1, getMaxPos() - 1);
			}

			@Override
			public boolean tryAdvance(final FloatConsumer action) {
				if (pos >= getMaxPos()) return false;
				action.accept(BigArrays.get(a, pos++));
				return true;
			}

			@Override
			public void forEachRemaining(final FloatConsumer action) {
				final long max = getMaxPos();
				while (pos < max) {
					action.accept(BigArrays.get(a, pos++));
				}
			}
		}

		@Override
		public FloatSpliterator spliterator() {
			return new SubListSpliterator();
		}

		boolean contentsEquals(float[][] otherA, long otherAFrom, long otherATo) {
			if (a == otherA && from == otherAFrom && to == otherATo) return true;
			if (otherATo - otherAFrom != size64()) {
				return false;
			}
			long pos = to, otherPos = otherATo;
			// We have already assured that the two ranges are the same size, so we only need to check one
			// bound.
			// If BigArrays.equals ever gets an overload that accepts bounds, use that instead
			// (but make sure to break out the reference equality case).
			while (--pos >= from) if (BigArrays.get(a, pos) != BigArrays.get(otherA, --otherPos)) return false;
			return true;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (o == null) return false;
			if (!(o instanceof BigList)) return false;
			if (o instanceof FloatBigArrayBigList) {

				FloatBigArrayBigList other = (FloatBigArrayBigList)o;
				return contentsEquals(other.a, 0, other.size64());
			}
			if (o instanceof FloatBigArrayBigList.SubList) {

				FloatBigArrayBigList.SubList other = (FloatBigArrayBigList.SubList)o;
				return contentsEquals(other.getParentArray(), other.from, other.to);
			}
			return super.equals(o);
		}

		int contentsCompareTo(float[][] otherA, long otherAFrom, long otherATo) {
			if (a == otherA && from == otherAFrom && to == otherATo) return 0;
			// TODO When minimum version of Java becomes Java 9, use Arrays.compare, which vectorizes.
			float e1, e2;
			int r;
			long i, j;
			for (i = from, j = otherAFrom; i < to && i < otherATo; i++, j++) {
				e1 = BigArrays.get(a, i);
				e2 = BigArrays.get(otherA, j);
				if ((r = (Float.compare((e1), (e2)))) != 0) return r;
			}
			return i < otherATo ? -1 : (i < to ? 1 : 0);
		}

		@Override
		public int compareTo(final BigList<? extends Float> l) {
			if (l instanceof FloatBigArrayBigList) {

				FloatBigArrayBigList other = (FloatBigArrayBigList)l;
				return contentsCompareTo(other.a, 0, other.size64());
			}
			if (l instanceof FloatBigArrayBigList.SubList) {

				FloatBigArrayBigList.SubList other = (FloatBigArrayBigList.SubList)l;
				return contentsCompareTo(other.getParentArray(), other.from, other.to);
			}
			return super.compareTo(l);
		}
		// We don't override subList as we want AbstractList's "sub-sublist" nesting handling,
		// which would be tricky to do here.
		// TODO Do override it so array access isn't sent through N indirections.
		// This will likely mean making this class static.
	}

	@Override
	public FloatBigList subList(long from, long to) {
		if (from == 0 && to == size64()) return this;
		ensureIndex(from);
		ensureIndex(to);
		if (from > to) throw new IndexOutOfBoundsException("Start index (" + from + ") is greater than end index (" + to + ")");
		return new SubList(from, to);
	}

	/**
	 * Copies element of this type-specific list into the given big array using optimized system calls.
	 *
	 * @param from the start index (inclusive).
	 * @param a the destination big array.
	 * @param offset the offset into the destination array where to store the first element copied.
	 * @param length the number of elements to be copied.
	 */
	@Override
	public void getElements(final long from, final float[][] a, final long offset, final long length) {
		BigArrays.copy(this.a, from, a, offset, length);
	}

	/**
	 * Copies element of this type-specific list into the given array using optimized system calls.
	 *
	 * @param from the start index (inclusive).
	 * @param a the destination array.
	 * @param offset the offset into the destination array where to store the first element copied.
	 * @param length the number of elements to be copied.
	 */
	@Override
	public void getElements(final long from, final float[] a, final int offset, final int length) {
		BigArrays.copyFromBig(this.a, from, a, offset, length);
	}

	/**
	 * Removes elements of this type-specific list using optimized system calls.
	 *
	 * @param from the start index (inclusive).
	 * @param to the end index (exclusive).
	 */
	@Override
	public void removeElements(final long from, final long to) {
		BigArrays.ensureFromTo(size, from, to);
		BigArrays.copy(a, to, a, from, size - to);
		size -= (to - from);
	}

	/**
	 * Adds elements to this type-specific list using optimized system calls.
	 *
	 * @param index the index at which to add elements.
	 * @param a the big array containing the elements.
	 * @param offset the offset of the first element to add.
	 * @param length the number of elements to add.
	 */
	@Override
	public void addElements(final long index, final float a[][], final long offset, final long length) {
		ensureIndex(index);
		BigArrays.ensureOffsetLength(a, offset, length);
		grow(size + length);
		BigArrays.copy(this.a, index, this.a, index + length, size - index);
		BigArrays.copy(a, offset, this.a, index, length);
		size += length;
	}

	/**
	 * Copies elements in the given big array into this type-specific list using optimized system calls.
	 *
	 * @param index the start index (inclusive).
	 * @param a the destination big array.
	 * @param offset the offset into the destination array where to store the first element copied.
	 * @param length the number of elements to be copied.
	 */
	@Override
	public void setElements(final long index, final float[][] a, final long offset, final long length) {
		BigArrays.copy(a, offset, this.a, index, length);
	}

	@Override
	public void forEach(final FloatConsumer action) {
		for (long i = 0; i < size; ++i) {
			action.accept(BigArrays.get(a, i));
		}
	}

	@Override
	public FloatBigListIterator listIterator(final long index) {
		ensureIndex(index);
		return new FloatBigListIterator() {
			long pos = index, last = -1;

			@Override
			public boolean hasNext() {
				return pos < size;
			}

			@Override
			public boolean hasPrevious() {
				return pos > 0;
			}

			@Override
			public float nextFloat() {
				if (!hasNext()) throw new NoSuchElementException();
				return BigArrays.get(a, last = pos++);
			}

			@Override
			public float previousFloat() {
				if (!hasPrevious()) throw new NoSuchElementException();
				return BigArrays.get(a, last = --pos);
			}

			@Override
			public long nextIndex() {
				return pos;
			}

			@Override
			public long previousIndex() {
				return pos - 1;
			}

			@Override
			public void add(float k) {
				FloatBigArrayBigList.this.add(pos++, k);
				last = -1;
			}

			@Override
			public void set(float k) {
				if (last == -1) throw new IllegalStateException();
				FloatBigArrayBigList.this.set(last, k);
			}

			@Override
			public void remove() {
				if (last == -1) throw new IllegalStateException();
				FloatBigArrayBigList.this.removeFloat(last);
				/* If the last operation was a next(), we are removing an element *before* us, and we must decrease pos correspondingly. */
				if (last < pos) pos--;
				last = -1;
			}

			@Override
			public void forEachRemaining(final FloatConsumer action) {
				while (pos < size) {
					action.accept(BigArrays.get(a, last = pos++));
				}
			}

			@Override
			public long back(long n) {
				if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
				final long remaining = size - pos;
				if (n < remaining) {
					pos -= n;
				} else {
					n = remaining;
					pos = 0;
				}
				last = pos;
				return n;
			}

			@Override
			public long skip(long n) {
				if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
				final long remaining = size - pos;
				if (n < remaining) {
					pos += n;
				} else {
					n = remaining;
					pos = size;
				}
				last = pos - 1;
				return n;
			}
		};
	}

	private final class Spliterator implements FloatSpliterator {
		// Until we split, we will track the size of the list.
		// Once we split, then we stop updating on structural modifications.
		// Aka, size is late-binding.
		boolean hasSplit = false;
		long pos, max;

		public Spliterator() {
			this(0, FloatBigArrayBigList.this.size, false);
		}

		private Spliterator(long pos, long max, boolean hasSplit) {
			assert pos <= max : "pos " + pos + " must be <= max " + max;
			this.pos = pos;
			this.max = max;
			this.hasSplit = hasSplit;
		}

		private long getWorkingMax() {
			return hasSplit ? max : FloatBigArrayBigList.this.size;
		}

		@Override
		public int characteristics() {
			return FloatSpliterators.LIST_SPLITERATOR_CHARACTERISTICS;
		}

		@Override
		public long estimateSize() {
			return getWorkingMax() - pos;
		}

		@Override
		public boolean tryAdvance(final FloatConsumer action) {
			if (pos >= getWorkingMax()) return false;
			action.accept(BigArrays.get(a, pos++));
			return true;
		}

		@Override
		public void forEachRemaining(final FloatConsumer action) {
			for (final long max = getWorkingMax(); pos < max; ++pos) {
				action.accept(BigArrays.get(a, pos));
			}
		}

		@Override
		public long skip(long n) {
			if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
			final long max = getWorkingMax();
			if (pos >= max) return 0;
			final long remaining = max - pos;
			if (n < remaining) {
				pos += n;
				return n;
			}
			n = remaining;
			pos = max;
			return n;
		}

		@Override
		public FloatSpliterator trySplit() {
			final long max = getWorkingMax();
			long retLen = (max - pos) >> 1;
			if (retLen <= 1) return null;
			// Update instance max with the last seen list size (if needed) before continuing
			this.max = max;
			long myNewPos = pos + retLen;
			// Align to an outer array boundary if possible
			// We add/subtract one to the bounds to ensure the new pos will always shrink the range
			myNewPos = BigArrays.nearestSegmentStart(myNewPos, pos + 1, max - 1);
			long retMax = myNewPos;
			long oldPos = pos;
			this.pos = myNewPos;
			this.hasSplit = true;
			return new Spliterator(oldPos, retMax, true);
		}
	}

	@Override
	public FloatSpliterator spliterator() {
		return new Spliterator();
	}

	@Override
	public FloatBigArrayBigList clone() {
		FloatBigArrayBigList c;
		// Test for fastpath we can do if exactly an BigArrayBigList
		if (getClass() == FloatBigArrayBigList.class) {
			c = new FloatBigArrayBigList(size);
			c.size = size;
		} else {
			try {
				c = (FloatBigArrayBigList)super.clone();
			} catch (CloneNotSupportedException e) {
				// Can't happen
				throw new InternalError(e);
			}
			c.a = FloatBigArrays.newBigArray(size);
		}
		BigArrays.copy(a, 0, c.a, 0, size);
		return c;
	}

	/**
	 * Compares this type-specific big-array list to another one.
	 *
	 * <p>
	 * This method exists only for sake of efficiency. The implementation inherited from the abstract
	 * implementation would already work.
	 *
	 * @param l a type-specific big-array list.
	 * @return true if the argument contains the same elements of this type-specific big-array list.
	 */
	public boolean equals(final FloatBigArrayBigList l) {
		if (l == this) return true;
		long s = size64();
		if (s != l.size64()) return false;
		final float[][] a1 = a;
		final float[][] a2 = l.a;
		// Already checked s == l.size64 above
		if (a1 == a2) return true;
		// Backwards loop is faster then forwards loop, at least in Java 8 and below.
		while (s-- != 0) if (BigArrays.get(a1, s) != BigArrays.get(a2, s)) return false;
		return true;
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (o == null) return false;
		if (!(o instanceof BigList)) return false;
		if (o instanceof FloatBigArrayBigList) {
			// Safe cast because we are only going to take elements from other list, never give them
			return equals((FloatBigArrayBigList)o);
		}
		if (o instanceof FloatBigArrayBigList.SubList) {
			// Safe cast because we are only going to take elements from other list, never give them
			// Sublist has an optimized sub-array based comparison, reuse that.
			return ((FloatBigArrayBigList.SubList)o).equals(this);
		}
		return super.equals(o);
	}

	/**
	 * Compares this big list to another big list.
	 *
	 * <p>
	 * This method exists only for sake of efficiency. The implementation inherited from the abstract
	 * implementation would already work.
	 *
	 * @param l a big list.
	 * @return a negative integer, zero, or a positive integer as this big list is lexicographically
	 *         less than, equal to, or greater than the argument.
	 */

	public int compareTo(final FloatBigArrayBigList l) {
		final long s1 = size64(), s2 = l.size64();
		final float a1[][] = a, a2[][] = l.a;
		if (a1 == a2 && s1 == s2) return 0;
		float e1, e2;
		int r, i;
		for (i = 0; i < s1 && i < s2; i++) {
			e1 = BigArrays.get(a1, i);
			e2 = BigArrays.get(a2, i);
			if ((r = (Float.compare((e1), (e2)))) != 0) return r;
		}
		return i < s2 ? -1 : (i < s1 ? 1 : 0);
	}

	@Override
	public int compareTo(final BigList<? extends Float> l) {
		if (l instanceof FloatBigArrayBigList) {
			return compareTo((FloatBigArrayBigList)l);
		}
		if (l instanceof FloatBigArrayBigList.SubList) {
			// Must negate because we are inverting the order of the comparison.
			return -((FloatBigArrayBigList.SubList)l).compareTo(this);
		}
		return super.compareTo(l);
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		s.defaultWriteObject();
		for (int i = 0; i < size; i++) s.writeFloat(BigArrays.get(a, i));
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		a = FloatBigArrays.newBigArray(size);
		for (int i = 0; i < size; i++) BigArrays.set(a, i, s.readFloat());
	}
}
