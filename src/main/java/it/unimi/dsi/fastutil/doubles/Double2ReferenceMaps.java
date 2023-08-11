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
package it.unimi.dsi.fastutil.doubles;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterable;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import it.unimi.dsi.fastutil.objects.ReferenceCollections;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import java.util.Map;
import java.util.function.Consumer;
import it.unimi.dsi.fastutil.doubles.Double2ReferenceMap.FastEntrySet;

/**
 * A class providing static methods and objects that do useful things with type-specific maps.
 *
 * @see java.util.Collections
 */
public final class Double2ReferenceMaps {
	private Double2ReferenceMaps() {
	}

	/**
	 * Returns an iterator that will be {@linkplain FastEntrySet fast}, if possible, on the
	 * {@linkplain Map#entrySet() entry set} of the provided {@code map}.
	 * 
	 * @param map a map from which we will try to extract a (fast) iterator on the entry set.
	 * @return an iterator on the entry set of the given map that will be fast, if possible.
	 * @since 8.0.0
	 */
	@SuppressWarnings("unchecked")
	public static <V> ObjectIterator<Double2ReferenceMap.Entry<V>> fastIterator(Double2ReferenceMap<V> map) {
		final ObjectSet<Double2ReferenceMap.Entry<V>> entries = map.double2ReferenceEntrySet();
		return entries instanceof Double2ReferenceMap.FastEntrySet ? ((Double2ReferenceMap.FastEntrySet<V>)entries).fastIterator() : entries.iterator();
	}

	/**
	 * Iterates {@linkplain FastEntrySet#fastForEach(Consumer) quickly}, if possible, on the
	 * {@linkplain Map#entrySet() entry set} of the provided {@code map}.
	 * 
	 * @param map a map on which we will try to iterate {@linkplain FastEntrySet#fastForEach(Consumer)
	 *            quickly}.
	 * @param consumer the consumer that will be passed to {@link FastEntrySet#fastForEach(Consumer)},
	 *            if possible, or to {@link Iterable#forEach(Consumer)}.
	 * @since 8.1.0
	 */
	@SuppressWarnings("unchecked")
	public static <V> void fastForEach(Double2ReferenceMap<V> map, final Consumer<? super Double2ReferenceMap.Entry<V>> consumer) {
		final ObjectSet<Double2ReferenceMap.Entry<V>> entries = map.double2ReferenceEntrySet();
		if (entries instanceof Double2ReferenceMap.FastEntrySet) ((Double2ReferenceMap.FastEntrySet<V>)entries).fastForEach(consumer);
		else entries.forEach(consumer);
	}

	/**
	 * Returns an iterable yielding an iterator that will be {@linkplain FastEntrySet fast}, if
	 * possible, on the {@linkplain Map#entrySet() entry set} of the provided {@code map}.
	 * 
	 * @param map a map from which we will try to extract an iterable yielding a (fast) iterator on the
	 *            entry set.
	 * @return an iterable yielding an iterator on the entry set of the given map that will be fast, if
	 *         possible.
	 * @since 8.0.0
	 */
	@SuppressWarnings("unchecked")
	public static <V> ObjectIterable<Double2ReferenceMap.Entry<V>> fastIterable(Double2ReferenceMap<V> map) {
		final ObjectSet<Double2ReferenceMap.Entry<V>> entries = map.double2ReferenceEntrySet();
		return entries instanceof Double2ReferenceMap.FastEntrySet ? new ObjectIterable<Double2ReferenceMap.Entry<V>>() {
			@Override
			public ObjectIterator<Double2ReferenceMap.Entry<V>> iterator() {
				return ((Double2ReferenceMap.FastEntrySet<V>)entries).fastIterator();
			}

			@Override
			public ObjectSpliterator<Double2ReferenceMap.Entry<V>> spliterator() {
				return entries.spliterator();
			}

			@Override
			public void forEach(final Consumer<? super Double2ReferenceMap.Entry<V>> consumer) {
				((Double2ReferenceMap.FastEntrySet<V>)entries).fastForEach(consumer);
			}
		} : entries;
	}

