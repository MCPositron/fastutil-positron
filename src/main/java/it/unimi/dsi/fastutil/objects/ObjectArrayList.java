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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;
import java.util.NoSuchElementException;
import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.stream.Collector;
import java.util.function.Consumer;

/**
 * A type-specific array-based list; provides some additional methods that use polymorphism to avoid
 * (un)boxing.
 *
 * <p>
 * This class implements a lightweight, fast, open, optimized, reuse-oriented version of array-based
 * lists. Instances of this class represent a list with an array that is enlarged as needed when new
 * entries are created (by increasing its current length by 50%), but is <em>never</em> made smaller
 * (even on a {@link #clear()}). A family of {@linkplain #trim() trimming methods} lets you control
 * the size of the backing array; this is particularly useful if you reuse instances of this class.
 * Range checks are equivalent to those of {@link java.util}'s classes, but they are delayed as much
 * as possible.
 *
 * <p>
 * The backing array is exposed by the {@link #elements()} method. If an instance of this class was
 * created {@linkplain #wrap(Object[],int) by wrapping}, backing-array reallocations will be
 * performed using reflection, so that {@link #elements()} can return an array of the same type of
 * the original array: the comments about efficiency made in
 * {@link it.unimi.dsi.fastutil.objects.ObjectArrays} apply here. Moreover, you must take into
 * consideration that assignment to an array not of type {@code Object[]} is slower due to type
 * checking.
 *
 * <p>
 * This class implements the bulk methods {@code removeElements()}, {@code addElements()} and
 * {@code getElements()} using high-performance system calls (e.g.,
 * {@link System#arraycopy(Object,int,Object,int,int) System.arraycopy()}) instead of expensive
 * loops.
 *
 * @see java.util.ArrayList
 */
public class ObjectArrayList<K> extends AbstractObjectList<K> implements RandomAccess, Cloneable, java.io.Serializable {
	private static final long serialVersionUID = -7046029254386353131L;
	/** The initial default capacity of an array list. */
	public static final int DEFAULT_INITIAL_CAPACITY = 10;
	/**
	 * Whether the backing array was passed to {@code wrap()}. In this case, we must reallocate with the
	 * same type of array.
	 */
	protected final boolean wrapped;
	/** The backing array. */
	protected transient K a[];
	/** The current actual size of the list (never greater than the backing-array length). */
	protected int size;

	/**
	 * Ensures that the component type of the given array is the proper type. This is irrelevant for
	 * primitive types, so it will just do a trivial copy. But for Reference types, you can have a
	 * {@code String[]} masquerading as an {@code Object[]}, which is a case we need to prepare for
	 * because we let the user give an array to use directly with {@link #wrap}.
	 */
	@SuppressWarnings("unchecked")
	private static final <K> K[] copyArraySafe(K[] a, int length) {
		if (length == 0) return (K[])ObjectArrays.EMPTY_ARRAY;
		return (K[])java.util.Arrays.copyOf(a, length, Object[].class);
	}

	private static final <K> K[] copyArrayFromSafe(ObjectArrayList<K> l) {
		return copyArraySafe(l.a, l.size);
	}

	/**
	 * Creates a new array list using a given array.
	 *
	 * <p>
	 * This constructor is only meant to be used by the wrapping methods.
	 *
	 * @param a the array that will be used to back this array list.
	 */
	protected ObjectArrayList(final K a[], @SuppressWarnings("unused") boolean wrapped) {
		this.a = a;
		this.wrapped = wrapped;
	}

	@SuppressWarnings("unchecked")
	private void initArrayFromCapacity(final int capacity) {
		if (capacity < 0) throw new IllegalArgumentException("Initial capacity (" + capacity + ") is negative");
		if (capacity == 0) a = (K[])ObjectArrays.EMPTY_ARRAY;
		else a = (K[])new Object[capacity];
	}

	/**
	 * Creates a new array list with given capacity.
	 *
	 * @param capacity the initial capacity of the array list (may be 0).
	 */
	public ObjectArrayList(final int capacity) {
		initArrayFromCapacity(capacity);
		this.wrapped = false;
	}

