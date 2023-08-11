/*
	* Copyright (C) 2020-2022 Sebastiano Vigna
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

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A type-specific {@link Predicate}; provides methods to test a primitive type both as object and
 * as primitive.
 *
 * <p>
 * Except for the boolean case, this interface extends both a parameterized
 * {@link java.util.function.Predicate} and a type-specific JDK predicate (e.g.,
 * {@link java.util.function.IntPredicate}). For types missing a type-specific JDK predicate (e.g.,
 * {@code short} or {@code float}), we extend the predicate associated with the smallest primitive
 * type that can represent the current type (e.g., {@code int} or {@code double}, respectively).
 *
 * @see Predicate
 * @since 8.5.0
 */
@FunctionalInterface
public interface ShortPredicate extends Predicate<Short>, java.util.function.IntPredicate {
	/**
	 * Evaluates this predicate on the given input.
	 *
	 * @param t the input.
	 * @return {@code true} if the input matches the predicate, otherwise {@code false}
	 */
	boolean test(short t);

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean test(final int t) {
		return test(it.unimi.dsi.fastutil.SafeMath.safeIntToShort(t));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean test(final Short t) {
		return test(t.shortValue());
	}

	/**
	 * Returns a composed type-specific predicate that represents a short-circuiting logical AND of this
	 * type-specific predicate and another.
	 * 
	 * @param other a predicate that will be logically-ANDed with this predicate.
	 * @return a composed predicate that represents the short-circuiting logical AND of this predicate
	 *         and the {@code other} predicate.
	 * @see Predicate#and
	 * @apiNote Implementing classes should generally override this method and keep the default
	 *          implementation of the other overloads, which will delegate to this method (after proper
	 *          conversions).
	 */
	default ShortPredicate and(final ShortPredicate other) {
		Objects.requireNonNull(other);
		return t -> test(t) && other.test(t);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @implNote Composing with a JDK type-specific predicate will be slightly less efficient than using
	 *           a type-specific predicate, as the argument will have to be widened at each call.
	 */
	@Override
	default ShortPredicate and(final java.util.function.IntPredicate other) {
		return and(other instanceof ShortPredicate ? (ShortPredicate)other : (ShortPredicate)other::test);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Predicate<Short> and(final Predicate<? super Short> other) {
		return Predicate.super.and(other);
	}

	@Override
	/** {@inheritDoc} */
	default ShortPredicate negate() {
		return t -> !test(t);
	}

	/**
	 * Returns a composed type-specific predicate that represents a short-circuiting logical OR of this
	 * type-specific predicate and another.
	 * 
	 * @param other a predicate that will be logically-ORed with this predicate.
	 * @return a composed predicate that represents the short-circuiting logical OR of this predicate
	 *         and the {@code other} predicate.
	 * @see Predicate#or
	 * @apiNote Implementing classes should generally override this method and keep the default
	 *          implementation of the other overloads, which will delegate to this method (after proper
	 *          conversions).
	 */
	default ShortPredicate or(final ShortPredicate other) {
		Objects.requireNonNull(other);
		return t -> test(t) || other.test(t);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @implNote Composing with a JDK type-specific predicate will be slightly less efficient than using
	 *           a type-specific predicate, as the argument will have to be widened at each call.
	 */
	@Override
	default ShortPredicate or(final java.util.function.IntPredicate other) {
		return or(other instanceof ShortPredicate ? (ShortPredicate)other : (ShortPredicate)other::test);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Predicate<Short> or(final Predicate<? super Short> other) {
		return Predicate.super.or(other);
	}
}
