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
package it.unimi.dsi.fastutil.longs;

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
public interface LongPredicate extends Predicate<Long>, java.util.function.LongPredicate {
	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean test(final Long t) {
		return test(t.longValue());
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
	default LongPredicate and(final java.util.function.LongPredicate other) {
		Objects.requireNonNull(other);
		return t -> test(t) && other.test(t);
	}

	/**
	 * Returns a composed type-specific predicate that represents a short-circuiting logical AND of this
	 * type-specific predicate and another.
	 *
	 * <p>
	 * <b>WARNING</b>: Overriding this method is almost always a mistake, as this overload only exists
	 * to disambiguate. Instead, override the {@code and()} overload that uses the JDK's primitive
	 * predicate type (e.g. {@link java.util.function.IntPredicate}).
	 *
	 * <p>
	 * If Java supported final default methods, this would be one, but sadly it does not.
	 *
	 * <p>
	 * If you checked and are overriding the version with {@code java.util.function.XPredicate}, and you
	 * still see this warning, then your IDE is incorrectly conflating this method with the proper
	 * method to override, and you can safely ignore this message.
	 *
	 * @param other a predicate that will be logically-ANDed with this predicate.
	 * @return a composed predicate that represents the short-circuiting logical AND of this predicate
	 *         and the {@code other} predicate.
	 * @see Predicate#and
	 */
	default LongPredicate and(final LongPredicate other) {
		return and((java.util.function.LongPredicate)other);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Predicate<Long> and(final Predicate<? super Long> other) {
		return Predicate.super.and(other);
	}

	@Override
	/** {@inheritDoc} */
	default LongPredicate negate() {
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
	default LongPredicate or(final java.util.function.LongPredicate other) {
		Objects.requireNonNull(other);
		return t -> test(t) || other.test(t);
	}

	/**
	 * Returns a composed type-specific predicate that represents a short-circuiting logical OR of this
	 * type-specific predicate and another.
	 *
	 * <p>
	 * <b>WARNING</b>: Overriding this method is almost always a mistake, as this overload only exists
	 * to disambiguate. Instead, override the {@code or()} overload that uses the JDK's primitive
	 * predicate type (e.g. {@link java.util.function.IntPredicate}).
	 *
	 * <p>
	 * If Java supported final default methods, this would be one, but sadly it does not.
	 *
	 * <p>
	 * If you checked and are overriding the version with {@code java.util.function.XPredicate}, and you
	 * still see this warning, then your IDE is incorrectly conflating this method with the proper
	 * method to override, and you can safely ignore this message.
	 *
	 * @param other a predicate that will be logically-ORed with this predicate.
	 * @return a composed predicate that represents the short-circuiting logical OR of this predicate
	 *         and the {@code other} predicate.
	 * @see Predicate#or
	 */
	default LongPredicate or(final LongPredicate other) {
		return or((java.util.function.LongPredicate)other);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Predicate<Long> or(final Predicate<? super Long> other) {
		return Predicate.super.or(other);
	}
}