	/** Creates a new array list with {@link #DEFAULT_INITIAL_CAPACITY} capacity. */
	@SuppressWarnings("unchecked")
	public ObjectArrayList() {
		a = (K[])ObjectArrays.DEFAULT_EMPTY_ARRAY; // We delay allocation
		wrapped = false;
	}

	/**
	 * Creates a new array list and fills it with a given collection.
	 *
	 * @param c a collection that will be used to fill the array list.
	 */
	public ObjectArrayList(final Collection<? extends K> c) {
		if (c instanceof ObjectArrayList) {
			a = copyArrayFromSafe((ObjectArrayList<? extends K>)c);
			size = a.length;
		} else {
			initArrayFromCapacity(c.size());
			if (c instanceof ObjectList) {
				((ObjectList<? extends K>)c).getElements(0, a, 0, size = c.size());
			} else {
				size = ObjectIterators.unwrap(c.iterator(), a);
			}
		}
		this.wrapped = false;
	}

	/**
	 * Creates a new array list and fills it with a given type-specific collection.
	 *
	 * @param c a type-specific collection that will be used to fill the array list.
	 */
	public ObjectArrayList(final ObjectCollection<? extends K> c) {
		if (c instanceof ObjectArrayList) {
			a = copyArrayFromSafe((ObjectArrayList<? extends K>)c);
			size = a.length;
		} else {
			initArrayFromCapacity(c.size());
			if (c instanceof ObjectList) {
				((ObjectList<? extends K>)c).getElements(0, a, 0, size = c.size());
			} else {
				size = ObjectIterators.unwrap(c.iterator(), a);
			}
		}
		this.wrapped = false;
	}

	/**
	 * Creates a new array list and fills it with a given type-specific list.
	 *
	 * @param l a type-specific list that will be used to fill the array list.
	 */
	public ObjectArrayList(final ObjectList<? extends K> l) {
		if (l instanceof ObjectArrayList) {
			a = copyArrayFromSafe((ObjectArrayList<? extends K>)l);
			size = a.length;
		} else {
			initArrayFromCapacity(l.size());
			l.getElements(0, a, 0, size = l.size());
		}
		this.wrapped = false;
	}

	/**
	 * Creates a new array list and fills it with the elements of a given array.
	 *
	 * @param a an array whose elements will be used to fill the array list.
	 */
	public ObjectArrayList(final K a[]) {
		this(a, 0, a.length);
	}

	/**
	 * Creates a new array list and fills it with the elements of a given array.
	 *
	 * @param a an array whose elements will be used to fill the array list.
	 * @param offset the first element to use.
	 * @param length the number of elements to use.
	 */
	public ObjectArrayList(final K a[], final int offset, final int length) {
		this(length);
		System.arraycopy(a, offset, this.a, 0, length);
		size = length;
	}

	/**
	 * Creates a new array list and fills it with the elements returned by an iterator..
	 *
	 * @param i an iterator whose returned elements will fill the array list.
	 */
	public ObjectArrayList(final Iterator<? extends K> i) {
		this();
		while (i.hasNext()) this.add((i.next()));
	}

	/**
	 * Creates a new array list and fills it with the elements returned by a type-specific iterator..
	 *
	 * @param i a type-specific iterator whose returned elements will fill the array list.
	 */
	public ObjectArrayList(final ObjectIterator<? extends K> i) {
		this();
		while (i.hasNext()) this.add(i.next());
	}

	/**
	 * Returns the backing array of this list.
	 *
	 * <p>
	 * If this array list was created by wrapping a given array, it is guaranteed that the type of the
	 * returned array will be the same. Otherwise, the returned array will be of type {@link Object
	 * Object[]} (in spite of the declared return type).
	 *
	 * <p>
	 * <strong>Warning</strong>: This behaviour may cause (unfathomable) run-time errors if a method
	 * expects an array actually of type {@code K[]}, but this methods returns an array of type
	 * {@link Object Object[]}.
	 *
	 * @return the backing array.
	 */
	public K[] elements() {
		return a;
	}

