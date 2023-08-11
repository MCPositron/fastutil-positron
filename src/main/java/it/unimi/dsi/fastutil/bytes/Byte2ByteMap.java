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
package it.unimi.dsi.fastutil.bytes;

import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.function.Consumer;
import java.util.Map;

/**
 * A type-specific {@link Map}; provides some additional methods that use polymorphism to avoid
 * (un)boxing, and handling of a default return value.
 *
 * <p>
 * Besides extending the corresponding type-specific {@linkplain it.unimi.dsi.fastutil.Function
 * function}, this interface strengthens {@link Map#entrySet()}, {@link #keySet()} and
 * {@link #values()}. Moreover, a number of methods, such as {@link #size()},
 * {@link #defaultReturnValue()}, etc., are un-defaulted as their function default do not make sense
 * for a map. Maps returning entry sets of type {@link FastEntrySet} support also fast iteration.
 *
 * <p>
 * A submap or subset may or may not have an independent default return value (which however must be
 * initialized to the default return value of the originator).
 *
 * @see Map
 */
public interface Byte2ByteMap extends Byte2ByteFunction, Map<Byte, Byte> {
	/**
	 * An entry set providing fast iteration.
	 *
	 * <p>
	 * In some cases (e.g., hash-based classes) iteration over an entry set requires the creation of a
	 * large number of {@link java.util.Map.Entry} objects. Some {@code fastutil} maps might return
	 * {@linkplain Map#entrySet() entry set} objects of type {@code FastEntrySet}: in this case,
	 * {@link #fastIterator() fastIterator()} will return an iterator that is guaranteed not to create a
	 * large number of objects, <em>possibly by returning always the same entry</em> (of course,
	 * mutated), and {@link #fastForEach(Consumer)} will apply the provided consumer to all elements of
	 * the entry set, <em>which might be represented always by the same entry</em> (of course, mutated).
	 */
	interface FastEntrySet extends ObjectSet<Byte2ByteMap.Entry> {
		/**
		 * Returns a fast iterator over this entry set; the iterator might return always the same entry
		 * instance, suitably mutated.
		 *
		 * @return a fast iterator over this entry set; the iterator might return always the same
		 *         {@link java.util.Map.Entry} instance, suitably mutated.
		 */
		ObjectIterator<Byte2ByteMap.Entry> fastIterator();

		/**
		 * Iterates quickly over this entry set; the iteration might happen always on the same entry
		 * instance, suitably mutated.
		 *
		 * <p>
		 * This default implementation just delegates to {@link #forEach(Consumer)}.
		 *
		 * @param consumer a consumer that will by applied to the entries of this set; the entries might be
		 *            represented by the same entry instance, suitably mutated.
		 * @since 8.1.0
		 */
		default void fastForEach(final Consumer<? super Byte2ByteMap.Entry> consumer) {
			forEach(consumer);
		}
	}

	/**
	 * Returns the number of key/value mappings in this map. If the map contains more than
	 * {@link Integer#MAX_VALUE} elements, returns {@link Integer#MAX_VALUE}.
	 *
	 * @return the number of key-value mappings in this map.
	 * @see it.unimi.dsi.fastutil.Size64
	 */
	@Override
	int size();

	/**
	 * Removes all of the mappings from this map (optional operation). The map will be empty after this
	 * call returns.
	 *
	 * @throws UnsupportedOperationException if the {@link #clear()} operation is not supported by this
	 *             map
	 */
	@Override
	default void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the default return value (optional operation).
	 *
	 * This value must be returned by type-specific versions of {@code get()}, {@code put()} and
	 * {@code remove()} to denote that the map does not contain the specified key. It must be
	 * 0/{@code false} by default.
	 *
	 * @param rv the new default return value.
	 * @see #defaultReturnValue()
	 */
	@Override
	void defaultReturnValue(byte rv);

	/**
	 * Gets the default return value.
	 *
	 * @return the current default return value.
	 */
	@Override
	byte defaultReturnValue();

