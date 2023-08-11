/*
	* Copyright (C) 2003-2022 Paolo Boldi and Sebastiano Vigna
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

import java.util.Comparator;

/**
 * A class providing static methods and objects that do useful things with heaps.
 *
 * <p>
 * The static methods of this class allow to treat arrays as 0-based heaps. They are used in the
 * implementation of heap-based queues, but they may be also used directly.
 *
 */
public final class ObjectHeaps {
	private ObjectHeaps() {
	}

	/**
	 * Moves the given element down into the heap until it reaches the lowest possible position.
	 *
	 * @param heap the heap (starting at 0).
	 * @param size the number of elements in the heap.
	 * @param i the index of the element that must be moved down.
	 * @param c a type-specific comparator, or {@code null} for the natural order.
	 * @return the new position of the element of index {@code i}.
	 */
	@SuppressWarnings("unchecked")
	public static <K> int downHeap(final K[] heap, final int size, int i, final Comparator<? super K> c) {
		assert i < size;
		final K e = heap[i];
		int child;
		if (c == null) while ((child = (i << 1) + 1) < size) {
			K t = heap[child];
			final int right = child + 1;
			if (right < size && (((Comparable<K>)(heap[right])).compareTo(t) < 0)) t = heap[child = right];
			if ((((Comparable<K>)(e)).compareTo(t) <= 0)) break;
			heap[i] = t;
			i = child;
		}
		else while ((child = (i << 1) + 1) < size) {
			K t = heap[child];
			final int right = child + 1;
			if (right < size && c.compare(heap[right], t) < 0) t = heap[child = right];
			if (c.compare(e, t) <= 0) break;
			heap[i] = t;
			i = child;
		}
		heap[i] = e;
		return i;
	}

	/**
	 * Moves the given element up in the heap until it reaches the highest possible position.
	 *
	 * @param heap the heap (starting at 0).
	 * @param size the number of elements in the heap.
	 * @param i the index of the element that must be moved up.
	 * @param c a type-specific comparator, or {@code null} for the natural order.
	 * @return the new position of the element of index {@code i}.
	 */
	@SuppressWarnings("unchecked")
	public static <K> int upHeap(final K[] heap, final int size, int i, final Comparator<K> c) {
		assert i < size;
		final K e = heap[i];
		if (c == null) while (i != 0) {
			final int parent = (i - 1) >>> 1;
			final K t = heap[parent];
			if ((((Comparable<K>)(t)).compareTo(e) <= 0)) break;
			heap[i] = t;
			i = parent;
		}
		else while (i != 0) {
			final int parent = (i - 1) >>> 1;
			final K t = heap[parent];
			if (c.compare(t, e) <= 0) break;
			heap[i] = t;
			i = parent;
		}
		heap[i] = e;
		return i;
	}

	/**
	 * Makes an array into a heap.
	 *
	 * @param heap the heap (starting at 0).
	 * @param size the number of elements in the heap.
	 * @param c a type-specific comparator, or {@code null} for the natural order.
	 */
	public static <K> void makeHeap(final K[] heap, final int size, final Comparator<K> c) {
		int i = size >>> 1;
		while (i-- != 0) downHeap(heap, size, i, c);
	}
}