	/**
	 * Wraps a given array into an array list of given size.
	 *
	 * <p>
	 * Note it is guaranteed that the type of the array returned by {@link #elements()} will be the same
	 * (see the comments in the class documentation).
	 *
	 * @param a an array to wrap.
	 * @param length the length of the resulting array list.
	 * @return a new array list of the given size, wrapping the given array.
	 */
	public static <K> ObjectArrayList<K> wrap(final K a[], final int length) {
		if (length > a.length) throw new IllegalArgumentException("The specified length (" + length + ") is greater than the array size (" + a.length + ")");
		final ObjectArrayList<K> l = new ObjectArrayList<>(a, true);
		l.size = length;
		return l;
	}

	/**
	 * Wraps a given array into an array list.
	 *
	 * <p>
	 * Note it is guaranteed that the type of the array returned by {@link #elements()} will be the same
	 * (see the comments in the class documentation).
	 *
	 * @param a an array to wrap.
	 * @return a new array list wrapping the given array.
	 */
	public static <K> ObjectArrayList<K> wrap(final K a[]) {
		return wrap(a, a.length);
	}

	/**
	 * Creates a new empty array list.
	 *
	 * @return a new empty array list.
	 */
	public static <K> ObjectArrayList<K> of() {
		return new ObjectArrayList<>();
	}

	/**
	 * Creates an array list using an array of elements.
	 *
	 * @param init a the array the will become the new backing array of the array list.
	 * @return a new array list backed by the given array.
	 * @see #wrap
	 */
	@SafeVarargs
	public static <K> ObjectArrayList<K> of(final K... init) {
		return wrap(init);
	}

	// Collector wants a function that returns the collection being added to.
	ObjectArrayList<K> combine(ObjectArrayList<? extends K> toAddFrom) {
		addAll(toAddFrom);
		return this;
	}

	private static final Collector<Object, ?, ObjectArrayList<Object>> TO_LIST_COLLECTOR = Collector.of(ObjectArrayList::new, ObjectArrayList::add, ObjectArrayList::combine);

	/** Returns a {@link Collector} that collects a {@code Stream}'s elements into a new ArrayList. */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <K> Collector<K, ?, ObjectArrayList<K>> toList() {
		return (Collector)TO_LIST_COLLECTOR;
	}

	/**
	 * Returns a {@link Collector} that collects a {@code Stream}'s elements into a new ArrayList,
	 * potentially pre-allocated to handle the given size.
	 */
	public static <K> Collector<K, ?, ObjectArrayList<K>> toListWithExpectedSize(int expectedSize) {
		if (expectedSize <= DEFAULT_INITIAL_CAPACITY) {
			// Already below default capacity. Just use all default construction instead of fiddling with
			// atomics in SizeDecreasingSupplier
			return toList();
		}
		return Collector.of(new ObjectCollections.SizeDecreasingSupplier<K, ObjectArrayList<K>>(expectedSize, (int size) -> size <= DEFAULT_INITIAL_CAPACITY ? new ObjectArrayList<K>() : new ObjectArrayList<K>(size)), ObjectArrayList::add, ObjectArrayList::combine);
	}

	/**
	 * Ensures that this array list can contain the given number of entries without resizing.
	 *
	 * @param capacity the new minimum capacity for this array list.
	 */
	@SuppressWarnings("unchecked")
	public void ensureCapacity(final int capacity) {
		if (capacity <= a.length || (a == ObjectArrays.DEFAULT_EMPTY_ARRAY && capacity <= DEFAULT_INITIAL_CAPACITY)) return;
		if (wrapped) a = ObjectArrays.ensureCapacity(a, capacity, size);
		else {
			if (capacity > a.length) {
				final Object t[] = new Object[capacity];
				System.arraycopy(a, 0, t, 0, size);
				a = (K[])t;
			}
		}
		assert size <= a.length;
	}

