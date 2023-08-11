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
package it.unimi.dsi.fastutil.longs;

import static it.unimi.dsi.fastutil.BigArrays.copyFromBig;
import static it.unimi.dsi.fastutil.BigArrays.copyToBig;
import static it.unimi.dsi.fastutil.BigArrays.grow;
import static it.unimi.dsi.fastutil.BigArrays.trim;
import static it.unimi.dsi.fastutil.longs.LongArrayFrontCodedList.count;
import static it.unimi.dsi.fastutil.longs.LongArrayFrontCodedList.readInt;
import static it.unimi.dsi.fastutil.longs.LongArrayFrontCodedList.writeInt;
import it.unimi.dsi.fastutil.BigArrays;
import it.unimi.dsi.fastutil.objects.AbstractObjectBigList;
import it.unimi.dsi.fastutil.objects.ObjectBigListIterator;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * Compact storage of big lists of arrays using front-coding (also known as prefix-omission)
 * compression.
 *
 * <p>
 * This class stores immutably a big list of arrays in a single
 * {@linkplain it.unimi.dsi.fastutil.BigArrays big array} using front coding (of course, the
 * compression will be reasonable only if the list is sorted lexicographically&mdash;see below). It
 * implements an immutable type-specific list that returns the <var>i</var>-th array when calling
 * {@link #get(long) get(<var>i</var>)}. The returned array may be freely modified.
 *
 * <p>
 * Front-coding (also known as prefix-omission) compression is based on the idea that if the
 * <var>i</var>-th and the (<var>i</var>+1)-th array have a common prefix, we might store the length
 * of the common prefix, and then the rest of the second array.
 *
 * <p>
 * This approach, of course, requires that once in a while an array is stored entirely. The
 * <em>ratio</em> of a front-coded list defines how often this happens (once every {@link #ratio()}
 * arrays). A higher ratio means more compression, but means also a longer access time, as more
 * arrays have to be probed to build the result. Note that we must build an array every time
 * {@link #get(long)} is called, but this class provides also methods that extract one of the stored
 * arrays in a given array, reducing garbage collection. See the documentation of the family of
 * {@code get()} methods.
 *
 * <p>
 * By setting the ratio to 1 we actually disable front coding: however, we still have a data
 * structure storing large list of arrays with a reduced overhead (just one integer per array, plus
 * the space required for lengths).
 *
 * <p>
 * Note that the typical usage of front-coded lists is under the form of serialized objects;
 * usually, the data that has to be compacted is processed offline, and the resulting structure is
 * stored permanently. Since the pointer array is not stored, the serialized format is very small.
 *
 * <H2>Implementation Details</H2>
 *
 * <p>
 * All arrays are stored in a {@linkplain it.unimi.dsi.fastutil.BigArrays big array}. A separate
 * array of pointers indexes arrays whose position is a multiple of the ratio: thus, a higher ratio
 * means also less pointers.
 *
 * <p>
 * More in detail, an array whose position is a multiple of the ratio is stored as the array length,
 * followed by the elements of the array. The array length is coded by a simple variable-length list
 * of <var>k</var>-1 bit blocks, where <var>k</var> is the number of bits of the underlying
 * primitive type. All other arrays are stored as follows: let {@code common} the length of the
 * maximum common prefix between the array and its predecessor. Then we store the array length
 * decremented by {@code common}, followed by {@code common}, followed by the array elements whose
 * index is greater than or equal to {@code common}. For instance, if we store {@code foo},
 * {@code foobar}, {@code football} and {@code fool} in a front-coded character-array list with
 * ratio 3, the character array will contain
 *
 * <pre>
* <b>3</b> f o o <b>3</b> <b>3</b> b a r <b>5</b> <b>3</b> t b a l l <b>4</b> f o o l
 * </pre>
 */
public class LongArrayFrontCodedBigList extends AbstractObjectBigList<long[]> implements Serializable, Cloneable, RandomAccess {
	private static final long serialVersionUID = 1L;
	/** The number of arrays in the list. */
	protected final long n;
	/** The ratio of this front-coded list. */
	protected final int ratio;
	/** The big array containing the compressed arrays. */
	protected final long[][] array;
	/** The pointers to entire arrays in the list. */
	protected transient long[][] p;

	/**
	 * Creates a new front-coded list containing the arrays returned by the given iterator.
	 *
	 * @param arrays an iterator returning arrays.
	 * @param ratio the desired ratio.
	 */
	public LongArrayFrontCodedBigList(final Iterator<long[]> arrays, final int ratio) {
		if (ratio < 1) throw new IllegalArgumentException("Illegal ratio (" + ratio + ")");
		long[][] array = LongBigArrays.EMPTY_BIG_ARRAY;
		long[][] p = LongBigArrays.EMPTY_BIG_ARRAY;
		long[][] a = new long[2][];
		long curSize = 0;
		long n = 0;
		int b = 0, length;
		while (arrays.hasNext()) {
			a[b] = arrays.next();
			length = a[b].length;
			if (n % ratio == 0) {
				p = grow(p, n / ratio + 1);
				BigArrays.set(p, n / ratio, curSize);
				array = grow(array, curSize + count(length) + length, curSize);
				curSize += writeInt(array, length, curSize);
				copyToBig(a[b], 0, array, curSize, length);
				curSize += length;
			} else {
				final int minLength = Math.min(a[1 - b].length, length);
				int common;
				for (common = 0; common < minLength; common++) if (a[0][common] != a[1][common]) break;
				length -= common;
				array = grow(array, curSize + count(length) + count(common) + length, curSize);
				curSize += writeInt(array, length, curSize);
				curSize += writeInt(array, common, curSize);
				copyToBig(a[b], common, array, curSize, length);
				curSize += length;
			}
			b = 1 - b;
			n++;
		}
		this.n = n;
		this.ratio = ratio;
		this.array = trim(array, curSize);
		this.p = trim(p, (n + ratio - 1) / ratio);
	}

	/**
	 * Creates a new front-coded list containing the arrays in the given collection.
	 *
	 * @param c a collection containing arrays.
	 * @param ratio the desired ratio.
	 */
	public LongArrayFrontCodedBigList(final Collection<long[]> c, final int ratio) {
		this(c.iterator(), ratio);
	}

	public int ratio() {
		return ratio;
	}

	/**
	 * Computes the length of the array at the given index.
	 *
	 * <p>
	 * This private version of {@link #arrayLength(long)} does not check its argument.
	 *
	 * @param index an index.
	 * @return the length of the {@code index}-th array.
	 */
	private int length(final long index) {
		final long[][] array = this.array;
		final int delta = (int)(index % ratio); // The index into the p array, and the delta inside the block.
		long pos = BigArrays.get(p, index / ratio); // The position into the array of the first entire word before the
													// index-th.
		int length = readInt(array, pos);
		if (delta == 0) return length;
		// First of all, we recover the array length and the maximum amount of copied elements.
		int common;
		pos += count(length) + length;
		length = readInt(array, pos);
		common = readInt(array, pos + count(length));
		for (int i = 0; i < delta - 1; i++) {
			pos += count(length) + count(common) + length;
			length = readInt(array, pos);
			common = readInt(array, pos + count(length));
		}
		return length + common;
	}

	/**
	 * Computes the length of the array at the given index.
	 *
	 * @param index an index.
	 * @return the length of the {@code index}-th array.
	 */
	public int arrayLength(final long index) {
		ensureRestrictedIndex(index);
		return length(index);
	}

	/**
	 * Extracts the array at the given index.
	 *
	 * @param index an index.
	 * @param a the array that will store the result (we assume that it can hold the result).
	 * @param offset an offset into {@code a} where elements will be store.
	 * @param length a maximum number of elements to store in {@code a}.
	 * @return the length of the extracted array.
	 */
	private int extract(final long index, final long a[], final int offset, final int length) {
		final int delta = (int)(index % ratio); // The delta inside the block.
		final long startPos = BigArrays.get(p, index / ratio); // The position into the array of the first entire word
																// before the index-th.
		long pos, prevArrayPos;
		int arrayLength = readInt(array, pos = startPos), currLen = 0, actualCommon;
		if (delta == 0) {
			pos = BigArrays.get(p, index / ratio) + count(arrayLength);
			copyFromBig(array, pos, a, offset, Math.min(length, arrayLength));
			return arrayLength;
		}
		int common = 0;
		for (int i = 0; i < delta; i++) {
			prevArrayPos = pos + count(arrayLength) + (i != 0 ? count(common) : 0);
			pos = prevArrayPos + arrayLength;
			arrayLength = readInt(array, pos);
			common = readInt(array, pos + count(arrayLength));
			actualCommon = Math.min(common, length);
			if (actualCommon <= currLen) currLen = actualCommon;
			else {
				copyFromBig(array, prevArrayPos, a, currLen + offset, actualCommon - currLen);
				currLen = actualCommon;
			}
		}
		if (currLen < length) copyFromBig(array, pos + count(arrayLength) + count(common), a, currLen + offset, Math.min(arrayLength, length - currLen));
		return arrayLength + common;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @implSpec This implementation delegates to {@link #getArray(long)}.
	 */
	@Override
	public long[] get(final long index) {
		return getArray(index);
	}

	/**
	 * Returns an array stored in this front-coded list.
	 *
	 * @param index an index.
	 * @return the corresponding array stored in this front-coded list.
	 */
	public long[] getArray(final long index) {
		ensureRestrictedIndex(index);
		final int length = length(index);
		final long a[] = new long[length];
		extract(index, a, 0, length);
		return a;
	}

	/**
	 * Stores in the given array elements from an array stored in this front-coded list.
	 *
	 * @param index an index.
	 * @param a the array that will store the result.
	 * @param offset an offset into {@code a} where elements will be store.
	 * @param length a maximum number of elements to store in {@code a}.
	 * @return if {@code a} can hold the extracted elements, the number of extracted elements;
	 *         otherwise, the number of remaining elements with the sign changed.
	 */
	public int get(final long index, final long[] a, final int offset, final int length) {
		ensureRestrictedIndex(index);
		LongArrays.ensureOffsetLength(a, offset, length);
		final int arrayLength = extract(index, a, offset, length);
		if (length >= arrayLength) return arrayLength;
		return length - arrayLength;
	}

	/**
	 * Stores in the given array an array stored in this front-coded list.
	 *
	 * @param index an index.
	 * @param a the array that will store the content of the result (we assume that it can hold the
	 *            result).
	 * @return if {@code a} can hold the extracted elements, the number of extracted elements;
	 *         otherwise, the number of remaining elements with the sign changed.
	 */
	public int get(final long index, final long[] a) {
		return get(index, a, 0, a.length);
	}

	@Override
	public long size64() {
		return n;
	}

	@Override
	public ObjectBigListIterator<long[]> listIterator(final long start) {
		ensureIndex(start);
		return new ObjectBigListIterator<long[]>() {
			long s[] = LongArrays.EMPTY_ARRAY;
			long i = 0;
			long pos = 0;
			boolean inSync; // Whether the current value in a is the string just before the next to be produced.
			{
				if (start != 0) {
					if (start == n) i = start; // If we start at the end, we do nothing.
					else {
						pos = BigArrays.get(p, start / ratio);
						int j = (int)(start % ratio);
						i = start - j;
						while (j-- != 0) next();
					}
				}
			}

			@Override
			public boolean hasNext() {
				return i < n;
			}

			@Override
			public boolean hasPrevious() {
				return i > 0;
			}

			@Override
			public long previousIndex() {
				return i - 1;
			}

			@Override
			public long nextIndex() {
				return i;
			}

			@Override
			public long[] next() {
				int length, common;
				if (!hasNext()) throw new NoSuchElementException();
				if (i % ratio == 0) {
					pos = BigArrays.get(p, i / ratio);
					length = readInt(array, pos);
					s = LongArrays.ensureCapacity(s, length, 0);
					copyFromBig(array, pos + count(length), s, 0, length);
					pos += length + count(length);
					inSync = true;
				} else {
					if (inSync) {
						length = readInt(array, pos);
						common = readInt(array, pos + count(length));
						s = LongArrays.ensureCapacity(s, length + common, common);
						copyFromBig(array, pos + count(length) + count(common), s, common, length);
						pos += count(length) + count(common) + length;
						length += common;
					} else {
						s = LongArrays.ensureCapacity(s, length = length(i), 0);
						extract(i, s, 0, length);
					}
				}
				i++;
				return LongArrays.copy(s, 0, length);
			}

			@Override
			public long[] previous() {
				if (!hasPrevious()) throw new NoSuchElementException();
				inSync = false;
				return getArray(--i);
			}
		};
	}

	/**
	 * Returns a copy of this list.
	 *
	 * @return a copy of this list.
	 */
	@Override
	public LongArrayFrontCodedBigList clone() {
		return this;
	}

	@Override
	public String toString() {
		final StringBuffer s = new StringBuffer();
		s.append("[");
		for (long i = 0; i < n; i++) {
			if (i != 0) s.append(", ");
			s.append(LongArrayList.wrap(getArray(i)).toString());
		}
		s.append("]");
		return s.toString();
	}

	/**
	 * Computes the pointer big array using the currently set ratio, number of elements and underlying
	 * array.
	 *
	 * @return the computed pointer big array.
	 */
	protected long[][] rebuildPointerArray() {
		final long[][] p = LongBigArrays.newBigArray((n + ratio - 1) / ratio);
		final long a[][] = array;
		int length, count;
		long pos = 0;
		int skip = ratio - 1;
		for (long i = 0, j = 0; i < n; i++) {
			length = readInt(a, pos);
			count = count(length);
			if (++skip == ratio) {
				skip = 0;
				BigArrays.set(p, j++, pos);
				pos += count + length;
			} else pos += count + count(readInt(a, pos + count)) + length;
		}
		return p;
	}

	public void dump(java.io.DataOutputStream array, java.io.DataOutputStream pointers) throws java.io.IOException {
		for (long[] s : this.array) for (long e : s) array.writeLong(e);
		for (long[] s : p) for (long e : s) pointers.writeLong(e);
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		// Rebuild pointer array
		p = rebuildPointerArray();
	}
}