	/**
	 * Returns a type-specific set view of the mappings contained in this map.
	 *
	 * <p>
	 * This method is necessary because there is no inheritance along type parameters: it is thus
	 * impossible to strengthen {@link Map#entrySet()} so that it returns an
	 * {@link it.unimi.dsi.fastutil.objects.ObjectSet} of type-specific entries (the latter makes it
	 * possible to access keys and values with type-specific methods).
	 *
	 * @return a type-specific set view of the mappings contained in this map.
	 * @see Map#entrySet()
	 */
	ObjectSet<Byte2ByteMap.Entry> byte2ByteEntrySet();

	/**
	 * Returns a set view of the mappings contained in this map.
	 * 
	 * @apiNote Note that this specification strengthens the one given in {@link Map#entrySet()}.
	 *
	 * @return a set view of the mappings contained in this map.
	 * @see Map#entrySet()
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default ObjectSet<Map.Entry<Byte, Byte>> entrySet() {
		return (ObjectSet)byte2ByteEntrySet();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation just delegates to the corresponding
	 * type-specific&ndash;{@linkplain it.unimi.dsi.fastutil.Function function} method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Byte put(final Byte key, final Byte value) {
		return Byte2ByteFunction.super.put(key, value);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation just delegates to the corresponding
	 * type-specific&ndash;{@linkplain it.unimi.dsi.fastutil.Function function} method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Byte get(final Object key) {
		return Byte2ByteFunction.super.get(key);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation just delegates to the corresponding
	 * type-specific&ndash;{@linkplain it.unimi.dsi.fastutil.Function function} method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Byte remove(final Object key) {
		return Byte2ByteFunction.super.remove(key);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @apiNote Note that this specification strengthens the one given in {@link Map#keySet()}.
	 * @return a set view of the keys contained in this map.
	 * @see Map#keySet()
	 */
	@Override
	ByteSet keySet();

	/**
	 * {@inheritDoc}
	 * 
	 * @apiNote Note that this specification strengthens the one given in {@link Map#values()}.
	 * @return a set view of the values contained in this map.
	 * @see Map#values()
	 */
	@Override
	ByteCollection values();

	/**
	 * Returns true if this function contains a mapping for the specified key.
	 *
	 * @param key the key.
	 * @return true if this function associates a value to {@code key}.
	 * @see Map#containsKey(Object)
	 */
	@Override
	boolean containsKey(byte key);

	/**
	 * Returns true if this function contains a mapping for the specified key.
	 * <p>
	 * This default implementation just delegates to the corresponding
	 * type-specific&ndash;{@linkplain it.unimi.dsi.fastutil.Function function} method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean containsKey(final Object key) {
		return Byte2ByteFunction.super.containsKey(key);
	}

	/**
	 * Returns {@code true} if this map maps one or more keys to the specified value.
	 * 
	 * @see Map#containsValue(Object)
	 */
	boolean containsValue(byte value);

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean containsValue(final Object value) {
		return value == null ? false : containsValue(((Byte)(value)).byteValue());
	}

	// Defaultable methods
	@Override
	default void forEach(final java.util.function.BiConsumer<? super Byte, ? super Byte> consumer) {
		final ObjectSet<Byte2ByteMap.Entry> entrySet = byte2ByteEntrySet();
		final Consumer<Byte2ByteMap.Entry> wrappingConsumer = (entry) -> consumer.accept(Byte.valueOf(entry.getByteKey()), Byte.valueOf(entry.getByteValue()));
		if (entrySet instanceof FastEntrySet) {
			((FastEntrySet)entrySet).fastForEach(wrappingConsumer);
		} else {
			entrySet.forEach(wrappingConsumer);
		}
	}