	/**
	 * Grows this array list, ensuring that it can contain the given number of entries without resizing,
	 * and in case increasing the current capacity at least by a factor of 50%.
	 *
	 * @param capacity the new minimum capacity for this array list.
	 */
	@SuppressWarnings("unchecked")
	private void grow(int capacity) {
		if (capacity <= a.length) return;
		if (a != ObjectArrays.DEFAULT_EMPTY_ARRAY) capacity = (int)Math.max(Math.min((long)a.length + (a.length >> 1), it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE), capacity);
		else if (capacity < DEFAULT_INITIAL_CAPACITY) capacity = DEFAULT_INITIAL_CAPACITY;
		if (wrapped) a = ObjectArrays.forceCapacity(a, capacity, size);
		else {
			final Object t[] = new Object[capacity];
			System.arraycopy(a, 0, t, 0, size);
			a = (K[])t;
		}
		assert size <= a.length;
	}

	@Override
	public void add(final int index, final K k) {
		ensureIndex(index);
		grow(size + 1);
		if (index != size) System.arraycopy(a, index, a, index + 1, size - index);
		a[index] = k;
		size++;
		assert size <= a.length;
	}

	@Override
	public boolean add(final K k) {
		grow(size + 1);
		a[size++] = k;
		assert size <= a.length;
		return true;
	}

	@Override
	public K get(final int index) {
		if (index >= size) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
		return a[index];
	}

	@Override
	public int indexOf(final Object k) {
		for (int i = 0; i < size; i++) if (java.util.Objects.equals(k, a[i])) return i;
		return -1;
	}

	@Override
	public int lastIndexOf(final Object k) {
		for (int i = size; i-- != 0;) if (java.util.Objects.equals(k, a[i])) return i;
		return -1;
	}

	@Override
	public K remove(final int index) {
		if (index >= size) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
		final K old = a[index];
		size--;
		if (index != size) System.arraycopy(a, index + 1, a, index, size - index);
		a[size] = null;
		assert size <= a.length;
		return old;
	}

	@Override
	public boolean remove(final Object k) {
		int index = indexOf(k);
		if (index == -1) return false;
		remove(index);
		assert size <= a.length;
		return true;
	}

	@Override
	public K set(final int index, final K k) {
		if (index >= size) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
		K old = a[index];
		a[index] = k;
		return old;
	}

