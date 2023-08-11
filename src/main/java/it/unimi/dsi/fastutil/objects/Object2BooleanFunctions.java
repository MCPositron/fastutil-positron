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

import it.unimi.dsi.fastutil.Function;

/**
 * A class providing static methods and objects that do useful things with type-specific functions.
 *
 * @see it.unimi.dsi.fastutil.Function
 * @see java.util.Collections
 */
public final class Object2BooleanFunctions {
	private Object2BooleanFunctions() {
	}

	/**
	 * An immutable class representing an empty type-specific function.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a type-specific function.
	 */
	public static class EmptyFunction<K> extends AbstractObject2BooleanFunction<K> implements java.io.Serializable, Cloneable {
		private static final long serialVersionUID = -7046029254386353129L;

		protected EmptyFunction() {
		}

		@Override
		public boolean getBoolean(final Object k) {
			return (false);
		}

		@Override
		public boolean getOrDefault(final Object k, final boolean defaultValue) {
			return defaultValue;
		}

		@Override
		public boolean containsKey(final Object k) {
			return false;
		}

		@Override
		public boolean defaultReturnValue() {
			return (false);
		}

		@Override
		public void defaultReturnValue(final boolean defRetValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public void clear() {
		}

		@Override
		public Object clone() {
			return EMPTY_FUNCTION;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof Function)) return false;
			return ((Function<?, ?>)o).size() == 0;
		}

		@Override
		public String toString() {
			return "{}";
		}

