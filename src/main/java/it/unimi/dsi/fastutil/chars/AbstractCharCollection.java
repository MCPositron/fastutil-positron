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
package it.unimi.dsi.fastutil.chars;

import java.util.AbstractCollection;
import java.util.Collection;

/**
 * An abstract class providing basic methods for collections implementing a type-specific interface.
 *
 * <p>
 * In particular, this class provide {@link #iterator()}, {@code add()}, {@link #remove(Object)} and
 * {@link #contains(Object)} methods that just call the type-specific counterpart.
 *
 * <p>
 * <strong>Warning</strong>: Because of a name clash between the list and collection interfaces the
 * type-specific deletion method of a type-specific abstract collection is {@code rem()}, rather
 * then {@code remove()}. A subclass must thus override {@code rem()}, rather than {@code remove()},
 * to make all inherited methods work properly.
 */
public abstract class AbstractCharCollection extends AbstractCollection<Character> implements CharCollection {
	protected AbstractCharCollection() {
	}

	@Override
	public abstract CharIterator iterator();

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean add(final char k) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation iterates over the elements in the collection, looking for the
	 *           specified element.
	 */
	@Override
	public boolean contains(final char k) {
		final CharIterator iterator = iterator();
		while (iterator.hasNext()) if (k == iterator.nextChar()) return true;
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation iterates over the elements in the collection, looking for the
	 *           specified element and tries to remove it.
	 */
	@Override
	public boolean rem(final char k) {
		final CharIterator iterator = iterator();
		while (iterator.hasNext()) if (k == iterator.nextChar()) {
			iterator.remove();
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean add(final Character key) {
		return CharCollection.super.add(key);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean contains(final Object key) {
		return CharCollection.super.contains(key);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean remove(final Object key) {
		return CharCollection.super.remove(key);
	}

	@Override
	public char[] toArray(char a[]) {
		final int size = size();
		if (a == null) {
			a = new char[size];
		} else if (a.length < size) {
			a = java.util.Arrays.copyOf(a, size);
		}
		CharIterators.unwrap(iterator(), a);
		return a;
	}

	@Override
	public char[] toCharArray() {
		return toArray((char[])null);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use {@code toArray()} instead&mdash;this method is redundant and will be
	 *             removed in the future.
	 */
	@Deprecated
	@Override
	public char[] toCharArray(final char a[]) {
		return toArray(a);
	}

	@Override
	public boolean addAll(final CharCollection c) {
		boolean retVal = false;
		for (final CharIterator i = c.iterator(); i.hasNext();) if (add(i.nextChar())) retVal = true;
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation delegates to the type-specific version if given a type-specific
	 *           collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean addAll(final Collection<? extends Character> c) {
		if (c instanceof CharCollection) {
			return addAll((CharCollection)c);
		}
		return super.addAll(c);
	}

	@Override
	public boolean containsAll(final CharCollection c) {
		for (final CharIterator i = c.iterator(); i.hasNext();) if (!contains(i.nextChar())) return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation delegates to the type-specific version if given a type-specific
	 *           collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {
		if (c instanceof CharCollection) {
			return containsAll((CharCollection)c);
		}
		return super.containsAll(c);
	}

	@Override
	public boolean removeAll(final CharCollection c) {
		boolean retVal = false;
		for (final CharIterator i = c.iterator(); i.hasNext();) if (rem(i.nextChar())) retVal = true;
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation delegates to the type-specific version if given a type-specific
	 *           collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
		if (c instanceof CharCollection) {
			return removeAll((CharCollection)c);
		}
		return super.removeAll(c);
	}

	@Override
	public boolean retainAll(final CharCollection c) {
		boolean retVal = false;
		for (final CharIterator i = iterator(); i.hasNext();) if (!c.contains(i.nextChar())) {
			i.remove();
			retVal = true;
		}
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation delegates to the type-specific version if given a type-specific
	 *           collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean retainAll(final Collection<?> c) {
		if (c instanceof CharCollection) {
			return retainAll((CharCollection)c);
		}
		return super.retainAll(c);
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		final CharIterator i = iterator();
		int n = size();
		char k;
		boolean first = true;
		s.append("{");
		while (n-- != 0) {
			if (first) first = false;
			else s.append(", ");
			k = i.nextChar();
			s.append(String.valueOf(k));
		}
		s.append("}");
		return s.toString();
	}
}
