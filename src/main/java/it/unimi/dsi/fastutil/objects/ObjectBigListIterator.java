/*
	* Copyright (C) 2010-2022 Sebastiano Vigna
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

import it.unimi.dsi.fastutil.BigListIterator;
import it.unimi.dsi.fastutil.SafeMath;

/**
 * A type-specific {@link BigListIterator}.
 *
 * @see BigListIterator
 */
public interface ObjectBigListIterator<K> extends ObjectBidirectionalIterator<K>, BigListIterator<K> {
	/**
	 * Replaces the last element returned by {@link BigListIterator#next() next()} or
	 * {@link BigListIterator#previous() previous()} with the specified element (optional operation).
	 * 
	 * @see java.util.ListIterator#set(Object)
	 */
	@Override
	default void set(@SuppressWarnings("unused") final K k) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Inserts the specified element into the list (optional operation).
	 * 
	 * @see java.util.ListIterator#add(Object)
	 */
	@Override
	default void add(@SuppressWarnings("unused") final K k) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Skips the given number of elements.
	 *
	 * <p>
	 * The effect of this call is exactly the same as that of calling {@link BigListIterator#next()
	 * next()} for {@code n} times (possibly stopping if {@link #hasNext()} becomes false).
	 *
	 * @param n the number of elements to skip.
	 * @return the number of elements actually skipped.
	 * @see BigListIterator#next()
	 */
	default long skip(final long n) {
		long i = n;
		while (i-- != 0 && hasNext()) next();
		return n - i - 1;
	}

	/**
	 * Moves back for the given number of elements.
	 *
	 * <p>
	 * The effect of this call is exactly the same as that of calling {@link BigListIterator#previous()
	 * previous()} for {@code n} times (possibly stopping if {@link #hasPrevious()} becomes false).
	 *
	 * @param n the number of elements to skip back.
	 * @return the number of elements actually skipped.
	 * @see BigListIterator#previous()
	 */
	default long back(final long n) {
		long i = n;
		while (i-- != 0 && hasPrevious()) previous();
		return n - i - 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default int skip(int n) {
		return SafeMath.safeLongToInt(skip((long)n));
	}
}
