/*
	* Copyright (C) 2007-2022 Sebastiano Vigna
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

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.IntArrays;

/**
 * A simple, brute-force implementation of a map based on two parallel backing arrays.
 *
 * <p>
 * The main purpose of this implementation is that of wrapping cleanly the brute-force approach to
 * the storage of a very small number of pairs: just put them into two parallel arrays and scan
 * linearly to find an item.
 */
public class Object2IntArrayMap<K> extends AbstractObject2IntMap<K> implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	/** The keys (valid up to {@link #size}, excluded). */
	private transient Object[] key;
	/** The values (parallel to {@link #key}). */
	private transient int[] value;
	/** The number of valid entries in {@link #key} and {@link #value}. */
	private int size;
	/** Cached set of entries. */
	private transient FastEntrySet<K> entries;
	/** Cached set of keys. */
	private transient ObjectSet<K> keys;
	/** Cached collection of values. */
	private transient IntCollection values;

	/**
	 * Creates a new empty array map with given key and value backing arrays. The resulting map will
	 * have as many entries as the given arrays.
	 *
	 * <p>
	 * It is responsibility of the caller that the elements of {@code key} are distinct.
	 *
	 * @param key the key array.
	 * @param value the value array (it <em>must</em> have the same length as {@code key}).
	 */
	public Object2IntArrayMap(final Object[] key, final int[] value) {
		this.key = key;
		this.value = value;
		size = key.length;
		if (key.length != value.length) throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
	}

	/**
	 * Creates a new empty array map.
	 */
	public Object2IntArrayMap() {
		this.key = ObjectArrays.EMPTY_ARRAY;
		this.value = IntArrays.EMPTY_ARRAY;
	}

	/**
	 * Creates a new empty array map of given capacity.
	 *
	 * @param capacity the initial capacity.
	 */
	public Object2IntArrayMap(final int capacity) {
		this.key = new Object[capacity];
		this.value = new int[capacity];
	}

	/**
	 * Creates a new empty array map copying the entries of a given map.
	 *
	 * @param m a map.
	 */
	public Object2IntArrayMap(final Object2IntMap<K> m) {
		this(m.size());
		int i = 0;
		for (Object2IntMap.Entry<K> e : m.object2IntEntrySet()) {
			key[i] = e.getKey();
			value[i] = e.getIntValue();
			i++;
		}
		size = i;
	}

	/**
	 * Creates a new empty array map copying the entries of a given map.
	 *
	 * @param m a map.
	 */
	public Object2IntArrayMap(final Map<? extends K, ? extends Integer> m) {
		this(m.size());
		int i = 0;
		for (Map.Entry<? extends K, ? extends Integer> e : m.entrySet()) {
			key[i] = (e.getKey());
			value[i] = (e.getValue()).intValue();
			i++;
		}
		size = i;
	}

	/**
	 * Creates a new array map with given key and value backing arrays, using the given number of
	 * elements.
	 *
	 * <p>
	 * It is responsibility of the caller that the first {@code size} elements of {@code key} are
	 * distinct.
	 *
	 * @param key the key array.
	 * @param value the value array (it <em>must</em> have the same length as {@code key}).
	 * @param size the number of valid elements in {@code key} and {@code value}.
	 */
	public Object2IntArrayMap(final Object[] key, final int[] value, final int size) {
		this.key = key;
		this.value = value;
		this.size = size;
		if (key.length != value.length) throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
		if (size > key.length) throw new IllegalArgumentException("The provided size (" + size + ") is larger than or equal to the backing-arrays size (" + key.length + ")");
	}

	private final class EntrySet extends AbstractObjectSet<Object2IntMap.Entry<K>> implements FastEntrySet<K> {
		// TODO Maybe make this return a list-iterator like the LinkedXHashMaps do? (same for other
		// collection view types)
		@Override
		public ObjectIterator<Object2IntMap.Entry<K>> iterator() {
			return new ObjectIterator<Object2IntMap.Entry<K>>() {
				int curr = -1, next = 0;

				@Override
				public boolean hasNext() {
					return next < size;
				}

				@Override
				@SuppressWarnings("unchecked")
				public Entry<K> next() {
					if (!hasNext()) throw new NoSuchElementException();
					return new AbstractObject2IntMap.BasicEntry<>((K)key[curr = next], value[next++]);
				}

				@Override
				public void remove() {
					if (curr == -1) throw new IllegalStateException();
					curr = -1;
					final int tail = size-- - next--;
					System.arraycopy(key, next + 1, key, next, tail);
					System.arraycopy(value, next + 1, value, next, tail);
					key[size] = null;
				}

				@Override
				@SuppressWarnings("unchecked")
				public void forEachRemaining(final Consumer<? super Object2IntMap.Entry<K>> action) {
					// Hoist containing class field ref into local
					final int max = size;
					while (next < max) {
						action.accept(new AbstractObject2IntMap.BasicEntry<>((K)key[curr = next], value[next++]));
					}
				}
			};
		}

		@Override
		public ObjectIterator<Object2IntMap.Entry<K>> fastIterator() {
			return new ObjectIterator<Object2IntMap.Entry<K>>() {
				int next = 0, curr = -1;
				final BasicEntry<K> entry = new BasicEntry<>();

				@Override
				public boolean hasNext() {
					return next < size;
				}

				@Override
				@SuppressWarnings("unchecked")
				public Entry<K> next() {
					if (!hasNext()) throw new NoSuchElementException();
					entry.key = (K)key[curr = next];
					entry.value = value[next++];
					return entry;
				}

				@Override
				public void remove() {
					if (curr == -1) throw new IllegalStateException();
					curr = -1;
					final int tail = size-- - next--;
					System.arraycopy(key, next + 1, key, next, tail);
					System.arraycopy(value, next + 1, value, next, tail);
					key[size] = null;
				}

				@Override
				@SuppressWarnings("unchecked")
				public void forEachRemaining(final Consumer<? super Object2IntMap.Entry<K>> action) {
					// Hoist containing class field ref into local
					final int max = size;
					while (next < max) {
						entry.key = (K)key[curr = next];
						entry.value = value[next++];
						action.accept(entry);
					}
				}
			};
		}

		// We already have to create an Entry object for each iteration, so the overhead from having
		// skeletal implementations isn't significant.
		final class EntrySetSpliterator extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Object2IntMap.Entry<K>> implements ObjectSpliterator<Object2IntMap.Entry<K>> {
			EntrySetSpliterator(int pos, int maxPos) {
				super(pos, maxPos);
			}

			@Override
			public int characteristics() {
				return ObjectSpliterators.SET_SPLITERATOR_CHARACTERISTICS | java.util.Spliterator.SUBSIZED | java.util.Spliterator.ORDERED;
			}

			@Override
			@SuppressWarnings("unchecked")
			protected final Object2IntMap.Entry<K> get(int location) {
				return new AbstractObject2IntMap.BasicEntry<>((K)key[location], value[location]);
			}

			@Override
			protected final EntrySetSpliterator makeForSplit(int pos, int maxPos) {
				return new EntrySetSpliterator(pos, maxPos);
			}
		}

		@Override
		public ObjectSpliterator<Object2IntMap.Entry<K>> spliterator() {
			return new EntrySetSpliterator(0, size);
		}

		/** {@inheritDoc} */
		@Override
		@SuppressWarnings("unchecked")
		public void forEach(final Consumer<? super Object2IntMap.Entry<K>> action) {
			// Hoist containing class field ref into local
			for (int i = 0, max = size; i < max; ++i) {
				action.accept(new AbstractObject2IntMap.BasicEntry<>((K)key[i], value[i]));
			}
		}

		/** {@inheritDoc} */
		@Override
		@SuppressWarnings("unchecked")
		public void fastForEach(final Consumer<? super Object2IntMap.Entry<K>> action) {
			final BasicEntry<K> entry = new BasicEntry<>();
			// Hoist containing class field ref into local
			for (int i = 0, max = size; i < max; ++i) {
				entry.key = (K)key[i];
				entry.value = value[i];
				action.accept(entry);
			}
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry)) return false;
			final Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
			if (e.getValue() == null || !(e.getValue() instanceof Integer)) return false;
			final K k = ((K)e.getKey());
			return Object2IntArrayMap.this.containsKey(k) && ((Object2IntArrayMap.this.getInt(k)) == (((Integer)(e.getValue())).intValue()));
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean remove(final Object o) {
			if (!(o instanceof Map.Entry)) return false;
			final Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
			if (e.getValue() == null || !(e.getValue() instanceof Integer)) return false;
			final K k = ((K)e.getKey());
			final int v = ((Integer)(e.getValue())).intValue();
			final int oldPos = Object2IntArrayMap.this.findKey(k);
			if (oldPos == -1 || !((v) == (Object2IntArrayMap.this.value[oldPos]))) return false;
			final int tail = size - oldPos - 1;
			System.arraycopy(Object2IntArrayMap.this.key, oldPos + 1, Object2IntArrayMap.this.key, oldPos, tail);
			System.arraycopy(Object2IntArrayMap.this.value, oldPos + 1, Object2IntArrayMap.this.value, oldPos, tail);
			Object2IntArrayMap.this.size--;
			Object2IntArrayMap.this.key[size] = null;
			return true;
		}
	}

	@Override
	public FastEntrySet<K> object2IntEntrySet() {
		if (entries == null) entries = new EntrySet();
		return entries;
	}

	private int findKey(final Object k) {
		final Object[] key = this.key;
		for (int i = size; i-- != 0;) if (java.util.Objects.equals(key[i], k)) return i;
		return -1;
	}

	@Override

	public int getInt(final Object k) {
		final Object[] key = this.key;
		for (int i = size; i-- != 0;) if (java.util.Objects.equals(key[i], k)) return value[i];
		return defRetValue;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		for (int i = size; i-- != 0;) {
			key[i] = null;
		}
		size = 0;
	}

	@Override
	public boolean containsKey(final Object k) {
		return findKey(k) != -1;
	}

	@Override
	public boolean containsValue(int v) {
		for (int i = size; i-- != 0;) if (((value[i]) == (v))) return true;
		return false;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override

	public int put(K k, int v) {
		final int oldKey = findKey(k);
		if (oldKey != -1) {
			final int oldValue = value[oldKey];
			value[oldKey] = v;
			return oldValue;
		}
		if (size == key.length) {
			final Object[] newKey = new Object[size == 0 ? 2 : size * 2];
			final int[] newValue = new int[size == 0 ? 2 : size * 2];
			for (int i = size; i-- != 0;) {
				newKey[i] = key[i];
				newValue[i] = value[i];
			}
			key = newKey;
			value = newValue;
		}
		key[size] = k;
		value[size] = v;
		size++;
		return defRetValue;
	}

	@Override

	public int removeInt(final Object k) {
		final int oldPos = findKey(k);
		if (oldPos == -1) return defRetValue;
		final int oldValue = value[oldPos];
		final int tail = size - oldPos - 1;
		System.arraycopy(key, oldPos + 1, key, oldPos, tail);
		System.arraycopy(value, oldPos + 1, value, oldPos, tail);
		size--;
		key[size] = null;
		return oldValue;
	}

	private final class KeySet extends AbstractObjectSet<K> {
		@Override
		public boolean contains(final Object k) {
			return findKey(k) != -1;
		}

		@Override
		public boolean remove(final Object k) {
			final int oldPos = findKey(k);
			if (oldPos == -1) return false;
			final int tail = size - oldPos - 1;
			System.arraycopy(key, oldPos + 1, key, oldPos, tail);
			System.arraycopy(value, oldPos + 1, value, oldPos, tail);
			size--;
			Object2IntArrayMap.this.key[size] = null;
			return true;
		}

		@Override
		public ObjectIterator<K> iterator() {
			return new ObjectIterator<K>() {
				int pos = 0;

				@Override
				public boolean hasNext() {
					return pos < size;
				}

				@Override
				@SuppressWarnings("unchecked")
				public K next() {
					if (!hasNext()) throw new NoSuchElementException();
					return (K)key[pos++];
				}

				@Override
				public void remove() {
					if (pos == 0) throw new IllegalStateException();
					final int tail = size - pos;
					System.arraycopy(key, pos, key, pos - 1, tail);
					System.arraycopy(value, pos, value, pos - 1, tail);
					size--;
					pos--;
					Object2IntArrayMap.this.key[size] = null;
				}

				@Override
				@SuppressWarnings("unchecked")
				public void forEachRemaining(final Consumer<? super K> action) {
					// Hoist containing class field ref into local
					final int max = size;
					while (pos < max) {
						action.accept((K)key[pos++]);
					}
				}
				// TODO either override skip or extend from AbstractIndexBasedIterator.
			};
		}

		final class KeySetSpliterator extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<K> implements ObjectSpliterator<K> {
			KeySetSpliterator(int pos, int maxPos) {
				super(pos, maxPos);
			}

			@Override
			public int characteristics() {
				return ObjectSpliterators.SET_SPLITERATOR_CHARACTERISTICS | java.util.Spliterator.SUBSIZED | java.util.Spliterator.ORDERED;
			}

			@Override
			@SuppressWarnings("unchecked")
			protected final K get(int location) {
				return (K)key[location];
			}

			@Override
			protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
				return new KeySetSpliterator(pos, maxPos);
			}

			@Override
			@SuppressWarnings("unchecked")
			public void forEachRemaining(final Consumer<? super K> action) {
				// Hoist containing class field ref into local
				final int max = size;
				while (pos < max) {
					action.accept((K)key[pos++]);
				}
			}
		}

		@Override
		public ObjectSpliterator<K> spliterator() {
			return new KeySetSpliterator(0, size);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void forEach(Consumer<? super K> action) {
			// Hoist containing class field ref into local
			for (int i = 0, max = size; i < max; ++i) {
				action.accept((K)key[i]);
			}
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public void clear() {
			Object2IntArrayMap.this.clear();
		}
	}

	@Override
	public ObjectSet<K> keySet() {
		if (keys == null) keys = new KeySet();
		return keys;
	}

	private final class ValuesCollection extends AbstractIntCollection {
		@Override
		public boolean contains(final int v) {
			return containsValue(v);
		}

		@Override
		public it.unimi.dsi.fastutil.ints.IntIterator iterator() {
			return new it.unimi.dsi.fastutil.ints.IntIterator() {
				int pos = 0;

				@Override
				public boolean hasNext() {
					return pos < size;
				}

				@Override

				public int nextInt() {
					if (!hasNext()) throw new NoSuchElementException();
					return value[pos++];
				}

				@Override
				public void remove() {
					if (pos == 0) throw new IllegalStateException();
					final int tail = size - pos;
					System.arraycopy(key, pos, key, pos - 1, tail);
					System.arraycopy(value, pos, value, pos - 1, tail);
					size--;
					pos--;
					Object2IntArrayMap.this.key[size] = null;
				}

				@Override

				public void forEachRemaining(final java.util.function.IntConsumer action) {
					// Hoist containing class field ref into local
					final int max = size;
					while (pos < max) {
						action.accept(value[pos++]);
					}
				}
				// TODO either override skip or extend from AbstractIndexBasedIterator.
			};
		}

		final class ValuesSpliterator extends it.unimi.dsi.fastutil.ints.IntSpliterators.EarlyBindingSizeIndexBasedSpliterator implements it.unimi.dsi.fastutil.ints.IntSpliterator {
			ValuesSpliterator(int pos, int maxPos) {
				super(pos, maxPos);
			}

			@Override
			public int characteristics() {
				return it.unimi.dsi.fastutil.ints.IntSpliterators.COLLECTION_SPLITERATOR_CHARACTERISTICS | java.util.Spliterator.SUBSIZED | java.util.Spliterator.ORDERED;
			}

			@Override

			protected final int get(int location) {
				return value[location];
			}

			@Override
			protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
				return new ValuesSpliterator(pos, maxPos);
			}

			@Override

			public void forEachRemaining(final java.util.function.IntConsumer action) {
				// Hoist containing class field ref into local
				final int max = size;
				while (pos < max) {
					action.accept(value[pos++]);
				}
			}
		}

		@Override
		public it.unimi.dsi.fastutil.ints.IntSpliterator spliterator() {
			return new ValuesSpliterator(0, size);
		}

		@Override

		public void forEach(java.util.function.IntConsumer action) {
			// Hoist containing class field ref into local
			for (int i = 0, max = size; i < max; ++i) {
				action.accept(value[i]);
			}
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public void clear() {
			Object2IntArrayMap.this.clear();
		}
	}

	@Override
	public IntCollection values() {
		if (values == null) values = new ValuesCollection();
		return values;
	}

	/**
	 * Returns a deep copy of this map.
	 *
	 * <p>
	 * This method performs a deep copy of this hash map; the data stored in the map, however, is not
	 * cloned. Note that this makes a difference only for object keys.
	 *
	 * @return a deep copy of this map.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object2IntArrayMap<K> clone() {
		Object2IntArrayMap<K> c;
		try {
			c = (Object2IntArrayMap<K>)super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new InternalError();
		}
		c.key = key.clone();
		c.value = value.clone();
		c.entries = null;
		c.keys = null;
		c.values = null;
		return c;
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		s.defaultWriteObject();
		for (int i = 0, max = size; i < max; i++) {
			s.writeObject(key[i]);
			s.writeInt(value[i]);
		}
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		key = new Object[size];
		value = new int[size];
		for (int i = 0; i < size; i++) {
			key[i] = s.readObject();
			value[i] = s.readInt();
		}
	}
}