		private Object readResolve() {
			return EMPTY_FUNCTION;
		}
	}

	/** An empty type-specific function (immutable). It is serializable and cloneable. */
	@SuppressWarnings("rawtypes")
	public static final EmptyFunction EMPTY_FUNCTION = new EmptyFunction();

	/**
	 * An immutable class representing a type-specific singleton function. Note that the default return
	 * value is still settable.
	 *
	 * <p>
	 * Note that albeit the function is immutable, its default return value may be changed.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a type-specific function.
	 */
	public static class Singleton<K> extends AbstractObject2BooleanFunction<K> implements java.io.Serializable, Cloneable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final K key;
		protected final boolean value;

		protected Singleton(final K key, final boolean value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public boolean containsKey(final Object k) {
			return java.util.Objects.equals(key, k);
		}

		@Override
		public boolean getBoolean(final Object k) {
			return java.util.Objects.equals(key, k) ? value : defRetValue;
		}

		@Override
		public boolean getOrDefault(final Object k, final boolean defaultValue) {
			return java.util.Objects.equals(key, k) ? value : defaultValue;
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public Object clone() {
			return this;
		}
	}

	/**
	 * Returns a type-specific immutable function containing only the specified pair. The returned
	 * function is serializable and cloneable.
	 *
	 * <p>
	 * Note that albeit the returned function is immutable, its default return value may be changed.
	 *
	 * @param key the only key of the returned function.
	 * @param value the only value of the returned function.
	 * @return a type-specific immutable function containing just the pair {@code &lt;key,value&gt;}.
	 */
	public static <K> Object2BooleanFunction<K> singleton(final K key, boolean value) {
		return new Singleton<>(key, value);
	}

	/**
	 * Returns a type-specific immutable function containing only the specified pair. The returned
	 * function is serializable and cloneable.
	 *
	 * <p>
	 * Note that albeit the returned function is immutable, its default return value may be changed.
	 *
	 * @param key the only key of the returned function.
	 * @param value the only value of the returned function.
	 * @return a type-specific immutable function containing just the pair {@code &lt;key,value&gt;}.
	 */
	public static <K> Object2BooleanFunction<K> singleton(final K key, final Boolean value) {
		return new Singleton<>((key), (value).booleanValue());
	}

	/** A synchronized wrapper class for functions. */
	public static class SynchronizedFunction<K> implements Object2BooleanFunction<K>, java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final Object2BooleanFunction<K> function;
		protected final Object sync;

		protected SynchronizedFunction(final Object2BooleanFunction<K> f, final Object sync) {
			if (f == null) throw new NullPointerException();
			this.function = f;
			this.sync = sync;
		}

		protected SynchronizedFunction(final Object2BooleanFunction<K> f) {
			if (f == null) throw new NullPointerException();
			this.function = f;
			this.sync = this;
		}

		@Override
		public boolean test(K operand) {
			synchronized (sync) {
				return function.test(operand);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Boolean apply(final K key) {
			synchronized (sync) {
				return function.apply(key);
			}
		}

		@Override
		public int size() {
			synchronized (sync) {
				return function.size();
			}
		}

		@Override
		public boolean defaultReturnValue() {
			synchronized (sync) {
				return function.defaultReturnValue();
			}
		}

		@Override
		public void defaultReturnValue(final boolean defRetValue) {
			synchronized (sync) {
				function.defaultReturnValue(defRetValue);
			}
		}

		@Override
		public boolean containsKey(final Object k) {
			synchronized (sync) {
				return function.containsKey(k);
			}
		}

		@Override
		public boolean put(final K k, final boolean v) {
			synchronized (sync) {
				return function.put(k, v);
			}
		}

		@Override
		public boolean getBoolean(final Object k) {
			synchronized (sync) {
				return function.getBoolean(k);
			}
		}

		@Override
		public boolean getOrDefault(final Object k, final boolean defaultValue) {
			synchronized (sync) {
				return function.getOrDefault(k, defaultValue);
			}
		}

		@Override
		public boolean removeBoolean(final Object k) {
			synchronized (sync) {
				return function.removeBoolean(k);
			}
		}

		@Override
		public void clear() {
			synchronized (sync) {
				function.clear();
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Boolean put(final K k, final Boolean v) {
			synchronized (sync) {
				return function.put(k, v);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Boolean get(final Object k) {
			synchronized (sync) {
				return function.get(k);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Boolean getOrDefault(final Object k, final Boolean defaultValue) {
			synchronized (sync) {
				return function.getOrDefault(k, defaultValue);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Boolean remove(final Object k) {
			synchronized (sync) {
				return function.remove(k);
			}
		}

		@Override
		public int hashCode() {
			synchronized (sync) {
				return function.hashCode();
			}
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) return true;
			synchronized (sync) {
				return function.equals(o);
			}
		}

		@Override
		public String toString() {
			synchronized (sync) {
				return function.toString();
			}
		}

		private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
			synchronized (sync) {
				s.defaultWriteObject();
			}
		}
	}

	/**
	 * Returns a synchronized type-specific function backed by the given type-specific function.
	 *
	 * @param f the function to be wrapped in a synchronized function.
	 * @return a synchronized view of the specified function.
	 * @see java.util.Collections#synchronizedMap(java.util.Map)
	 */
	public static <K> Object2BooleanFunction<K> synchronize(final Object2BooleanFunction<K> f) {
		return new SynchronizedFunction<>(f);
	}

	/**
	 * Returns a synchronized type-specific function backed by the given type-specific function, using
	 * an assigned object to synchronize.
	 *
	 * @param f the function to be wrapped in a synchronized function.
	 * @param sync an object that will be used to synchronize the access to the function.
	 * @return a synchronized view of the specified function.
	 * @see java.util.Collections#synchronizedMap(java.util.Map)
	 */
	public static <K> Object2BooleanFunction<K> synchronize(final Object2BooleanFunction<K> f, final Object sync) {
		return new SynchronizedFunction<>(f, sync);
	}

	/** An unmodifiable wrapper class for functions. */
	public static class UnmodifiableFunction<K> extends AbstractObject2BooleanFunction<K> implements java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final Object2BooleanFunction<? extends K> function;

		protected UnmodifiableFunction(final Object2BooleanFunction<? extends K> f) {
			if (f == null) throw new NullPointerException();
			this.function = f;
		}

		@Override
		public int size() {
			return function.size();
		}

		@Override
		public boolean defaultReturnValue() {
			return function.defaultReturnValue();
		}

		@Override
		public void defaultReturnValue(final boolean defRetValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsKey(final Object k) {
			return function.containsKey(k);
		}

		@Override
		public boolean put(final K k, final boolean v) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean getBoolean(final Object k) {
			return function.getBoolean(k);
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean getOrDefault(final Object k, final boolean defaultValue) {
			return ((Object2BooleanFunction<K>)function).getOrDefault(k, defaultValue);
		}

		@Override
		public boolean removeBoolean(final Object k) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Boolean put(final K k, final Boolean v) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Boolean get(final Object k) {
			return function.get(k);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		@SuppressWarnings("unchecked")
		public Boolean getOrDefault(final Object k, final Boolean defaultValue) {
			return ((Object2BooleanFunction<K>)function).getOrDefault(k, defaultValue);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Boolean remove(final Object k) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int hashCode() {
			return function.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return o == this || function.equals(o);
		}

		@Override
		public String toString() {
			return function.toString();
		}
	}

	/**
	 * Returns an unmodifiable type-specific function backed by the given type-specific function.
	 *
	 * @param f the function to be wrapped in an unmodifiable function.
	 * @return an unmodifiable view of the specified function.
	 * @see java.util.Collections#unmodifiableMap(java.util.Map)
	 */
	public static <K> Object2BooleanFunction<K> unmodifiable(final Object2BooleanFunction<? extends K> f) {
		return new UnmodifiableFunction<>(f);
	}

	/** An adapter for mapping generic total functions to partial primitive functions. */
	public static class PrimitiveFunction<K> implements Object2BooleanFunction<K> {
		protected final java.util.function.Function<? super K, ? extends Boolean> function;

		protected PrimitiveFunction(java.util.function.Function<? super K, ? extends Boolean> function) {
			this.function = function;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean containsKey(Object key) {
			return function.apply((K)(key)) != null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean getBoolean(Object key) {
			Boolean v = function.apply((K)(key));
			if (v == null) return defaultReturnValue();
			return (v).booleanValue();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean getOrDefault(Object key, boolean defaultValue) {
			Boolean v = function.apply((K)(key));
			if (v == null) return defaultValue;
			return (v).booleanValue();
		}

		@SuppressWarnings("unchecked")
		@Deprecated
		@Override
		public Boolean get(Object key) {
			return function.apply((K)key);
		}

		@SuppressWarnings("unchecked")
		@Deprecated
		@Override
		public Boolean getOrDefault(Object key, Boolean defaultValue) {
			final Boolean v;
			return (v = function.apply((K)key)) == null ? defaultValue : v;
		}

		@Deprecated
		@Override
		public Boolean put(final K key, final Boolean value) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Returns a (partial) type-specific function based on the given total generic function.
	 * <p>
	 * The returned function contains all keys which are not mapped to {@code null}. If the function
	 * already is a primitive function, it is returned without changes.
	 * <p>
	 * <strong>Warning</strong>: If the given function is a &ldquo;widened&rdquo; primitive function
	 * (e.g. an {@code Int2IntFunction} given to {@code Short2ShortFunctions}), it still is wrapped into
	 * a proxy, decreasing performance.
	 *
	 * @param f the function to be converted to a type-specific function.
	 * @return a primitive view of the specified function.
	 * @throws NullPointerException if {@code f} is null.
	 * @see PrimitiveFunction
	 * @since 8.1.0
	 */
	@SuppressWarnings("unchecked")
	public static <K> Object2BooleanFunction<K> primitive(final java.util.function.Function<? super K, ? extends Boolean> f) {
		java.util.Objects.requireNonNull(f);
		if (f instanceof Object2BooleanFunction) return (Object2BooleanFunction<K>)f;
		if (f instanceof java.util.function.Predicate) return key -> ((java.util.function.Predicate<K>)f).test((K)(key));
		return new PrimitiveFunction<>(f);
	}
}
