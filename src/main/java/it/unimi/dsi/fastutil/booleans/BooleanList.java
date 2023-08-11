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
package it.unimi.dsi.fastutil.booleans;

import java.util.Spliterator;
import java.util.List;
import static it.unimi.dsi.fastutil.Size64.sizeOf;

/**
 * A type-specific {@link List}; provides some additional methods that use polymorphism to avoid
 * (un)boxing.
 *
 * <p>
 * Note that this type-specific interface extends {@link Comparable}: it is expected that
 * implementing classes perform a lexicographical comparison using the standard operator "less then"
 * for primitive types, and the usual {@link Comparable#compareTo(Object) compareTo()} method for
 * objects.
 *
 * <p>
 * Additionally, this interface strengthens {@link #iterator()}, {@link #listIterator()},
 * {@link #listIterator(int)} and {@link #subList(int,int)}. The former had been already
 * strengthened upstream, but unfortunately {@link List} re-specifies it.
 *
 * <p>
 * Besides polymorphic methods, this interfaces specifies methods to copy into an array or remove
 * contiguous sublists. Although the abstract implementation of this interface provides simple,
 * one-by-one implementations of these methods, it is expected that concrete implementation override
 * them with optimized versions.
 *
 * @see List
 */
public interface BooleanList extends List<Boolean>, Comparable<List<? extends Boolean>>, BooleanCollection {
	/**
	 * Returns a type-specific iterator on the elements of this list.
	 *
	 * @apiNote This specification strengthens the one given in {@link List#iterator()}. It would not be
	 *          normally necessary, but {@link java.lang.Iterable#iterator()} is bizarrily re-specified
	 *          in {@link List}.
	 *          <p>
	 *          Also, this is generally the only {@code iterator} method subclasses should override.
	 *
	 * @return an iterator on the elements of this list.
	 */
	@Override
	BooleanListIterator iterator();

	/**
	 * Returns a type-specific spliterator on the elements of this list.
	 *
	 * <p>
	 * List spliterators must report at least {@link Spliterator#SIZED} and {@link Spliterator#ORDERED}.
	 *
	 * <p>
	 * See {@link java.util.List#spliterator()} for more documentation on the requirements of the
	 * returned spliterator.
	 *
	 * @apiNote This specification strengthens the one given in
	 *          {@link java.util.Collection#spliterator()}, which was already strengthened in the
	 *          corresponding type-specific class, but was weakened by the fact that this interface
	 *          extends {@link List}.
	 *          <p>
	 *          Also, this is generally the only {@code spliterator} method subclasses should override.
	 *
	 * @implSpec The default implementation returns a late-binding spliterator (see {@link Spliterator}
	 *           for documentation on what binding policies mean).
	 *           <ul>
	 *           <li>For {@link java.util.RandomAccess RandomAccess} lists, this will return a
	 *           spliterator that calls the type-specific {@link #get(int)} method on the appropriate
	 *           indexes.</li>
	 *           <li>Otherwise, the spliterator returned will wrap this instance's type specific
	 *           {@link #iterator}.</li>
	 *           </ul>
	 *           <p>
	 *           In either case, the spliterator reports {@link Spliterator#SIZED},
	 *           {@link Spliterator#SUBSIZED}, and {@link Spliterator#ORDERED}.
	 *
	 * @implNote As the non-{@linkplain java.util.RandomAccess RandomAccess} case is based on the
	 *           iterator, and {@link java.util.Iterator} is an inherently linear API, the returned
	 *           spliterator will yield limited performance gains when run in parallel contexts, as the
	 *           returned spliterator's {@link Spliterator#trySplit() trySplit()} will have linear
	 *           runtime.
	 *           <p>
	 *           For {@link java.util.RandomAccess RandomAccess} lists, the parallel performance should
	 *           be reasonable assuming {@link #get(int)} is truly constant time like
	 *           {@link java.util.RandomAccess RandomAccess} suggests.
	 *
	 * @return {@inheritDoc}
	 * @since 8.5.0
	 */
	@Override
	default BooleanSpliterator spliterator() {
		if (this instanceof java.util.RandomAccess) {
			return new AbstractBooleanList.IndexBasedSpliterator(this, 0);
		} else {
			return BooleanSpliterators.asSpliterator(iterator(), sizeOf(this), BooleanSpliterators.LIST_SPLITERATOR_CHARACTERISTICS);
		}
	}

