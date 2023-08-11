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
package it.unimi.dsi.fastutil.longs;

import java.lang.Iterable;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A type-specific {@link Iterable} that strengthens that specification of {@link #iterator()} and
 * {@link #forEach(Consumer)}.
 *
 * <p>
 * Note that whenever there exist a primitive consumer in {@link java.util.function} (e.g.,
 * {@link java.util.function.IntConsumer}), trying to access any version of
 * {@link #forEach(Consumer)} using a lambda expression with untyped arguments will generate an
 * ambiguous method error. This can be easily solved by specifying the type of the argument, as in
 * 
 * <pre>
*    intIterable.forEach((int x) -&gt; { // Do something with x });
 * </pre>
 * <p>
 * The same problem plagues, for example,
 * {@link java.util.PrimitiveIterator.OfInt#forEachRemaining(java.util.function.IntConsumer)}.
 *
 * <p>
 * <strong>Warning</strong>: Java will let you write &ldquo;colon&rdquo; {@code for} statements with
 * primitive-type loop variables; however, what is (unfortunately) really happening is that at each
 * iteration an unboxing (and, in the case of {@code fastutil} type-specific data structures, a
 * boxing) will be performed. Watch out.
 *
 * @see Iterable
 */
public interface LongIterable extends Iterable<Long> {
	/**
	 * Returns a type-specific iterator.
	 *
	 * @apiNote Note that this specification strengthens the one given in {@link Iterable#iterator()}.
	 *
	 * @return a type-specific iterator.
	 * @see Iterable#iterator()
	 */
	@Override
	LongIterator iterator();

	/**
	 * Returns a primitive iterator on the elements of this iterable.
	 * <p>
	 *
	 * This method is identical to {@link #iterator()}, as the type-specific iterator is already
	 * compatible with the JDK's primitive iterators. It only exists for compatibility with the other
	 * primitive types' {@code Iterable}s that have use for widened iterators.
	 *
	 * @return a primitive iterator on the elements of this iterable.
	 * @since 8.5.0
	 */
	default LongIterator longIterator() {
		return iterator();
	}

	// If you change these default spliterator methods, you will likely need to update Collection, List,
	// Set, and SortedSet too.
	/**
	 * Returns a type-specific spliterator on the elements of this iterable.
	 *
	 * @apiNote Note that this specification strengthens the one given in
	 *          {@link java.lang.Iterable#spliterator()}.
	 *
	 * @return a type-specific spliterator on the elements of this iterable.
	 * @since 8.5.0
	 */
	@Override
	default LongSpliterator spliterator() {
		return LongSpliterators.asSpliteratorUnknownSize(iterator(), 0);
	}

	/**
	 * Returns a primitive spliterator on the elements of this iterable.
	 * <p>
	 *
	 * This method is identical to {@link #spliterator()}, as the type-specific spliterator is already
	 * compatible with the JDK's primitive spliterators. It only exists for compatibility with the other
	 * primitive types' {@code Iterable}s that have use for widened spliterators.
	 *
	 * @return a primitive spliterator on the elements of this collection.
	 * @since 8.5.0
	 */
	default LongSpliterator longSpliterator() {
		return spliterator();
	}

	/**
	 * Performs the given action for each element of this type-specific {@link java.lang.Iterable} until
	 * all elements have been processed or the action throws an exception.
	 *
	 * @param action the action to be performed for each element.
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 * @since 8.0.0
	 * @apiNote Implementing classes should generally override this method, and take the default
	 *          implementation of the other overloads which will delegate to this method (after proper
	 *          conversions).
	 */
	default void forEach(final java.util.function.LongConsumer action) {
		Objects.requireNonNull(action);
		iterator().forEachRemaining(action);
	}

	// Because our primitive Consumer interface extends both the JDK's primitive
	// and object Consumer interfaces, calling this method with it would be ambiguous.
	// This overload exists to pass it to the proper primitive overload.
	/**
	 * Performs the given action for each element of this type-specific {@link java.lang.Iterable} until
	 * all elements have been processed or the action throws an exception.
	 *
	 * <p>
	 * <b>WARNING</b>: Overriding this method is almost always a mistake, as this overload only exists
	 * to disambiguate. Instead, override the {@code forEach()} overload that uses the JDK's primitive
	 * consumer type (e.g. {@link java.util.function.IntConsumer}).
	 *
	 * <p>
	 * If Java supported final default methods, this would be one, but sadly it does not.
	 *
	 * <p>
	 * If you checked and are overriding the version with {@code java.util.function.XConsumer}, and
	 * still see this warning, then your IDE is incorrectly conflating this method with the proper
	 * method to override, and you can safely ignore this message.
	 *
	 * @param action the action to be performed for each element.
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 * @since 8.5.0
	 */
	default void forEach(final LongConsumer action) {
		forEach((java.util.function.LongConsumer)action);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default void forEach(final Consumer<? super Long> action) {
		Objects.requireNonNull(action);
		// The instanceof and cast is required for performance. Without it, calls routed through this
		// overload using a primitive consumer would go through the slow lambda.
		forEach(action instanceof java.util.function.LongConsumer ? (java.util.function.LongConsumer)action : (java.util.function.LongConsumer)action::accept);
	}
}
