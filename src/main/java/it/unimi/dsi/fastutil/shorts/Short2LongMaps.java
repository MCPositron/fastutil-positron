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
package it.unimi.dsi.fastutil.shorts;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterable;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongCollections;
import it.unimi.dsi.fastutil.longs.LongSets;
import java.util.Map;
import java.util.function.Consumer;
import it.unimi.dsi.fastutil.shorts.Short2LongMap.FastEntrySet;

/**
 * A class providing static methods and objects that do useful things with type-specific maps.
 *
 * @see java.util.Collections
 */
public final class Short2LongMaps {
	private Short2LongMaps() {
	}

	/**
	 * Returns an iterator that will be {@linkplain FastEntrySet fast}, if possible, on the
	 * {@linkplain Map#entrySet() entry set} of the provided {@code map}.
	 * 
	 * @param map a map from which we will try to extract a (fast) iterator on the entry set.
	 * @return an iterator on the entry set of the given map that will be fast, if possible.
	 * @since 8.0.0
	 */

	public static ObjectIterator<Short2LongMap.Entry> fastIterator(Short2LongMap map) {
		final ObjectSet<Short2LongMap.Entry> entries = map.short2LongEntrySet();
		return entries instanceof Short2LongMap.FastEntrySet ? ((Short2LongMap.FastEntrySet)entries).fastIterator() : entries.iterator();
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

	public static void fastForEach(Short2LongMap map, final Consumer<? super Short2LongMap.Entry> consumer) {
		final ObjectSet<Short2LongMap.Entry> entries = map.short2LongEntrySet();
		if (entries instanceof Short2LongMap.FastEntrySet) ((Short2LongMap.FastEntrySet)entries).fastForEach(consumer);
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

	public static ObjectIterable<Short2LongMap.Entry> fastIterable(Short2LongMap map) {
		final ObjectSet<Short2LongMap.Entry> entries = map.short2LongEntrySet();
		return entries instanceof Short2LongMap.FastEntrySet ? new ObjectIterable<Short2LongMap.Entry>() {
			@Override
			public ObjectIterator<Short2LongMap.Entry> iterator() {
				return ((Short2LongMap.FastEntrySet)entries).fastIterator();
			}

			@Override
			public ObjectSpliterator<Short2LongMap.Entry> spliterator() {
				return entries.spliterator();
			}

			@Override
			public void forEach(final Consumer<? super Short2LongMap.Entry> consumer) {
				((Short2LongMap.FastEntrySet)entries).fastForEach(consumer);
			}
		} : entries;
	}

	/**
	 * An immutable class representing an empty type-specific map.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a type-specific map.
	 */
	public static class EmptyMap extends Short2LongFunctions.EmptyFunction implements Short2LongMap, java.io.Serializable, Cloneable {
		private static final long serialVersionUID = -7046029254386353129L;

		protected EmptyMap() {
		}

		@Override
		public boolean containsValue(final long v) {
			return false;
		}

		@Deprecated
		@Override
		public Long getOrDefault(final Object key, final Long defaultValue) {
			return defaultValue;
		}

		@Override
		public long getOrDefault(final short key, final long defaultValue) {
			return defaultValue;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public boolean containsValue(final Object ov) {
			return false;
		}

		@Override
		public void putAll(final Map<? extends Short, ? extends Long> m) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ObjectSet<Short2LongMap.Entry> short2LongEntrySet() {
			return ObjectSets.EMPTY_SET;
		}

		@Override
		public ShortSet keySet() {
			return ShortSets.EMPTY_SET;
		}

		@Override
		public LongCollection values() {
			return LongSets.EMPTY_SET;
		}

		@Override
		public void forEach(final java.util.function.BiConsumer<? super Short, ? super Long> consumer) {
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

	public static final EmptyMap EMPTY_MAP = new EmptyMap();

	/**
	 * An immutable class representing a type-specific singleton map.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a type-specific map.
	 */
	public static class Singleton extends Short2LongFunctions.Singleton implements Short2LongMap, java.io.Serializable, Cloneable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected transient ObjectSet<Short2LongMap.Entry> entries;
		protected transient ShortSet keys;
		protected transient LongCollection values;

		protected Singleton(final short key, final long value) {
			super(key, value);
		}

		@Override
		public boolean containsValue(final long v) {
			return ((value) == (v));
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public boolean containsValue(final Object ov) {
			return ((((Long)(ov)).longValue()) == (value));
		}

		@Override
		public void putAll(final Map<? extends Short, ? extends Long> m) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ObjectSet<Short2LongMap.Entry> short2LongEntrySet() {
			if (entries == null) entries = ObjectSets.singleton(new AbstractShort2LongMap.BasicEntry(key, value));
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
		public ObjectSet<Map.Entry<Short, Long>> entrySet() {
			return (ObjectSet)short2LongEntrySet();
		}

		@Override
		public ShortSet keySet() {
			if (keys == null) keys = ShortSets.singleton(key);
			return keys;
		}

		@Override
		public LongCollection values() {
			if (values == null) values = LongSets.singleton(value);
			return values;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public int hashCode() {
			return (key) ^ it.unimi.dsi.fastutil.HashCommon.long2int(value);
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
	public static Short2LongMap singleton(final short key, long value) {
		return new Singleton(key, value);
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
	public static Short2LongMap singleton(final Short key, final Long value) {
		return new Singleton((key).shortValue(), (value).longValue());
	}

	/** A synchronized wrapper class for maps. */
	public static class SynchronizedMap extends Short2LongFunctions.SynchronizedFunction implements Short2LongMap, java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final Short2LongMap map;
		protected transient ObjectSet<Short2LongMap.Entry> entries;
		protected transient ShortSet keys;
		protected transient LongCollection values;

		protected SynchronizedMap(final Short2LongMap m, final Object sync) {
			super(m, sync);
			this.map = m;
		}

		protected SynchronizedMap(final Short2LongMap m) {
			super(m);
			this.map = m;
		}

		@Override
		public boolean containsValue(final long v) {
			synchronized (sync) {
				return map.containsValue(v);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public boolean containsValue(final Object ov) {
			synchronized (sync) {
				return map.containsValue(ov);
			}
		}

		@Override
		public void putAll(final Map<? extends Short, ? extends Long> m) {
			synchronized (sync) {
				map.putAll(m);
			}
		}

		@Override
		public ObjectSet<Short2LongMap.Entry> short2LongEntrySet() {
			synchronized (sync) {
				if (entries == null) entries = ObjectSets.synchronize(map.short2LongEntrySet(), sync);
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
		public ObjectSet<Map.Entry<Short, Long>> entrySet() {
			return (ObjectSet)short2LongEntrySet();
		}

		@Override
		public ShortSet keySet() {
			synchronized (sync) {
				if (keys == null) keys = ShortSets.synchronize(map.keySet(), sync);
				return keys;
			}
		}

		@Override
		public LongCollection values() {
			synchronized (sync) {
				if (values == null) values = LongCollections.synchronize(map.values(), sync);
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
		public long getOrDefault(final short key, final long defaultValue) {
			synchronized (sync) {
				return map.getOrDefault(key, defaultValue);
			}
		}

		@Override
		public void forEach(final java.util.function.BiConsumer<? super Short, ? super Long> action) {
			synchronized (sync) {
				map.forEach(action);
			}
		}

		@Override
		public void replaceAll(final java.util.function.BiFunction<? super Short, ? super Long, ? extends Long> function) {
			synchronized (sync) {
				map.replaceAll(function);
			}
		}

		@Override
		public long putIfAbsent(final short key, final long value) {
			synchronized (sync) {
				return map.putIfAbsent(key, value);
			}
		}

		@Override
		public boolean remove(final short key, final long value) {
			synchronized (sync) {
				return map.remove(key, value);
			}
		}

		@Override
		public long replace(final short key, final long value) {
			synchronized (sync) {
				return map.replace(key, value);
			}
		}

		@Override
		public boolean replace(final short key, final long oldValue, final long newValue) {
			synchronized (sync) {
				return map.replace(key, oldValue, newValue);
			}
		}

		@Override
		public long computeIfAbsent(final short key, final java.util.function.IntToLongFunction mappingFunction) {
			synchronized (sync) {
				return map.computeIfAbsent(key, mappingFunction);
			}
		}

		@Override
		public long computeIfAbsentNullable(final short key, final java.util.function.IntFunction<? extends Long> mappingFunction) {
			synchronized (sync) {
				return map.computeIfAbsentNullable(key, mappingFunction);
			}
		}

		@Override
		public long computeIfAbsent(final short key, final Short2LongFunction mappingFunction) {
			synchronized (sync) {
				return map.computeIfAbsent(key, mappingFunction);
			}
		}

		@Override
		public long computeIfPresent(final short key, final java.util.function.BiFunction<? super Short, ? super Long, ? extends Long> remappingFunction) {
			synchronized (sync) {
				return map.computeIfPresent(key, remappingFunction);
			}
		}

		@Override
		public long compute(final short key, final java.util.function.BiFunction<? super Short, ? super Long, ? extends Long> remappingFunction) {
			synchronized (sync) {
				return map.compute(key, remappingFunction);
			}
		}

		@Override
		public long merge(final short key, final long value, final java.util.function.BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
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
		public Long getOrDefault(final Object key, final Long defaultValue) {
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
		public Long replace(final Short key, final Long value) {
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
		public boolean replace(final Short key, final Long oldValue, final Long newValue) {
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
		public Long putIfAbsent(final Short key, final Long value) {
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
		public Long computeIfAbsent(final Short key, final java.util.function.Function<? super Short, ? extends Long> mappingFunction) {
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
		public Long computeIfPresent(final Short key, final java.util.function.BiFunction<? super Short, ? super Long, ? extends Long> remappingFunction) {
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
		public Long compute(final Short key, final java.util.function.BiFunction<? super Short, ? super Long, ? extends Long> remappingFunction) {
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
		public Long merge(final Short key, final Long value, final java.util.function.BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
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
	public static Short2LongMap synchronize(final Short2LongMap m) {
		return new SynchronizedMap(m);
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
	public static Short2LongMap synchronize(final Short2LongMap m, final Object sync) {
		return new SynchronizedMap(m, sync);
	}

	/** An unmodifiable wrapper class for maps. */
	public static class UnmodifiableMap extends Short2LongFunctions.UnmodifiableFunction implements Short2LongMap, java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final Short2LongMap map;
		protected transient ObjectSet<Short2LongMap.Entry> entries;
		protected transient ShortSet keys;
		protected transient LongCollection values;

		protected UnmodifiableMap(final Short2LongMap m) {
			super(m);
			this.map = m;
		}

		@Override
		public boolean containsValue(final long v) {
			return map.containsValue(v);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public boolean containsValue(final Object ov) {
			return map.containsValue(ov);
		}

		@Override
		public void putAll(final Map<? extends Short, ? extends Long> m) {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public ObjectSet<Short2LongMap.Entry> short2LongEntrySet() {
			if (entries == null) entries = ObjectSets.unmodifiable((ObjectSet)map.short2LongEntrySet());
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
		public ObjectSet<Map.Entry<Short, Long>> entrySet() {
			return (ObjectSet)short2LongEntrySet();
		}

		@Override
		public ShortSet keySet() {
			if (keys == null) keys = ShortSets.unmodifiable(map.keySet());
			return keys;
		}

		@Override
		public LongCollection values() {
			if (values == null) values = LongCollections.unmodifiable(map.values());
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
		public long getOrDefault(final short key, final long defaultValue) {
			return map.getOrDefault(key, defaultValue);
		}

		@Override
		public void forEach(final java.util.function.BiConsumer<? super Short, ? super Long> action) {
			map.forEach(action);
		}

		@Override
		public void replaceAll(final java.util.function.BiFunction<? super Short, ? super Long, ? extends Long> function) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long putIfAbsent(final short key, final long value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(final short key, final long value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long replace(final short key, final long value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean replace(final short key, final long oldValue, final long newValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long computeIfAbsent(final short key, final java.util.function.IntToLongFunction mappingFunction) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long computeIfAbsentNullable(final short key, final java.util.function.IntFunction<? extends Long> mappingFunction) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long computeIfAbsent(final short key, final Short2LongFunction mappingFunction) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long computeIfPresent(final short key, final java.util.function.BiFunction<? super Short, ? super Long, ? extends Long> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long compute(final short key, final java.util.function.BiFunction<? super Short, ? super Long, ? extends Long> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long merge(final short key, final long value, final java.util.function.BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Long getOrDefault(final Object key, final Long defaultValue) {
			return map.getOrDefault(key, defaultValue);
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
		public Long replace(final Short key, final Long value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public boolean replace(final Short key, final Long oldValue, final Long newValue) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Long putIfAbsent(final Short key, final Long value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Long computeIfAbsent(final Short key, final java.util.function.Function<? super Short, ? extends Long> mappingFunction) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Long computeIfPresent(final Short key, final java.util.function.BiFunction<? super Short, ? super Long, ? extends Long> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Long compute(final Short key, final java.util.function.BiFunction<? super Short, ? super Long, ? extends Long> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public Long merge(final Short key, final Long value, final java.util.function.BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
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
	public static Short2LongMap unmodifiable(final Short2LongMap m) {
		return new UnmodifiableMap(m);
	}
}