	/**
	 * Returns a type-specific list iterator on the list.
	 *
	 * @see List#listIterator()
	 */
	@Override
	BooleanListIterator listIterator();

	/**
	 * Returns a type-specific list iterator on the list starting at a given index.
	 *
	 * @see List#listIterator(int)
	 */
	@Override
	BooleanListIterator listIterator(int index);

	/**
	 * Returns a type-specific view of the portion of this list from the index {@code from}, inclusive,
	 * to the index {@code to}, exclusive.
	 *
	 * @apiNote This specification strengthens the one given in {@link List#subList(int,int)}.
	 *
	 * @see List#subList(int,int)
	 */
	@Override
	BooleanList subList(int from, int to);

	/**
	 * Sets the size of this list.
	 *
	 * <p>
	 * If the specified size is smaller than the current size, the last elements are discarded.
	 * Otherwise, they are filled with 0/{@code null}/{@code false}.
	 *
	 * @param size the new size.
	 */
	void size(int size);

	/**
	 * Copies (hopefully quickly) elements of this type-specific list into the given array.
	 *
	 * @param from the start index (inclusive).
	 * @param a the destination array.
	 * @param offset the offset into the destination array where to store the first element copied.
	 * @param length the number of elements to be copied.
	 */
	void getElements(int from, boolean a[], int offset, int length);

	/**
	 * Removes (hopefully quickly) elements of this type-specific list.
	 *
	 * @param from the start index (inclusive).
	 * @param to the end index (exclusive).
	 */
	void removeElements(int from, int to);

	/**
	 * Add (hopefully quickly) elements to this type-specific list.
	 *
	 * @param index the index at which to add elements.
	 * @param a the array containing the elements.
	 */
	void addElements(int index, boolean a[]);

	/**
	 * Add (hopefully quickly) elements to this type-specific list.
	 *
	 * @param index the index at which to add elements.
	 * @param a the array containing the elements.
	 * @param offset the offset of the first element to add.
	 * @param length the number of elements to add.
	 */
	void addElements(int index, boolean a[], int offset, int length);

	/**
	 * Set (hopefully quickly) elements to match the array given.
	 * 
	 * @param a the array containing the elements.
	 * @since 8.3.0
	 */
	default void setElements(boolean a[]) {
		setElements(0, a);
	}

	/**
	 * Set (hopefully quickly) elements to match the array given.
	 * 
	 * @param index the index at which to start setting elements.
	 * @param a the array containing the elements.
	 * @since 8.3.0
	 */
	default void setElements(int index, boolean a[]) {
		setElements(index, a, 0, a.length);
	}

	/**
	 * Set (hopefully quickly) elements to match the array given.
	 *
	 * Sets each in this list to the corresponding elements in the array, as if by
	 * 
	 * <pre>
	 * ListIterator iter = listIterator(index);
	 * int i = 0;
	 * while (i &lt; length) {
	 * 	iter.next();
	 * 	iter.set(a[offset + i++]);
	 * }
	 * </pre>
	 * 
	 * However, the exact implementation may be more efficient, taking into account whether random
	 * access is faster or not, or at the discretion of subclasses, abuse internals.
	 *
	 * @param index the index at which to start setting elements.
	 * @param a the array containing the elements
	 * @param offset the offset of the first element to add.
	 * @param length the number of elements to add.
	 * @since 8.3.0
	 */
	default void setElements(int index, boolean a[], int offset, int length) {
		// We can't use AbstractList#ensureIndex, sadly.
		if (index < 0) throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
		if (index > size()) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than list size (" + (size()) + ")");
		BooleanArrays.ensureOffsetLength(a, offset, length);
		if (index + length > size()) throw new IndexOutOfBoundsException("End index (" + (index + length) + ") is greater than list size (" + size() + ")");
		BooleanListIterator iter = listIterator(index);
		int i = 0;
		while (i < length) {
			iter.nextBoolean();
			iter.set(a[offset + i++]);
		}
	}

	/**
	 * Appends the specified element to the end of this list (optional operation).
	 * 
	 * @see List#add(Object)
	 */
	@Override
	boolean add(boolean key);

