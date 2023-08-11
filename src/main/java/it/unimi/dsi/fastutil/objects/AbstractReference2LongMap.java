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

import static it.unimi.dsi.fastutil.Size64.sizeOf;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.AbstractLongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSpliterator;
import it.unimi.dsi.fastutil.longs.LongSpliterators;
import java.util.function.Consumer;
import java.util.Iterator;
import java.util.Map;

/**
 * An abstract class providing basic methods for maps implementing a type-specific interface.
 *
 * <p>
 * Optional operations just throw an {@link UnsupportedOperationException}. Generic versions of
 * accessors delegate to the corresponding type-specific counterparts following the interface rules
 * (they take care of returning {@code null} on a missing key).
 *
 * <p>
 * As a further help, this class provides a {@link BasicEntry BasicEntry} inner class that
 * implements a type-specific version of {@link java.util.Map.Entry}; it is particularly useful for
 * those classes that do not implement their own entries (e.g., most immutable maps).
 */
public abstract class AbstractReference2LongMap<K> extends AbstractReference2LongFunction<K> implements Reference2LongMap<K>, java.io.Serializable {
	private static final long serialVersionUID = -4940583368468432370L;

	protected AbstractReference2LongMap() {
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @implSpec This implementation does a linear search over the entry set, finding an entry that has
	 *           the key specified.
	 *           <p>
	 *           If you override {@link #keySet()}, you should probably override this method too to take
	 *           advantage of the (presumably) faster {@linkplain java.util.Set#contains key membership
	 *           test} your {@link #keySet()} provides.
	 *           <p>
	 *           If you override this method but not {@link #keySet()}, then the returned key set will
	 *           take advantage of this method.
	 */
	@Override
	public boolean containsKey(final Object k) {
		final ObjectIterator<Reference2LongMap.Entry<K>> i = reference2LongEntrySet().iterator();
		while (i.hasNext()) if (i.next().getKey() == k) return true;
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @implSpec This implementation does a linear search over the entry set, finding an entry that has
	 *           the value specified.
	 *           <p>
	 *           If you override {@link #values()}, you should probably override this method too to take
	 *           advantage of the (presumably) faster {@linkplain java.util.Collection#contains value
	 *           membership test} your {@link #values()} provides.
	 *           <p>
	 *           If you override this method but not {@link #values()}, then the returned values
	 *           collection will take advantage of this method.
	 */
	@Override
	public boolean containsValue(final long v) {
		final ObjectIterator<Reference2LongMap.Entry<K>> i = reference2LongEntrySet().iterator();
		while (i.hasNext()) if (i.next().getLongValue() == v) return true;
		return false;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @implSpec This method just delegates to the interface default method, as the default method, but
	 *           it is final, so it cannot be overridden.
	 */
	@Override
	public final long mergeLong(final K key, final long value, final it.unimi.dsi.fastutil.longs.LongBinaryOperator remappingFunction) {
		return mergeLong(key, value, (java.util.function.LongBinaryOperator)remappingFunction);
	}

	/**
	 * This class provides a basic but complete type-specific entry class for all those maps
	 * implementations that do not have entries on their own (e.g., most immutable maps).
	 *
	 * <p>
	 * This class does not implement {@link java.util.Map.Entry#setValue(Object) setValue()}, as the
	 * modification would not be reflected in the base map.
	 */
	public static class BasicEntry<K> implements Reference2LongMap.Entry<K> {
		protected K key;
		protected long value;

		public BasicEntry() {
		}

		public BasicEntry(final K key, final Long value) {
			this.key = (key);
			this.value = (value).longValue();
		}

		public BasicEntry(final K key, final long value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public long getLongValue() {
			return value;
		}

		@Override
		public long setValue(final long value) {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof Map.Entry)) return false;
			if (o instanceof Reference2LongMap.Entry) {
				final Reference2LongMap.Entry<K> e = (Reference2LongMap.Entry<K>)o;
				return ((key) == (e.getKey())) && ((value) == (e.getLongValue()));
			}
			final Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
			final Object key = e.getKey();
			final Object value = e.getValue();
			if (value == null || !(value instanceof Long)) return false;
			return ((this.key) == ((key))) && ((this.value) == (((Long)(value)).longValue()));
		}

		@Override
		public int hashCode() {
			return (System.identityHashCode(key)) ^ it.unimi.dsi.fastutil.HashCommon.long2int(value);
		}

		@Override
		public String toString() {
			return key + "->" + value;
		}
	}

	/**
	 * This class provides a basic implementation for an Entry set which forwards some queries to the
	 * map.
	 */
	public abstract static class BasicEntrySet<K> extends AbstractObjectSet<Entry<K>> {
		protected final Reference2LongMap<K> map;

		public BasicEntrySet(final Reference2LongMap<K> map) {
			this.map = map;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(final Object o) {
			if (!(o instanceof Map.Entry)) return false;
			if (o instanceof Reference2LongMap.Entry) {
				final Reference2LongMap.Entry<K> e = (Reference2LongMap.Entry<K>)o;
				final K k = e.getKey();
				return map.containsKey(k) && ((map.getLong(k)) == (e.getLongValue()));
			}
			final Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
			final Object k = e.getKey();
			final Object value = e.getValue();
			if (value == null || !(value instanceof Long)) return false;
			return map.containsKey(k) && ((map.getLong(k)) == (((Long)(value)).longValue()));
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(final Object o) {
			if (!(o instanceof Map.Entry)) return false;
			if (o instanceof Reference2LongMap.Entry) {
				final Reference2LongMap.Entry<K> e = (Reference2LongMap.Entry<K>)o;
				return map.remove(e.getKey(), e.getLongValue());
			}
			Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
			final Object k = e.getKey();
			final Object value = e.getValue();
			if (value == null || !(value instanceof Long)) return false;
			final long v = ((Long)(value)).longValue();
			return map.remove(k, v);
		}

		@Override
		public int size() {
			return map.size();
		}

		@Override
		public ObjectSpliterator<Entry<K>> spliterator() {
			return ObjectSpliterators.asSpliterator(iterator(), sizeOf(map), ObjectSpliterators.SET_SPLITERATOR_CHARACTERISTICS);
		}
	}

	/**
	 * Returns a type-specific-set view of the keys of this map.
	 *
	 * <p>
	 * The view is backed by the set returned by {@link Map#entrySet()}. Note that <em>no attempt is
	 * made at caching the result of this method</em>, as this would require adding some attributes that
	 * lightweight implementations would not need. Subclasses may easily override this policy by calling
	 * this method and caching the result, but implementors are encouraged to write more efficient
	 * ad-hoc implementations.
	 *
	 * @return a set view of the keys of this map; it may be safely cast to a type-specific interface.
	 */
	@Override
	public ReferenceSet<K> keySet() {
		return new AbstractReferenceSet<K>() {
			@Override
			public boolean contains(final Object k) {
				return containsKey(k);
			}

			@Override
			public int size() {
				return AbstractReference2LongMap.this.size();
			}

			@Override
			public void clear() {
				AbstractReference2LongMap.this.clear();
			}

			@Override
			public ObjectIterator<K> iterator() {
				return new ObjectIterator<K>() {
					private final ObjectIterator<Reference2LongMap.Entry<K>> i = Reference2LongMaps.fastIterator(AbstractReference2LongMap.this);

					@Override
					public K next() {
						return i.next().getKey();
					}

					@Override
					public boolean hasNext() {
						return i.hasNext();
					}

					@Override
					public void remove() {
						i.remove();
					}

					@Override
					public void forEachRemaining(final Consumer<? super K> action) {
						i.forEachRemaining(entry -> action.accept(entry.getKey()));
					}
				};
			}

			@Override
			public ObjectSpliterator<K> spliterator() {
				return ObjectSpliterators.asSpliterator(iterator(), sizeOf(AbstractReference2LongMap.this), ObjectSpliterators.SET_SPLITERATOR_CHARACTERISTICS);
			}
		};
	}

	/**
	 * Returns a type-specific-set view of the values of this map.
	 *
	 * <p>
	 * The view is backed by the set returned by {@link Map#entrySet()}. Note that <em>no attempt is
	 * made at caching the result of this method</em>, as this would require adding some attributes that
	 * lightweight implementations would not need. Subclasses may easily override this policy by calling
	 * this method and caching the result, but implementors are encouraged to write more efficient
	 * ad-hoc implementations.
	 *
	 * @return a set view of the values of this map; it may be safely cast to a type-specific interface.
	 */
	@Override
	public LongCollection values() {
		return new AbstractLongCollection() {
			@Override
			public boolean contains(final long k) {
				return containsValue(k);
			}

			@Override
			public int size() {
				return AbstractReference2LongMap.this.size();
			}

			@Override
			public void clear() {
				AbstractReference2LongMap.this.clear();
			}

			@Override
			public LongIterator iterator() {
				return new LongIterator() {
					private final ObjectIterator<Reference2LongMap.Entry<K>> i = Reference2LongMaps.fastIterator(AbstractReference2LongMap.this);

					@Override
					public long nextLong() {
						return i.next().getLongValue();
					}

					@Override
					public boolean hasNext() {
						return i.hasNext();
					}

					@Override
					public void remove() {
						i.remove();
					}

					@Override
					public void forEachRemaining(final java.util.function.LongConsumer action) {
						i.forEachRemaining(entry -> action.accept(entry.getLongValue()));
					}
				};
			}

			@Override
			public LongSpliterator spliterator() {
				return LongSpliterators.asSpliterator(iterator(), sizeOf(AbstractReference2LongMap.this), LongSpliterators.COLLECTION_SPLITERATOR_CHARACTERISTICS);
			}
		};
	}

	/** {@inheritDoc} */
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public void putAll(final Map<? extends K, ? extends Long> m) {
		if (m instanceof Reference2LongMap) {
			ObjectIterator<Reference2LongMap.Entry<K>> i = Reference2LongMaps.fastIterator((Reference2LongMap<K>)m);
			while (i.hasNext()) {
				final Reference2LongMap.Entry<? extends K> e = i.next();
				put(e.getKey(), e.getLongValue());
			}
		} else {
			int n = m.size();
			final Iterator<? extends Map.Entry<? extends K, ? extends Long>> i = m.entrySet().iterator();
			Map.Entry<? extends K, ? extends Long> e;
			while (n-- != 0) {
				e = i.next();
				put(e.getKey(), e.getValue());
			}
		}
	}

	/**
	 * Returns a hash code for this map.
	 *
	 * The hash code of a map is computed by summing the hash codes of its entries.
	 *
	 * @return a hash code for this map.
	 */
	@Override
	public int hashCode() {
		int h = 0, n = size();
		final ObjectIterator<Reference2LongMap.Entry<K>> i = Reference2LongMaps.fastIterator(this);
		while (n-- != 0) h += i.next().hashCode();
		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Map)) return false;
		final Map<?, ?> m = (Map<?, ?>)o;
		if (m.size() != size()) return false;
		return reference2LongEntrySet().containsAll(m.entrySet());
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		final ObjectIterator<Reference2LongMap.Entry<K>> i = Reference2LongMaps.fastIterator(this);
		int n = size();
		Reference2LongMap.Entry<K> e;
		boolean first = true;
		s.append("{");
		while (n-- != 0) {
			if (first) first = false;
			else s.append(", ");
			e = i.next();
			if (this == e.getKey()) s.append("(this map)");
			else s.append(String.valueOf(e.getKey()));
			s.append("=>");
			s.append(String.valueOf(e.getLongValue()));
		}
		s.append("}");
		return s.toString();
	}
}