	/**
	 * An immutable class representing an empty type-specific map.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a type-specific map.
	 */
	public static class EmptyMap<V> extends Double2ReferenceFunctions.EmptyFunction<V> implements Double2ReferenceMap<V>, java.io.Serializable, Cloneable {
		private static final long serialVersionUID = -7046029254386353129L;

		protected EmptyMap() {
		}

		@Override
		public boolean containsValue(final Object v) {
			return false;
		}

		@Deprecated
		@Override
		public V getOrDefault(final Object key, final V defaultValue) {
			return defaultValue;
		}

		@Override
		public V getOrDefault(final double key, final V defaultValue) {
			return defaultValue;
		}

		@Override
		public void putAll(final Map<? extends Double, ? extends V> m) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ObjectSet<Double2ReferenceMap.Entry<V>> double2ReferenceEntrySet() {
			return ObjectSets.EMPTY_SET;
		}

		@Override
		public DoubleSet keySet() {
			return DoubleSets.EMPTY_SET;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ReferenceCollection<V> values() {
			return ReferenceSets.EMPTY_SET;
		}

		@Override
		public void forEach(final java.util.function.BiConsumer<? super Double, ? super V> consumer) {
		}

		@Override
		public Object clone() {
			return EMPTY_MAP;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof Map)) return false;
			return ((Map<?, ?>)o).isEmpty();
		}