	/**
	 * Returns the value to which the specified key is mapped, or the {@code defaultValue} if this map
	 * contains no mapping for the key.
	 *
	 * @param key the key.
	 * @param defaultValue the default mapping of the key.
	 *
	 * @return the value to which the specified key is mapped, or the {@code defaultValue} if this map
	 *         contains no mapping for the key.
	 *
	 * @see java.util.Map#getOrDefault(Object, Object)
	 * @since 8.0.0
	 */
	@Override
	default byte getOrDefault(final byte key, final byte defaultValue) {
		final byte v;
		return ((v = get(key)) != defaultReturnValue() || containsKey(key)) ? v : defaultValue;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation just delegates to the corresponding {@link Map} method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Byte getOrDefault(final Object key, final Byte defaultValue) {
		return Map.super.getOrDefault(key, defaultValue);
	}

	/**
	 * If the specified key is not already associated with a value, associates it with the given value
	 * and returns the {@linkplain #defaultReturnValue() default return value}, else returns the current
	 * value.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 *
	 * @return the previous value associated with the specified key, or the
	 *         {@linkplain #defaultReturnValue() default return value} if there was no mapping for the
	 *         key.
	 *
	 * @see java.util.Map#putIfAbsent(Object, Object)
	 * @since 8.0.0
	 */
	default byte putIfAbsent(final byte key, final byte value) {
		final byte v = get(key), drv = defaultReturnValue();
		if (v != drv || containsKey(key)) return v;
		put(key, value);
		return drv;
	}

	/**
	 * Removes the entry for the specified key only if it is currently mapped to the specified value.
	 *
	 * @param key key with which the specified value is associated.
	 * @param value value expected to be associated with the specified key.
	 *
	 * @return {@code true} if the value was removed.
	 *
	 * @see java.util.Map#remove(Object, Object)
	 * @since 8.0.0
	 */
	default boolean remove(final byte key, final byte value) {
		final byte curValue = get(key);
		if (!((curValue) == (value)) || (curValue == defaultReturnValue() && !containsKey(key))) return false;
		remove(key);
		return true;
	}

	/**
	 * Replaces the entry for the specified key only if currently mapped to the specified value.
	 *
	 * @param key key with which the specified value is associated.
	 * @param oldValue value expected to be associated with the specified key.
	 * @param newValue value to be associated with the specified key.
	 *
	 * @return {@code true} if the value was replaced.
	 *
	 * @see java.util.Map#replace(Object, Object, Object)
	 * @since 8.0.0
	 */
	default boolean replace(final byte key, final byte oldValue, final byte newValue) {
		final byte curValue = get(key);
		if (!((curValue) == (oldValue)) || (curValue == defaultReturnValue() && !containsKey(key))) return false;
		put(key, newValue);
		return true;
	}

	/**
	 * Replaces the entry for the specified key only if it is currently mapped to some value.
	 *
	 * @param key key with which the specified value is associated.
	 * @param value value to be associated with the specified key.
	 *
	 * @return the previous value associated with the specified key, or the
	 *         {@linkplain #defaultReturnValue() default return value} if there was no mapping for the
	 *         key.
	 *
	 * @see java.util.Map#replace(Object, Object)
	 * @since 8.0.0
	 */
	default byte replace(final byte key, final byte value) {
		return containsKey(key) ? put(key, value) : defaultReturnValue();
	}

	/**
	 * If the specified key is not already associated with a value, attempts to compute its value using
	 * the given mapping function and enters it into this map.
	 *
	 * <p>
	 * Note that contrarily to the default
	 * {@linkplain java.util.Map#computeIfAbsent(Object, java.util.function.Function)
	 * computeIfAbsent()}, it is not possible to not add a value for a given key, since the
	 * {@code mappingFunction} cannot return {@code null}. If such a behavior is needed, please use the
	 * corresponding <em>nullable</em> version.
	 *
	 * @apiNote all {@code computeIfAbsent()} methods have a different logic based on the argument; no
	 *          delegation is performed, contrarily to other superficially similar methods such as
	 *          {@link java.util.Iterator#forEachRemaining} or {@link java.util.List#replaceAll}.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param mappingFunction the function to compute a value.
	 *
	 * @return the current (existing or computed) value associated with the specified key.
	 *
	 * @see java.util.Map#computeIfAbsent(Object, java.util.function.Function)
	 * @since 8.0.0
	 */
	default byte computeIfAbsent(final byte key, final java.util.function.IntUnaryOperator mappingFunction) {
		java.util.Objects.requireNonNull(mappingFunction);
		final byte v = get(key);
		if (v != defaultReturnValue() || containsKey(key)) return v;
		byte newValue = it.unimi.dsi.fastutil.SafeMath.safeIntToByte(mappingFunction.applyAsInt(key));
		put(key, newValue);
		return newValue;
	}

	/**
	 * If the specified key is not already associated with a value, attempts to compute its value using
	 * the given mapping function and enters it into this map unless it is {@code null}.
	 *
	 * <p>
	 * Note that this version of
	 * {@linkplain java.util.Map#computeIfAbsent(Object, java.util.function.Function) computeIfAbsent()}
	 * should be used only if you plan to return {@code null} in the mapping function.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param mappingFunction the function to compute a value.
	 *
	 * @return the current (existing or computed) value associated with the specified key, or the
	 *         {@linkplain #defaultReturnValue() default return value} if the computed value is
	 *         {@code null}.
	 *
	 * @see java.util.Map#computeIfAbsent(Object, java.util.function.Function)
	 * @since 8.0.0
	 */
	default byte computeIfAbsentNullable(final byte key, final java.util.function.IntFunction<? extends Byte> mappingFunction) {
		java.util.Objects.requireNonNull(mappingFunction);
		final byte v = get(key), drv = defaultReturnValue();
		if (v != drv || containsKey(key)) return v;
		Byte mappedValue = mappingFunction.apply(key);
		if (mappedValue == null) return drv;
		byte newValue = (mappedValue).byteValue();
		put(key, newValue);
		return newValue;
	}

	/**
	 * If the specified key is not already associated with a value, attempts to compute its value using
	 * the given mapping function and enters it into this map, unless the key is not present in the
	 * given mapping function.
	 *
	 * <p>
	 * This version of {@linkplain java.util.Map#computeIfAbsent(Object, java.util.function.Function)
	 * computeIfAbsent()} uses a type-specific version of {@code fastutil}'s
	 * {@link it.unimi.dsi.fastutil.Function Function}. Since {@link it.unimi.dsi.fastutil.Function
	 * Function} has a {@link it.unimi.dsi.fastutil.Function#containsKey(Object) containsKey()} method,
	 * it is possible to avoid adding a key by having {@code containsKey()} return {@code false} for
	 * that key.
	 *
	 * @apiNote all {@code computeIfAbsent()} methods have a different logic based on the argument; no
	 *          delegation is performed, contrarily to other superficially similar methods such as
	 *          {@link java.util.Iterator#forEachRemaining} or {@link java.util.List#replaceAll}.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param mappingFunction the function to compute a value.
	 *
	 * @return the current (existing or computed) value associated with the specified key.
	 *
	 * @see java.util.Map#computeIfAbsent(Object, java.util.function.Function)
	 * @since 8.0.0
	 */
	default byte computeIfAbsent(final byte key, final Byte2ByteFunction mappingFunction) {
		java.util.Objects.requireNonNull(mappingFunction);
		final byte v = get(key), drv = defaultReturnValue();
		if (v != drv || containsKey(key)) return v;
		if (!mappingFunction.containsKey(key)) return drv;
		byte newValue = mappingFunction.get(key);
		put(key, newValue);
		return newValue;
	}

	/**
	 * @deprecated Please use {@code computeIfAbsent()} instead.
	 */
	@Deprecated
	default byte computeIfAbsentPartial(final byte key, final Byte2ByteFunction mappingFunction) {
		return computeIfAbsent(key, mappingFunction);
	}

	/**
	 * If the value for the specified key is present, attempts to compute a new mapping given the key
	 * and its current mapped value.
	 *
	 * @apiNote The JDK specification for this method equates not being associated with a value with
	 *          being associated with {code null}. This is not the case for this method.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param remappingFunction the function to compute a value.
	 *
	 * @return the new value associated with the specified key, or the {@linkplain #defaultReturnValue()
	 *         default return value} if none.
	 *
	 * @see java.util.Map#computeIfPresent(Object, java.util.function.BiFunction)
	 * @since 8.0.0
	 */
	default byte computeIfPresent(final byte key, final java.util.function.BiFunction<? super Byte, ? super Byte, ? extends Byte> remappingFunction) {
		java.util.Objects.requireNonNull(remappingFunction);
		final byte oldValue = get(key), drv = defaultReturnValue();
		if (oldValue == drv && !containsKey(key)) return drv;
		final Byte newValue = remappingFunction.apply(Byte.valueOf(key), Byte.valueOf(oldValue));
		if (newValue == null) {
			remove(key);
			return drv;
		}
		byte newVal = (newValue).byteValue();
		put(key, newVal);
		return newVal;
	}

	/**
	 * Attempts to compute a mapping for the specified key and its current mapped value (or {@code null}
	 * if there is no current mapping).
	 *
	 * <p>
	 * If the function returns {@code null}, the mapping is removed (or remains absent if initially
	 * absent). If the function itself throws an (unchecked) exception, the exception is rethrown, and
	 * the current mapping is left unchanged.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param remappingFunction the function to compute a value.
	 *
	 * @return the new value associated with the specified key, or the {@linkplain #defaultReturnValue()
	 *         default return value} if none.
	 *
	 * @see java.util.Map#compute(Object, java.util.function.BiFunction)
	 * @since 8.0.0
	 */
	default byte compute(final byte key, final java.util.function.BiFunction<? super Byte, ? super Byte, ? extends Byte> remappingFunction) {
		java.util.Objects.requireNonNull(remappingFunction);
		final byte oldValue = get(key), drv = defaultReturnValue();
		final boolean contained = oldValue != drv || containsKey(key);
		final Byte newValue = remappingFunction.apply(Byte.valueOf(key), contained ? Byte.valueOf(oldValue) : null);
		if (newValue == null) {
			if (contained) remove(key);
			return drv;
		}
		final byte newVal = (newValue).byteValue();
		put(key, newVal);
		return newVal;
	}

	/**
	 * If the specified key is not already associated with a value, associates it with the given
	 * {@code value}. Otherwise, replaces the associated value with the results of the given remapping
	 * function, or removes if the result is {@code null}.
	 *
	 * @apiNote The JDK specification for this method equates not being associated with a value with
	 *          being associated with {code null}. This is not the case for this method.
	 *
	 * @param key key with which the resulting value is to be associated.
	 * @param value the value to be merged with the existing value associated with the key or, if no
	 *            existing value is associated with the key, to be associated with the key.
	 * @param remappingFunction the function to recompute a value if present.
	 *
	 * @return the new value associated with the specified key, or the {@linkplain #defaultReturnValue()
	 *         default return value} if no value is associated with the key.
	 *
	 * @see java.util.Map#merge(Object, Object, java.util.function.BiFunction)
	 * @since 8.0.0
	 */
	default byte merge(final byte key, final byte value, final java.util.function.BiFunction<? super Byte, ? super Byte, ? extends Byte> remappingFunction) {
		java.util.Objects.requireNonNull(remappingFunction);

		final byte oldValue = get(key), drv = defaultReturnValue();
		final byte newValue;
		if (oldValue != drv || containsKey(key)) {
			final Byte mergedValue = remappingFunction.apply(Byte.valueOf(oldValue), Byte.valueOf(value));
			if (mergedValue == null) {
				remove(key);
				return drv;
			}
			newValue = (mergedValue).byteValue();
		} else {
			newValue = value;
		}
		put(key, newValue);
		return newValue;
	}

	/**
	 * If the specified key is not already associated with a value, associates it with the given
	 * {@code value}. Otherwise, replaces the associated value with the results of the given remapping
	 * function.
	 *
	 * @apiNote The JDK specification for this method equates not being associated with a value with
	 *          being associated with {code null}. This is not the case for this method. Moreover, this
	 *          method cannot delete entries as the type-specific remapping function cannot return
	 *          {@code null}.
	 *
	 * @param key key with which the resulting value is to be associated.
	 * @param value the value to be merged with the existing value associated with the key or, if no
	 *            existing value is associated with the key, to be associated with the key.
	 * @param remappingFunction the function to recompute a value if present.
	 *
	 * @return the new value associated with the specified key.
	 *
	 * @see java.util.Map#merge(Object, Object, java.util.function.BiFunction)
	 * @since 8.5.0
	 */
	default byte mergeByte(final byte key, final byte value, final it.unimi.dsi.fastutil.bytes.ByteBinaryOperator remappingFunction) {
		java.util.Objects.requireNonNull(remappingFunction);
		final byte oldValue = get(key), drv = defaultReturnValue();
		final byte newValue = oldValue != drv || containsKey(key) ? remappingFunction.apply(oldValue, value) : value;
		put(key, newValue);
		return newValue;
	}

	/**
	 * If the specified key is not already associated with a value, associates it with the given
	 * {@code value}. Otherwise, replaces the associated value with the results of the given remapping
	 * function.
	 *
	 * @apiNote The JDK specification for this method equates not being associated with a value with
	 *          being associated with {code null}. This is not the case for this method. Moreover, this
	 *          method cannot delete entries as the type-specific remapping function cannot return
	 *          {@code null}.
	 *
	 * @param key key with which the resulting value is to be associated.
	 * @param value the value to be merged with the existing value associated with the key or, if no
	 *            existing value is associated with the key, to be associated with the key.
	 * @param remappingFunction the function to recompute a value if present.
	 *
	 * @return the new value associated with the specified key.
	 *
	 * @see java.util.Map#merge(Object, Object, java.util.function.BiFunction)
	 * @since 8.5.0
	 * @implNote Unless the argument is type-specific, this method will introduce an intermediary lambda
	 *           to perform narrowing and widening casts. Please use the type-specific overload to avoid
	 *           this overhead.
	 */
	default byte mergeByte(final byte key, final byte value, final java.util.function.IntBinaryOperator remappingFunction) {
		return mergeByte(key, value, remappingFunction instanceof it.unimi.dsi.fastutil.bytes.ByteBinaryOperator ? (it.unimi.dsi.fastutil.bytes.ByteBinaryOperator)remappingFunction : (x, y) -> it.unimi.dsi.fastutil.SafeMath.safeIntToByte(remappingFunction.applyAsInt(x, y)));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation just delegates to the corresponding {@link Map} method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Byte putIfAbsent(final Byte key, final Byte value) {
		return Map.super.putIfAbsent(key, value);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation just delegates to the corresponding {@link Map} method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean remove(final Object key, final Object value) {
		return Map.super.remove(key, value);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation just delegates to the corresponding {@link Map} method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean replace(final Byte key, final Byte oldValue, final Byte newValue) {
		return Map.super.replace(key, oldValue, newValue);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation just delegates to the corresponding {@link Map} method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Byte replace(final Byte key, final Byte value) {
		return Map.super.replace(key, value);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation just delegates to the corresponding {@link Map} method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Byte computeIfAbsent(final Byte key, final java.util.function.Function<? super Byte, ? extends Byte> mappingFunction) {
		return Map.super.computeIfAbsent(key, mappingFunction);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation just delegates to the corresponding {@link Map} method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Byte computeIfPresent(final Byte key, final java.util.function.BiFunction<? super Byte, ? super Byte, ? extends Byte> remappingFunction) {
		return Map.super.computeIfPresent(key, remappingFunction);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation just delegates to the corresponding {@link Map} method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Byte compute(final Byte key, final java.util.function.BiFunction<? super Byte, ? super Byte, ? extends Byte> remappingFunction) {
		return Map.super.compute(key, remappingFunction);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation just delegates to the corresponding {@link Map} method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Byte merge(final Byte key, final Byte value, final java.util.function.BiFunction<? super Byte, ? super Byte, ? extends Byte> remappingFunction) {
		return Map.super.merge(key, value, remappingFunction);
	}

	/**
	 * A type-specific {@link java.util.Map.Entry}; provides some additional methods that use
	 * polymorphism to avoid (un)boxing.
	 *
	 * @see java.util.Map.Entry
	 */
	interface Entry extends Map.Entry<Byte, Byte> {
		/**
		 * Returns the key corresponding to this entry.
		 * 
		 * @see java.util.Map.Entry#getKey()
		 */
		byte getByteKey();

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		default Byte getKey() {
			return Byte.valueOf(getByteKey());
		}

		/**
		 * Returns the value corresponding to this entry.
		 * 
		 * @see java.util.Map.Entry#getValue()
		 */
		byte getByteValue();

		/**
		 * Replaces the value corresponding to this entry with the specified value (optional operation).
		 * 
		 * @see java.util.Map.Entry#setValue(Object)
		 */
		byte setValue(final byte value);

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		default Byte getValue() {
			return Byte.valueOf(getByteValue());
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		default Byte setValue(final Byte value) {
			return Byte.valueOf(setValue((value).byteValue()));
		}
	}
}
