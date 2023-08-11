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

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A class providing static methods and objects that do useful things with type-specific sets.
 *
 * @see java.util.Collections
 */
public final class BooleanSets {
	private BooleanSets() {
	}

	/** The maximum size to choose ArraySet over OpenHashSet for utilites that automatically choose. */
	static final int ARRAY_SET_CUTOFF = 4;

	/**
	 * An immutable class representing the empty set and implementing a type-specific set interface.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a type-specific set.
	 */
	public static class EmptySet extends BooleanCollections.EmptyCollection implements BooleanSet, java.io.Serializable, Cloneable {
		private static final long serialVersionUID = -7046029254386353129L;

		protected EmptySet() {
		}

		@Override
		public boolean remove(boolean ok) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object clone() {
			return EMPTY_SET;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public boolean equals(final Object o) {
			return o instanceof Set && ((Set)o).isEmpty();
		}

		@Deprecated
		@Override
		public boolean rem(final boolean k) {
			return super.rem(k);
		}

		private Object readResolve() {
			return EMPTY_SET;
		}
	}

	/**
	 * An empty set (immutable). It is serializable and cloneable.
	 */

	public static final EmptySet EMPTY_SET = new EmptySet();
	// Used by Sets.of() instead of SETS.EMPTY_SET to make a dimorphic call site.
	// See https://github.com/vigna/fastutil/issues/183

	static final BooleanSet UNMODIFIABLE_EMPTY_SET = BooleanSets.unmodifiable(new BooleanArraySet(BooleanArrays.EMPTY_ARRAY));

	/**
	 * Returns an empty set (immutable). It is serializable and cloneable.
	 *
	 * <p>
	 * This method provides a typesafe access to {@link #EMPTY_SET}.
	 * 
	 * @return an empty set (immutable).
	 */
	@SuppressWarnings("unchecked")
	public static BooleanSet emptySet() {
		return EMPTY_SET;
	}

	/**
	 * An immutable class representing a type-specific singleton set.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a type-specific set.
	 */
	public static class Singleton extends AbstractBooleanSet implements java.io.Serializable, Cloneable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final boolean element;

		protected Singleton(final boolean element) {
			this.element = element;
		}

		@Override
		public boolean contains(final boolean k) {
			return ((k) == (element));
		}

		@Override
		public boolean remove(final boolean k) {
			throw new UnsupportedOperationException();
		}

		@Override
		public BooleanListIterator iterator() {
			return BooleanIterators.singleton(element);
		}

		@Override
		public BooleanSpliterator spliterator() {
			return BooleanSpliterators.singleton(element);
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public boolean[] toBooleanArray() {
			return new boolean[] { element };
		}

		@Deprecated
		@Override
		public void forEach(final Consumer<? super Boolean> action) {
			action.accept(Boolean.valueOf(element));
		}

		@Override
		public boolean addAll(final Collection<? extends Boolean> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(final Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(final Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Deprecated
		@Override
		public boolean removeIf(final java.util.function.Predicate<? super Boolean> filter) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void forEach(final BooleanConsumer action) {
			action.accept(element);
		}

		@Override
		public boolean addAll(final BooleanCollection c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(final BooleanCollection c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(final BooleanCollection c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeIf(final BooleanPredicate filter) {
			throw new UnsupportedOperationException();
		}

		@Deprecated
		@Override
		public Object[] toArray() {
			return new Object[] { Boolean.valueOf(element) };
		}

		@Override
		public Object clone() {
			return this;
		}
	}

	/**
	 * Returns a type-specific immutable set containing only the specified element. The returned set is
	 * serializable and cloneable.
	 *
	 * @param element the only element of the returned set.
	 * @return a type-specific immutable set containing just {@code element}.
	 */
	public static BooleanSet singleton(final boolean element) {
		return new Singleton(element);
	}

	/**
	 * Returns a type-specific immutable set containing only the specified element. The returned set is
	 * serializable and cloneable.
	 *
	 * @param element the only element of the returned set.
	 * @return a type-specific immutable set containing just {@code element}.
	 */
	public static BooleanSet singleton(final Boolean element) {
		return new Singleton((element).booleanValue());
	}

	/** A synchronized wrapper class for sets. */
	public static class SynchronizedSet extends BooleanCollections.SynchronizedCollection implements BooleanSet, java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;

		protected SynchronizedSet(final BooleanSet s, final Object sync) {
			super(s, sync);
		}

		protected SynchronizedSet(final BooleanSet s) {
			super(s);
		}

		@Override
		public boolean remove(final boolean k) {
			synchronized (sync) {
				return collection.rem(k);
			}
		}

		@Deprecated
		@Override
		public boolean rem(final boolean k) {
			return super.rem(k);
		}
	}

	/**
	 * Returns a synchronized type-specific set backed by the given type-specific set.
	 *
	 * @param s the set to be wrapped in a synchronized set.
	 * @return a synchronized view of the specified set.
	 * @see java.util.Collections#synchronizedSet(Set)
	 */
	public static BooleanSet synchronize(final BooleanSet s) {
		return new SynchronizedSet(s);
	}

	/**
	 * Returns a synchronized type-specific set backed by the given type-specific set, using an assigned
	 * object to synchronize.
	 *
	 * @param s the set to be wrapped in a synchronized set.
	 * @param sync an object that will be used to synchronize the access to the set.
	 * @return a synchronized view of the specified set.
	 * @see java.util.Collections#synchronizedSet(Set)
	 */
	public static BooleanSet synchronize(final BooleanSet s, final Object sync) {
		return new SynchronizedSet(s, sync);
	}

	/** An unmodifiable wrapper class for sets. */
	public static class UnmodifiableSet extends BooleanCollections.UnmodifiableCollection implements BooleanSet, java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;

		protected UnmodifiableSet(final BooleanSet s) {
			super(s);
		}

		@Override
		public boolean remove(final boolean k) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) return true;
			return collection.equals(o);
		}

		@Override
		public int hashCode() {
			return collection.hashCode();
		}

		@Deprecated
		@Override
		public boolean rem(final boolean k) {
			return super.rem(k);
		}
	}

	/**
	 * Returns an unmodifiable type-specific set backed by the given type-specific set.
	 *
	 * @param s the set to be wrapped in an unmodifiable set.
	 * @return an unmodifiable view of the specified set.
	 * @see java.util.Collections#unmodifiableSet(Set)
	 */
	public static BooleanSet unmodifiable(final BooleanSet s) {
		return new UnmodifiableSet(s);
	}
}