	/**
	 * Inserts the specified element at the specified position in this list (optional operation).
	 * 
	 * @see List#add(int,Object)
	 */
	void add(int index, boolean key);

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default void add(int index, Boolean key) {
		add(index, (key).booleanValue());
	}

	/**
	 * Inserts all of the elements in the specified type-specific collection into this type-specific
	 * list at the specified position (optional operation).
	 * 
	 * @see List#addAll(int,java.util.Collection)
	 */
	boolean addAll(int index, BooleanCollection c);

	/**
	 * Replaces the element at the specified position in this list with the specified element (optional
	 * operation).
	 * 
	 * @see List#set(int,Object)
	 */
	boolean set(int index, boolean k);

	/**
	 * Replaces each element of this list with the result of applying the operator to that element.
	 * 
	 * @param operator the operator to apply to each element.
	 * @see java.util.List#replaceAll
	 */
	default void replaceAll(final BooleanUnaryOperator operator) {
		final BooleanListIterator iter = listIterator();
		while (iter.hasNext()) {
			iter.set(operator.apply(iter.nextBoolean()));
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	@SuppressWarnings("boxing")
	default void replaceAll(final java.util.function.UnaryOperator<Boolean> operator) {
		java.util.Objects.requireNonNull(operator);
		// The instanceof and cast is required for performance. Without it, calls routed through this
		// overload using a primitive consumer would go through the slow lambda.
		replaceAll(operator instanceof BooleanUnaryOperator ? (BooleanUnaryOperator)operator : (BooleanUnaryOperator)operator::apply);
	}

	/**
	 * Returns the element at the specified position in this list.
	 * 
	 * @see List#get(int)
	 */
	boolean getBoolean(int index);

	/**
	 * Returns the index of the first occurrence of the specified element in this list, or -1 if this
	 * list does not contain the element.
	 * 
	 * @see List#indexOf(Object)
	 */
	int indexOf(boolean k);

	/**
	 * Returns the index of the last occurrence of the specified element in this list, or -1 if this
	 * list does not contain the element.
	 * 
	 * @see List#lastIndexOf(Object)
	 */
	int lastIndexOf(boolean k);

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean contains(final Object key) {
		return BooleanCollection.super.contains(key);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Boolean get(int index) {
		return Boolean.valueOf(getBoolean(index));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default int indexOf(Object o) {
		return indexOf(((Boolean)(o)).booleanValue());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default int lastIndexOf(Object o) {
		return lastIndexOf(((Boolean)(o)).booleanValue());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method specification is a workaround for
	 * <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8177440">bug 8177440</a>.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean add(Boolean k) {
		return add((k).booleanValue());
	}

	/**
	 * Removes the element at the specified position in this list (optional operation).
	 * 
	 * @see List#remove(int)
	 */
	boolean removeBoolean(int index);

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean remove(final Object key) {
		return BooleanCollection.super.remove(key);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Boolean remove(int index) {
		return Boolean.valueOf(removeBoolean(index));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Boolean set(int index, Boolean k) {
		return Boolean.valueOf(set(index, (k).booleanValue()));
	}

	/**
	 * Inserts all of the elements in the specified type-specific list into this type-specific list at
	 * the specified position (optional operation).
	 * 
	 * @apiNote This method exists only for the sake of efficiency: override are expected to use
	 *          {@link #getElements}/{@link #addElements}.
	 * @implSpec This method delegates to the one accepting a collection, but it might be implemented
	 *           more efficiently.
	 * @see List#addAll(int,Collection)
	 */
	default boolean addAll(int index, BooleanList l) {
		return addAll(index, (BooleanCollection)l);
	}

	/**
	 * Appends all of the elements in the specified type-specific list to the end of this type-specific
	 * list (optional operation).
	 * 
	 * @implSpec This method delegates to the index-based version, passing {@link #size()} as first
	 *           argument.
	 * @see List#addAll(Collection)
	 */
	default boolean addAll(BooleanList l) {
		return addAll(size(), l);
	}

	/**
	 * Returns an immutable empty list.
	 *
	 * @return an immutable empty list.
	 */
	public static BooleanList of() {
		// Returning ImmutableList.EMPTY instead of LISTS.EMPTY_LIST to make dimorphic call site.
		// See https://github.com/vigna/fastutil/issues/183
		return BooleanImmutableList.of();
	}

	/**
	 * Returns an immutable list with the element given.
	 *
	 * @param e the element that the returned list will contain.
	 * @return an immutable list containing {@code e}.
	 */
	public static BooleanList of(final boolean e) {
		return BooleanLists.singleton(e);
	}

	/**
	 * Returns an immutable list with the elements given.
	 *
	 * @param e0 the first element.
	 * @param e1 the second element.
	 * @return an immutable list containing {@code e0} and {@code e1}.
	 */
	public static BooleanList of(final boolean e0, final boolean e1) {
		return BooleanImmutableList.of(e0, e1);
	}

	/**
	 * Returns an immutable list with the elements given.
	 *
	 * @param e0 the first element.
	 * @param e1 the second element.
	 * @param e2 the third element.
	 * @return an immutable list containing {@code e0}, {@code e1}, and {@code e2}.
	 */
	public static BooleanList of(final boolean e0, final boolean e1, final boolean e2) {
		return BooleanImmutableList.of(e0, e1, e2);
	}

	/**
	 * Returns an immutable list with the elements given.
	 *
	 * <p>
	 * Note that this method does not perform a defensive copy.
	 *
	 * @param a a list of elements that will be used to initialize the immutable list.
	 * @return an immutable list containing the elements of {@code a}.
	 */

	public static BooleanList of(final boolean... a) {
		switch (a.length) {
		case 0:
			return of();
		case 1:
			return of(a[0]);
		// Add cases of 2 and 3 if we ever have special logic for those.
		default:
			// fall through
		}
		return BooleanImmutableList.of(a);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default void sort(final java.util.Comparator<? super Boolean> comparator) {
		sort(BooleanComparators.asBooleanComparator(comparator));
	}

	/**
	 * Sort a list using a type-specific comparator.
	 *
	 * <p>
	 * Pass {@code null} to sort using natural ordering.
	 * 
	 * @see List#sort(java.util.Comparator)
	 *
	 * @implSpec The default implementation dumps the elements into an array using {@link #toArray()},
	 *           sorts the array, then replaces all elements using the {@link #setElements} function.
	 *
	 *           <p>
	 *           It is possible for this method to call {@link #unstableSort} if it can determine that
	 *           the results of a stable and unstable sort are completely equivalent. This means if you
	 *           override {@link #unstableSort}, it should <em>not</em> call this method unless you
	 *           override this method as well.
	 *
	 * @since 8.3.0
	 */
	default void sort(final BooleanComparator comparator) {
		if (comparator == null) {
			// For non-floating point primitive types, when comparing naturally,
			// it is impossible to tell the difference between a stable and not-stable sort.
			// So just use the probably faster unstable sort.
			unstableSort(comparator);
		} else {
			boolean[] elements = toBooleanArray();
			BooleanArrays.stableSort(elements, comparator);
			setElements(elements);
		}
	}

	/**
	 * Sorts this list using a sort not assured to be stable.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	default void unstableSort(final java.util.Comparator<? super Boolean> comparator) {
		unstableSort(BooleanComparators.asBooleanComparator(comparator));
	}

	/**
	 * Sorts this list using a sort not assured to be stable.
	 *
	 * <p>
	 * Pass {@code null} to sort using natural ordering.
	 *
	 * <p>
	 * This differs from {@link List#sort(java.util.Comparator)} in that the results are not assured to
	 * be stable, but may be a bit faster.
	 *
	 * <p>
	 * Unless a subclass specifies otherwise, the results of the method if the list is concurrently
	 * modified during the sort are unspecified.
	 *
	 * @implSpec The default implementation dumps the elements into an array using {@link #toArray()},
	 *           sorts the array, then replaces all elements using the {@link #setElements} function.
	 *
	 * @since 8.3.0
	 */
	default void unstableSort(final BooleanComparator comparator) {
		boolean[] elements = toBooleanArray();
		if (comparator == null) {
			BooleanArrays.unstableSort(elements);
		} else {
			BooleanArrays.unstableSort(elements, comparator);
		}
		setElements(elements);
	}
}
