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
public interface ObjectList<K> extends List<K>, Comparable<List<? extends K>>, ObjectCollection<K> {
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
	ObjectListIterator<K> iterator();

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
	default ObjectSpliterator<K> spliterator() {
		if (this instanceof java.util.RandomAccess) {
			return new AbstractObjectList.IndexBasedSpliterator<>(this, 0);
		} else {
			return ObjectSpliterators.asSpliterator(iterator(), sizeOf(this), ObjectSpliterators.LIST_SPLITERATOR_CHARACTERISTICS);
		}
	}

	/**
	 * Returns a type-specific list iterator on the list.
	 *
	 * @see List#listIterator()
	 */
	@Override
	ObjectListIterator<K> listIterator();

	/**
	 * Returns a type-specific list iterator on the list starting at a given index.
	 *
	 * @see List#listIterator(int)
	 */
	@Override
	ObjectListIterator<K> listIterator(int index);

	/**
	 * Returns a type-specific view of the portion of this list from the index {@code from}, inclusive,
	 * to the index {@code to}, exclusive.
	 *
	 * @apiNote This specification strengthens the one given in {@link List#subList(int,int)}.
	 *
	 * @see List#subList(int,int)
	 */
	@Override
	ObjectList<K> subList(int from, int to);

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
	void getElements(int from, Object a[], int offset, int length);

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
	void addElements(int index, K a[]);

	/**
	 * Add (hopefully quickly) elements to this type-specific list.
	 *
	 * @param index the index at which to add elements.
	 * @param a the array containing the elements.
	 * @param offset the offset of the first element to add.
	 * @param length the number of elements to add.
	 */
	void addElements(int index, K a[], int offset, int length);

	/**
	 * Set (hopefully quickly) elements to match the array given.
	 * 
	 * @param a the array containing the elements.
	 * @since 8.3.0
	 */
	default void setElements(K a[]) {
		setElements(0, a);
	}

	/**
	 * Set (hopefully quickly) elements to match the array given.
	 * 
	 * @param index the index at which to start setting elements.
	 * @param a the array containing the elements.
	 * @since 8.3.0
	 */
	default void setElements(int index, K a[]) {
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
	default void setElements(int index, K a[], int offset, int length) {
		// We can't use AbstractList#ensureIndex, sadly.
		if (index < 0) throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
		if (index > size()) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than list size (" + (size()) + ")");
		ObjectArrays.ensureOffsetLength(a, offset, length);
		if (index + length > size()) throw new IndexOutOfBoundsException("End index (" + (index + length) + ") is greater than list size (" + size() + ")");
		ObjectListIterator<K> iter = listIterator(index);
		int i = 0;
		while (i < length) {
			iter.next();
			iter.set(a[offset + i++]);
		}
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
	default boolean addAll(int index, ObjectList<? extends K> l) {
		return addAll(index, (ObjectCollection<? extends K>)l);
	}

	/**
	 * Appends all of the elements in the specified type-specific list to the end of this type-specific
	 * list (optional operation).
	 * 
	 * @implSpec This method delegates to the index-based version, passing {@link #size()} as first
	 *           argument.
	 * @see List#addAll(Collection)
	 */
	default boolean addAll(ObjectList<? extends K> l) {
		return addAll(size(), l);
	}

	/**
	 * Returns an immutable empty list.
	 *
	 * @return an immutable empty list.
	 */
	public static <K> ObjectList<K> of() {
		// Returning ImmutableList.EMPTY instead of LISTS.EMPTY_LIST to make dimorphic call site.
		// See https://github.com/vigna/fastutil/issues/183
		return ObjectImmutableList.of();
	}

	/**
	 * Returns an immutable list with the element given.
	 *
	 * @param e the element that the returned list will contain.
	 * @return an immutable list containing {@code e}.
	 */
	public static <K> ObjectList<K> of(final K e) {
		return ObjectLists.singleton(e);
	}

	/**
	 * Returns an immutable list with the elements given.
	 *
	 * @param e0 the first element.
	 * @param e1 the second element.
	 * @return an immutable list containing {@code e0} and {@code e1}.
	 */
	public static <K> ObjectList<K> of(final K e0, final K e1) {
		return ObjectImmutableList.of(e0, e1);
	}

	/**
	 * Returns an immutable list with the elements given.
	 *
	 * @param e0 the first element.
	 * @param e1 the second element.
	 * @param e2 the third element.
	 * @return an immutable list containing {@code e0}, {@code e1}, and {@code e2}.
	 */
	public static <K> ObjectList<K> of(final K e0, final K e1, final K e2) {
		return ObjectImmutableList.of(e0, e1, e2);
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
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static <K> ObjectList<K> of(final K... a) {
		switch (a.length) {
		case 0:
			return of();
		case 1:
			return of(a[0]);
		// Add cases of 2 and 3 if we ever have special logic for those.
		default:
			// fall through
		}
		return ObjectImmutableList.of(a);
	}

	/**
	 * Sorts this list using a sort assured to be stable.
	 *
	 * <p>
	 * Pass {@code null} to sort using natural ordering.
	 *
	 * <p>
	 * Unless a subclass specifies otherwise, the results of the method if the list is concurrently
	 * modified during the sort are unspecified.
	 *
	 * @implSpec The default implementation dumps the elements into an array using {@link #toArray()},
	 *           sorts the array, then replaces all elements using the {@link #setElements} function.
	 *
	 * @since 8.5.0
	 */
	@Override
	@SuppressWarnings("unchecked")
	default void sort(final java.util.Comparator<? super K> comparator) {
		K[] elements = (K[])toArray();
		// Current stableSort implementation delegates to java.util.Arrays.sort for reference types,
		// so we aren't losing out on JDK's optimized Timsort.
		if (comparator == null) {
			ObjectArrays.stableSort(elements);
		} else {
			ObjectArrays.stableSort(elements, comparator);
		}
		setElements(elements);
	}

	/**
	 * Sorts this list using a sort not assured to be stable. This differs from
	 * {@link List#sort(java.util.Comparator)} in that the results are not assured to be stable, but may
	 * be a bit faster.
	 *
	 * <p>
	 * Pass {@code null} to sort using natural ordering.
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
	@SuppressWarnings("unchecked")
	default void unstableSort(final java.util.Comparator<? super K> comparator) {
		K[] elements = (K[])toArray();
		if (comparator == null) {
			ObjectArrays.unstableSort(elements);
		} else {
			ObjectArrays.unstableSort(elements, comparator);
		}
		setElements(elements);
	}
}