	@Override
	public void clear() {
		Arrays.fill(a, 0, size, null);
		size = 0;
		assert size <= a.length;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void size(final int size) {
		if (size > a.length) a = ObjectArrays.forceCapacity(a, size, this.size);
		if (size > this.size) Arrays.fill(a, this.size, size, (null));
		else Arrays.fill(a, size, this.size, (null));
		this.size = size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Trims this array list so that the capacity is equal to the size.
	 *
	 * @see java.util.ArrayList#trimToSize()
	 */
	public void trim() {
		trim(0);
	}

	/**
	 * Trims the backing array if it is too large.
	 *
	 * If the current array length is smaller than or equal to {@code n}, this method does nothing.
	 * Otherwise, it trims the array length to the maximum between {@code n} and {@link #size()}.
	 *
	 * <p>
	 * This method is useful when reusing lists. {@linkplain #clear() Clearing a list} leaves the array
	 * length untouched. If you are reusing a list many times, you can call this method with a typical
	 * size to avoid keeping around a very large array just because of a few large transient lists.
	 *
	 * @param n the threshold for the trimming.
	 */
	@SuppressWarnings("unchecked")
	public void trim(final int n) {
		// TODO: use Arrays.trim() and preserve type only if necessary
		if (n >= a.length || size == a.length) return;
		final K t[] = (K[])new Object[Math.max(n, size)];
		System.arraycopy(a, 0, t, 0, size);
		a = t;
		assert size <= a.length;
	}

	private class SubList extends AbstractObjectList.ObjectRandomAccessSubList<K> {
		private static final long serialVersionUID = -3185226345314976296L;

		protected SubList(int from, int to) {
			super(ObjectArrayList.this, from, to);
		}

		// Most of the inherited methods should be fine, but we can override a few of them for performance.
		// Needed because we can't access the parent class' instance variables directly in a different
		// instance of SubList.
		private K[] getParentArray() {
			return a;
		}

		@Override
		public K get(int i) {
			ensureRestrictedIndex(i);
			return a[i + from];
		}

		private final class SubListIterator extends ObjectIterators.AbstractIndexBasedListIterator<K> {
			// We are using pos == 0 to be 0 relative to SubList.from (meaning you need to do a[from + i] when
			// accessing array).
			SubListIterator(int index) {
				super(0, index);
			}

			@Override
			protected final K get(int i) {
				return a[from + i];
			}

			@Override
			protected final void add(int i, K k) {
				SubList.this.add(i, k);
			}

			@Override
			protected final void set(int i, K k) {
				SubList.this.set(i, k);
			}

			@Override
			protected final void remove(int i) {
				SubList.this.remove(i);
			}

			@Override
			protected final int getMaxPos() {
				return to - from;
			}

			@Override
			public K next() {
				if (!hasNext()) throw new NoSuchElementException();
				return a[from + (lastReturned = pos++)];
			}

			@Override
			public K previous() {
				if (!hasPrevious()) throw new NoSuchElementException();
				return a[from + (lastReturned = --pos)];
			}

			@Override
			public void forEachRemaining(final Consumer<? super K> action) {
				final int max = to - from;
				while (pos < max) {
					action.accept(a[from + (lastReturned = pos++)]);
				}
			}
		}

		@Override
		public ObjectListIterator<K> listIterator(int index) {
			return new SubListIterator(index);
		}

		private final class SubListSpliterator extends ObjectSpliterators.LateBindingSizeIndexBasedSpliterator<K> {
			// We are using pos == 0 to be 0 relative to real array 0
			SubListSpliterator() {
				super(from);
			}

			private SubListSpliterator(int pos, int maxPos) {
				super(pos, maxPos);
			}

			@Override
			protected final int getMaxPosFromBackingStore() {
				return to;
			}

			@Override
			protected final K get(int i) {
				return a[i];
			}

			@Override
			protected final SubListSpliterator makeForSplit(int pos, int maxPos) {
				return new SubListSpliterator(pos, maxPos);
			}

			@Override
			public boolean tryAdvance(final Consumer<? super K> action) {
				if (pos >= getMaxPos()) return false;
				action.accept(a[pos++]);
				return true;
			}

			@Override
			public void forEachRemaining(final Consumer<? super K> action) {
				final int max = getMaxPos();
				while (pos < max) {
					action.accept(a[pos++]);
				}
			}
		}

		@Override
		public ObjectSpliterator<K> spliterator() {
			return new SubListSpliterator();
		}

		boolean contentsEquals(K[] otherA, int otherAFrom, int otherATo) {
			if (a == otherA && from == otherAFrom && to == otherATo) return true;
			if (otherATo - otherAFrom != size()) {
				return false;
			}
			int pos = from, otherPos = otherAFrom;
			// We have already assured that the two ranges are the same size, so we only need to check one
			// bound.
			// TODO When minimum version of Java becomes Java 9, use the Arrays.equals which takes bounds, which
			// is vectorized.
			// Make sure to split out the reference equality case when you do this.
			while (pos < to) if (!java.util.Objects.equals(a[pos++], otherA[otherPos++])) return false;
			return true;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (o == null) return false;
			if (!(o instanceof java.util.List)) return false;
			if (o instanceof ObjectArrayList) {
				@SuppressWarnings("unchecked")
				ObjectArrayList<K> other = (ObjectArrayList<K>)o;
				return contentsEquals(other.a, 0, other.size());
			}
			if (o instanceof ObjectArrayList.SubList) {
				@SuppressWarnings("unchecked")
				ObjectArrayList<K>.SubList other = (ObjectArrayList<K>.SubList)o;
				return contentsEquals(other.getParentArray(), other.from, other.to);
			}
			return super.equals(o);
		}

		@SuppressWarnings("unchecked")
		int contentsCompareTo(K[] otherA, int otherAFrom, int otherATo) {
			// TODO When minimum version of Java becomes Java 9, use Arrays.compare, which vectorizes.
			K e1, e2;
			int r, i, j;
			for (i = from, j = otherAFrom; i < to && i < otherATo; i++, j++) {
				e1 = a[i];
				e2 = otherA[j];
				if ((r = (((Comparable<K>)(e1)).compareTo(e2))) != 0) return r;
			}
			return i < otherATo ? -1 : (i < to ? 1 : 0);
		}

		@SuppressWarnings("unchecked")
		@Override
		public int compareTo(final java.util.List<? extends K> l) {
			if (l instanceof ObjectArrayList) {
				@SuppressWarnings("unchecked")
				ObjectArrayList<K> other = (ObjectArrayList<K>)l;
				return contentsCompareTo(other.a, 0, other.size());
			}
			if (l instanceof ObjectArrayList.SubList) {
				@SuppressWarnings("unchecked")
				ObjectArrayList<K>.SubList other = (ObjectArrayList<K>.SubList)l;
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
	public ObjectList<K> subList(int from, int to) {
		if (from == 0 && to == size()) return this;
		ensureIndex(from);
		ensureIndex(to);
		if (from > to) throw new IndexOutOfBoundsException("Start index (" + from + ") is greater than end index (" + to + ")");
		return new SubList(from, to);
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
	public void getElements(final int from, final Object[] a, final int offset, final int length) {
		ObjectArrays.ensureOffsetLength(a, offset, length);
		System.arraycopy(this.a, from, a, offset, length);
	}

	/**
	 * Removes elements of this type-specific list using optimized system calls.
	 *
	 * @param from the start index (inclusive).
	 * @param to the end index (exclusive).
	 */
	@Override
	public void removeElements(final int from, final int to) {
		it.unimi.dsi.fastutil.Arrays.ensureFromTo(size, from, to);
		System.arraycopy(a, to, a, from, size - to);
		size -= (to - from);
		int i = to - from;
		while (i-- != 0) a[size + i] = null;
	}

	/**
	 * Adds elements to this type-specific list using optimized system calls.
	 *
	 * @param index the index at which to add elements.
	 * @param a the array containing the elements.
	 * @param offset the offset of the first element to add.
	 * @param length the number of elements to add.
	 */
	@Override
	public void addElements(final int index, final K a[], final int offset, final int length) {
		ensureIndex(index);
		ObjectArrays.ensureOffsetLength(a, offset, length);
		grow(size + length);
		System.arraycopy(this.a, index, this.a, index + length, size - index);
		System.arraycopy(a, offset, this.a, index, length);
		size += length;
	}

	/**
	 * Sets elements to this type-specific list using optimized system calls.
	 *
	 * @param index the index at which to start setting elements.
	 * @param a the array containing the elements.
	 * @param offset the offset of the first element to add.
	 * @param length the number of elements to add.
	 */
	@Override
	public void setElements(final int index, final K a[], final int offset, final int length) {
		ensureIndex(index);
		ObjectArrays.ensureOffsetLength(a, offset, length);
		if (index + length > size) throw new IndexOutOfBoundsException("End index (" + (index + length) + ") is greater than list size (" + size + ")");
		System.arraycopy(a, offset, this.a, index, length);
	}

	@Override
	public void forEach(final Consumer<? super K> action) {
		for (int i = 0; i < size; ++i) {
			action.accept(a[i]);
		}
	}

	@Override
	public boolean addAll(int index, final Collection<? extends K> c) {
		if (c instanceof ObjectList) {
			return addAll(index, (ObjectList<? extends K>)c);
		}
		ensureIndex(index);
		int n = c.size();
		if (n == 0) return false;
		grow(size + n);
		System.arraycopy(a, index, a, index + n, size - index);
		final Iterator<? extends K> i = c.iterator();
		size += n;
		while (n-- != 0) a[index++] = i.next();
		assert size <= a.length;
		return true;
	}

	@Override
	public boolean addAll(final int index, final ObjectList<? extends K> l) {
		ensureIndex(index);
		final int n = l.size();
		if (n == 0) return false;
		grow(size + n);
		System.arraycopy(a, index, a, index + n, size - index);
		l.getElements(0, a, index, n);
		size += n;
		assert size <= a.length;
		return true;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		final Object[] a = this.a;
		int j = 0;
		for (int i = 0; i < size; i++) if (!c.contains(a[i])) a[j++] = a[i];
		Arrays.fill(a, j, size, null);
		final boolean modified = size != j;
		size = j;
		return modified;
	}

	@Override
	public Object[] toArray() {
		// A subtle part of the spec says the returned array must be Object[] exactly.
		return Arrays.copyOf(a, size(), Object[].class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K> K[] toArray(K a[]) {
		if (a == null) {
			a = (K[])new Object[size()];
		} else if (a.length < size()) {
			a = (K[])Array.newInstance(a.getClass().getComponentType(), size());
		}
		System.arraycopy(this.a, 0, a, 0, size());
		if (a.length > size()) {
			a[size()] = null;
		}
		return a;
	}

	@Override
	public ObjectListIterator<K> listIterator(final int index) {
		ensureIndex(index);
		return new ObjectListIterator<K>() {
			int pos = index, last = -1;

			@Override
			public boolean hasNext() {
				return pos < size;
			}

			@Override
			public boolean hasPrevious() {
				return pos > 0;
			}

			@Override
			public K next() {
				if (!hasNext()) throw new NoSuchElementException();
				return a[last = pos++];
			}

			@Override
			public K previous() {
				if (!hasPrevious()) throw new NoSuchElementException();
				return a[last = --pos];
			}

			@Override
			public int nextIndex() {
				return pos;
			}

			@Override
			public int previousIndex() {
				return pos - 1;
			}

			@Override
			public void add(K k) {
				ObjectArrayList.this.add(pos++, k);
				last = -1;
			}

			@Override
			public void set(K k) {
				if (last == -1) throw new IllegalStateException();
				ObjectArrayList.this.set(last, k);
			}

			@Override
			public void remove() {
				if (last == -1) throw new IllegalStateException();
				ObjectArrayList.this.remove(last);
				/* If the last operation was a next(), we are removing an element *before* us, and we must decrease pos correspondingly. */
				if (last < pos) pos--;
				last = -1;
			}

			@Override
			public void forEachRemaining(final Consumer<? super K> action) {
				while (pos < size) {
					action.accept(a[last = pos++]);
				}
			}

			@Override
			public int back(int n) {
				if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
				final int remaining = size - pos;
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
			public int skip(int n) {
				if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
				final int remaining = size - pos;
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

	// If you update this, you will probably want to update ArraySet as well
	private final class Spliterator implements ObjectSpliterator<K> {
		// Until we split, we will track the size of the list.
		// Once we split, then we stop updating on structural modifications.
		// Aka, size is late-binding.
		boolean hasSplit = false;
		int pos, max;

		public Spliterator() {
			this(0, ObjectArrayList.this.size, false);
		}

		private Spliterator(int pos, int max, boolean hasSplit) {
			assert pos <= max : "pos " + pos + " must be <= max " + max;
			this.pos = pos;
			this.max = max;
			this.hasSplit = hasSplit;
		}

		private int getWorkingMax() {
			return hasSplit ? max : ObjectArrayList.this.size;
		}

		@Override
		public int characteristics() {
			return ObjectSpliterators.LIST_SPLITERATOR_CHARACTERISTICS;
		}

		@Override
		public long estimateSize() {
			return getWorkingMax() - pos;
		}

		@Override
		public boolean tryAdvance(final Consumer<? super K> action) {
			if (pos >= getWorkingMax()) return false;
			action.accept(a[pos++]);
			return true;
		}

		@Override
		public void forEachRemaining(final Consumer<? super K> action) {
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
		public ObjectSpliterator<K> trySplit() {
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
	 * The returned spliterator is late-binding; it will track structural changes after the current
	 * index, up until the first {@link java.util.Spliterator#trySplit() trySplit()}, at which point the
	 * maximum index will be fixed. <br>
	 * Structural changes before the current index or after the first
	 * {@link java.util.Spliterator#trySplit() trySplit()} will result in unspecified behavior.
	 */
	@Override
	public ObjectSpliterator<K> spliterator() {
		// If it wasn't for the possibility of the list being expanded or shrunk,
		// we could return SPLITERATORS.wrap(a, 0, size).
		return new Spliterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void sort(final Comparator<? super K> comp) {
		if (comp == null) {
			ObjectArrays.stableSort(a, 0, size);
		} else {
			ObjectArrays.stableSort(a, 0, size, comp);
		}
	}

	@Override
	public void unstableSort(final Comparator<? super K> comp) {
		if (comp == null) {
			ObjectArrays.unstableSort(a, 0, size);
		} else {
			ObjectArrays.unstableSort(a, 0, size, comp);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ObjectArrayList<K> clone() {
		ObjectArrayList<K> cloned = null;
		// Test for fastpath we can do if exactly an ArrayList
		if (getClass() == ObjectArrayList.class) {
			// Preserve backwards compatibility and make new list have Object[] even if it was wrapped from some
			// subclass.
			cloned = new ObjectArrayList<>(copyArraySafe(a, size), false);
			cloned.size = size;
		} else {
			try {
				cloned = (ObjectArrayList<K>)super.clone();
			} catch (CloneNotSupportedException err) {
				// Can't happen
				throw new InternalError(err);
			}
			// Preserve backwards compatibility and make new list have Object[] even if it was wrapped from some
			// subclass.
			cloned.a = copyArraySafe(a, size);
			// We can't clear cloned.wrapped because it is final.
		}
		return cloned;
	}

	/**
	 * Compares this type-specific array list to another one.
	 *
	 * @apiNote This method exists only for sake of efficiency. The implementation inherited from the
	 *          abstract implementation would already work.
	 *
	 * @param l a type-specific array list.
	 * @return true if the argument contains the same elements of this type-specific array list.
	 */
	public boolean equals(final ObjectArrayList<K> l) {
		// TODO When minimum version of Java becomes Java 9, use the Arrays.equals which takes bounds, which
		// is vectorized.
		if (l == this) return true;
		int s = size();
		if (s != l.size()) return false;
		final K[] a1 = a;
		final K[] a2 = l.a;
		if (a1 == a2 && s == l.size()) return true;
		while (s-- != 0) if (!java.util.Objects.equals(a1[s], a2[s])) return false;
		return true;
	}

	@SuppressWarnings({ "unchecked", "unlikely-arg-type" })
	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (o == null) return false;
		if (!(o instanceof java.util.List)) return false;
		if (o instanceof ObjectArrayList) {
			// Safe cast because we are only going to take elements from other list, never give them
			return equals((ObjectArrayList<K>)o);
		}
		if (o instanceof ObjectArrayList.SubList) {
			// Safe cast because we are only going to take elements from other list, never give them
			// Sublist has an optimized sub-array based comparison, reuse that.
			return ((ObjectArrayList<K>.SubList)o).equals(this);
		}
		return super.equals(o);
	}

	/**
	 * Compares this array list to another array list.
	 *
	 * @apiNote This method exists only for sake of efficiency. The implementation inherited from the
	 *          abstract implementation would already work.
	 *
	 * @param l an array list.
	 * @return a negative integer, zero, or a positive integer as this list is lexicographically less
	 *         than, equal to, or greater than the argument.
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(final ObjectArrayList<? extends K> l) {
		final int s1 = size(), s2 = l.size();
		final K a1[] = a, a2[] = l.a;
		// TODO When minimum version of Java becomes Java 9, use Arrays.compare, which vectorizes.
		K e1, e2;
		int r, i;
		for (i = 0; i < s1 && i < s2; i++) {
			e1 = a1[i];
			e2 = a2[i];
			if ((r = (((Comparable<K>)(e1)).compareTo(e2))) != 0) return r;
		}
		return i < s2 ? -1 : (i < s1 ? 1 : 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(final java.util.List<? extends K> l) {
		if (l instanceof ObjectArrayList) {
			return compareTo((ObjectArrayList<? extends K>)l);
		}
		if (l instanceof ObjectArrayList.SubList) {
			// Must negate because we are inverting the order of the comparison.
			return -((ObjectArrayList<K>.SubList)l).compareTo(this);
		}
		return super.compareTo(l);
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		s.defaultWriteObject();
		for (int i = 0; i < size; i++) s.writeObject(a[i]);
	}

	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		a = (K[])new Object[size];
		for (int i = 0; i < size; i++) a[i] = (K)s.readObject();
	}
}