		@Override
		public String toString() {
			return "{}";
		}
	}

	/**
	 * An empty type-specific map (immutable). It is serializable and cloneable.
	 */
	@SuppressWarnings("rawtypes")
	public static final EmptyMap EMPTY_MAP = new EmptyMap();

	/**
	 * Returns an empty map (immutable). It is serializable and cloneable.
	 *
	 * <p>
	 * This method provides a typesafe access to {@link #EMPTY_MAP}.
	 * 
	 * @return an empty map (immutable).
	 */
	@SuppressWarnings("unchecked")
	public static <V> Double2ReferenceMap<V> emptyMap() {
		return EMPTY_MAP;
	}

	/**
	 * An immutable class representing a type-specific singleton map.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a type-specific map.
	 */
	public static class Singleton<V> extends Double2ReferenceFunctions.Singleton<V> implements Double2ReferenceMap<V>, java.io.Serializable, Cloneable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected transient ObjectSet<Double2ReferenceMap.Entry<V>> entries;
		protected transient DoubleSet keys;
		protected transient ReferenceCollection<V> values;

		protected Singleton(final double key, final V value) {
			super(key, value);
		}

		@Override
		public boolean containsValue(final Object v) {
			return ((value) == (v));
		}

		@Override
		public void putAll(final Map<? extends Double, ? extends V> m) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ObjectSet<Double2ReferenceMap.Entry<V>> double2ReferenceEntrySet() {
			if (entries == null) entries = ObjectSets.singleton(new AbstractDouble2ReferenceMap.BasicEntry<>(key, value));
			return entries;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public ObjectSet<Map.Entry<Double, V>> entrySet() {
			return (ObjectSet)double2ReferenceEntrySet();
		}

		@Override
		public DoubleSet keySet() {
			if (keys == null) keys = DoubleSets.singleton(key);
			return keys;
		}

		@Override
		public ReferenceCollection<V> values() {
			if (values == null) values = ReferenceSets.singleton(value);
			return values;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public int hashCode() {
			return it.unimi.dsi.fastutil.HashCommon.double2int(key) ^ ((value) == null ? 0 : System.identityHashCode(value));
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) return true;
			if (!(o instanceof Map)) return false;
			Map<?, ?> m = (Map<?, ?>)o;
			if (m.size() != 1) return false;
			return m.entrySet().iterator().next().equals(entrySet().iterator().next());
		}

		@Override
		public String toString() {
			return "{" + key + "=>" + value + "}";
		}
	}

	/**
	 * Returns a type-specific immutable map containing only the specified pair. The returned map is
	 * serializable and cloneable.
	 *
	 * <p>
	 * Note that albeit the returned map is immutable, its default return value may be changed.
	 *
	 * @param key the only key of the returned map.
	 * @param value the only value of the returned map.
	 * @return a type-specific immutable map containing just the pair {@code &lt;key,value&gt;}.
	 */
	public static <V> Double2ReferenceMap<V> singleton(final double key, V value) {
		return new Singleton<>(key, value);
	}

	/**
	 * Returns a type-specific immutable map containing only the specified pair. The returned map is
	 * serializable and cloneable.
	 *
	 * <p>
	 * Note that albeit the returned map is immutable, its default return value may be changed.
	 *
	 * @param key the only key of the returned map.
	 * @param value the only value of the returned map.
	 * @return a type-specific immutable map containing just the pair {@code &lt;key,value&gt;}.
	 */
	public static <V> Double2ReferenceMap<V> singleton(final Double key, final V value) {
		return new Singleton<>((key).doubleValue(), (value));
	}

	/** A synchronized wrapper class for maps. */
	public static class SynchronizedMap<V> extends Double2ReferenceFunctions.SynchronizedFunction<V> implements Double2ReferenceMap<V>, java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final Double2ReferenceMap<V> map;
		protected transient ObjectSet<Double2ReferenceMap.Entry<V>> entries;
		protected transient DoubleSet keys;
		protected transient ReferenceCollection<V> values;

		protected SynchronizedMap(final Double2ReferenceMap<V> m, final Object sync) {
			super(m, sync);
			this.map = m;
		}

		protected SynchronizedMap(final Double2ReferenceMap<V> m) {
			super(m);
			this.map = m;
		}

		@Override
		public boolean containsValue(final Object v) {
			synchronized (sync) {
				return map.containsValue(v);
			}
		}

		@Override
		public void putAll(final Map<? extends Double, ? extends V> m) {
			synchronized (sync) {
				map.putAll(m);
			}
		}

		@Override
		public ObjectSet<Double2ReferenceMap.Entry<V>> double2ReferenceEntrySet() {
			synchronized (sync) {
				if (entries == null) entries = ObjectSets.synchronize(map.double2ReferenceEntrySet(), sync);
				return entries;
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ObjectSet<Map.Entry<Double, V>> entrySet() {
			return (ObjectSet)double2ReferenceEntrySet();
		}

		@Override
		public DoubleSet keySet() {
			synchronized (sync) {
				if (keys == null) keys = DoubleSets.synchronize(map.keySet(), sync);
				return keys;
			}
		}

		@Override
		public ReferenceCollection<V> values() {
			synchronized (sync) {
				if (values == null) values = ReferenceCollections.synchronize(map.values(), sync);
				return values;
			}
		}

		@Override
		public boolean isEmpty() {
			synchronized (sync) {
				return map.isEmpty();
			}
		}

		@Override
		public int hashCode() {
			synchronized (sync) {
				return map.hashCode();
			}
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) return true;
			synchronized (sync) {
				return map.equals(o);
			}
		}

		private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
			synchronized (sync) {
				s.defaultWriteObject();
			}
		}

		// Defaultable methods
		@Override
		public V getOrDefault(final double key, final V defaultValue) {
			synchronized (sync) {
				return map.getOrDefault(key, defaultValue);
			}
		}

		@Override
		public void forEach(final java.util.function.BiConsumer<? super Double, ? super V> action) {
			synchronized (sync) {
				map.forEach(action);
			}
		}

		@Override
		public void replaceAll(final java.util.function.BiFunction<? super Double, ? super V, ? extends V> function) {
			synchronized (sync) {
				map.replaceAll(function);
			}
		}

		@Override
		public V putIfAbsent(final double key, final V value) {
			synchronized (sync) {
				return map.putIfAbsent(key, value);
			}
		}

		@Override
		public boolean remove(final double key, final Object value) {
			synchronized (sync) {
				return map.remove(key, value);
			}
		}

		@Override
		public V replace(final double key, final V value) {
			synchronized (sync) {
				return map.replace(key, value);
			}
		}

		@Override
		public boolean replace(final double key, final V oldValue, final V newValue) {
			synchronized (sync) {
				return map.replace(key, oldValue, newValue);
			}
		}

		@Override
		public V computeIfAbsent(final double key, final java.util.function.DoubleFunction<? extends V> mappingFunction) {
			synchronized (sync) {
				return map.computeIfAbsent(key, mappingFunction);
			}
		}

		@Override
		public V computeIfAbsent(final double key, final Double2ReferenceFunction<? extends V> mappingFunction) {
			synchronized (sync) {
				return map.computeIfAbsent(key, mappingFunction);
			}
		}

		@Override
		public V computeIfPresent(final double key, final java.util.function.BiFunction<? super Double, ? super V, ? extends V> remappingFunction) {
			synchronized (sync) {
				return map.computeIfPresent(key, remappingFunction);
			}
		}

		@Override
		public V compute(final double key, final java.util.function.BiFunction<? super Double, ? super V, ? extends V> remappingFunction) {
			synchronized (sync) {
				return map.compute(key, remappingFunction);
			}
		}

		@Override
		public V merge(final double key, final V value, final java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
			synchronized (sync) {
				return map.merge(key, value, remappingFunction);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V getOrDefault(final Object key, final V defaultValue) {
			synchronized (sync) {
				return map.getOrDefault(key, defaultValue);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public boolean remove(final Object key, final Object value) {
			synchronized (sync) {
				return map.remove(key, value);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V replace(final Double key, final V value) {
			synchronized (sync) {
				return map.replace(key, value);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public boolean replace(final Double key, final V oldValue, final V newValue) {
			synchronized (sync) {
				return map.replace(key, oldValue, newValue);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V putIfAbsent(final Double key, final V value) {
			synchronized (sync) {
				return map.putIfAbsent(key, value);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V computeIfAbsent(final Double key, final java.util.function.Function<? super Double, ? extends V> mappingFunction) {
			synchronized (sync) {
				return map.computeIfAbsent(key, mappingFunction);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V computeIfPresent(final Double key, final java.util.function.BiFunction<? super Double, ? super V, ? extends V> remappingFunction) {
			synchronized (sync) {
				return map.computeIfPresent(key, remappingFunction);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V compute(final Double key, final java.util.function.BiFunction<? super Double, ? super V, ? extends V> remappingFunction) {
			synchronized (sync) {
				return map.compute(key, remappingFunction);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V merge(final Double key, final V value, final java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
			synchronized (sync) {
				return map.merge(key, value, remappingFunction);
			}
		}
	}

	/**
	 * Returns a synchronized type-specific map backed by the given type-specific map.
	 *
	 * @param m the map to be wrapped in a synchronized map.
	 * @return a synchronized view of the specified map.
	 * @see java.util.Collections#synchronizedMap(Map)
	 */
	public static <V> Double2ReferenceMap<V> synchronize(final Double2ReferenceMap<V> m) {
		return new SynchronizedMap<>(m);
	}

	/**
	 * Returns a synchronized type-specific map backed by the given type-specific map, using an assigned
	 * object to synchronize.
	 *
	 * @param m the map to be wrapped in a synchronized map.
	 * @param sync an object that will be used to synchronize the access to the map.
	 * @return a synchronized view of the specified map.
	 * @see java.util.Collections#synchronizedMap(Map)
	 */
	public static <V> Double2ReferenceMap<V> synchronize(final Double2ReferenceMap<V> m, final Object sync) {
		return new SynchronizedMap<>(m, sync);
	}

	/** An unmodifiable wrapper class for maps. */
	public static class UnmodifiableMap<V> extends Double2ReferenceFunctions.UnmodifiableFunction<V> implements Double2ReferenceMap<V>, java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final Double2ReferenceMap<? extends V> map;
		protected transient ObjectSet<Double2ReferenceMap.Entry<V>> entries;
		protected transient DoubleSet keys;
		protected transient ReferenceCollection<V> values;

		protected UnmodifiableMap(final Double2ReferenceMap<? extends V> m) {
			super(m);
			this.map = m;
		}

		@Override
		public boolean containsValue(final Object v) {
			return map.containsValue(v);
		}

		@Override
		public void putAll(final Map<? extends Double, ? extends V> m) {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public ObjectSet<Double2ReferenceMap.Entry<V>> double2ReferenceEntrySet() {
			if (entries == null) entries = ObjectSets.unmodifiable((ObjectSet)map.double2ReferenceEntrySet());
			return entries;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public ObjectSet<Map.Entry<Double, V>> entrySet() {
			return (ObjectSet)double2ReferenceEntrySet();
		}

		@Override
		public DoubleSet keySet() {
			if (keys == null) keys = DoubleSets.unmodifiable(map.keySet());
			return keys;
		}

		@Override
		public ReferenceCollection<V> values() {
			if (values == null) values = ReferenceCollections.unmodifiable(map.values());
			return values;
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public int hashCode() {
			return map.hashCode();
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) return true;
			return map.equals(o);
		}

		// Defaultable methods
		@Override
		@SuppressWarnings("unchecked")
		public V getOrDefault(final double key, final V defaultValue) {
			return ((Double2ReferenceMap<V>)map).getOrDefault(key, defaultValue);
		}

		@Override
		public void forEach(final java.util.function.BiConsumer<? super Double, ? super V> action) {
			map.forEach(action);
		}

		@Override
		public void replaceAll(final java.util.function.BiFunction<? super Double, ? super V, ? extends V> function) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V putIfAbsent(final double key, final V value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(final double key, final Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V replace(final double key, final V value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean replace(final double key, final V oldValue, final V newValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V computeIfAbsent(final double key, final java.util.function.DoubleFunction<? extends V> mappingFunction) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V computeIfAbsent(final double key, final Double2ReferenceFunction<? extends V> mappingFunction) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V computeIfPresent(final double key, final java.util.function.BiFunction<? super Double, ? super V, ? extends V> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V compute(final double key, final java.util.function.BiFunction<? super Double, ? super V, ? extends V> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V merge(final double key, final V value, final java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		@SuppressWarnings("unchecked")
		public V getOrDefault(final Object key, final V defaultValue) {
			return ((Double2ReferenceMap<V>)map).getOrDefault(key, defaultValue);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public boolean remove(final Object key, final Object value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V replace(final Double key, final V value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public boolean replace(final Double key, final V oldValue, final V newValue) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V putIfAbsent(final Double key, final V value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V computeIfAbsent(final Double key, final java.util.function.Function<? super Double, ? extends V> mappingFunction) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V computeIfPresent(final Double key, final java.util.function.BiFunction<? super Double, ? super V, ? extends V> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V compute(final Double key, final java.util.function.BiFunction<? super Double, ? super V, ? extends V> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V merge(final Double key, final V value, final java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Returns an unmodifiable type-specific map backed by the given type-specific map.
	 *
	 * @param m the map to be wrapped in an unmodifiable map.
	 * @return an unmodifiable view of the specified map.
	 * @see java.util.Collections#unmodifiableMap(Map)
	 */
	public static <V> Double2ReferenceMap<V> unmodifiable(final Double2ReferenceMap<? extends V> m) {
		return new UnmodifiableMap<>(m);
	}
}
