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
package it.unimi.dsi.fastutil.chars;

import java.util.function.UnaryOperator;

/**
 * A type-specific {@link UnaryOperator}; provides methods operating both on objects and on
 * primitives.
 *
 * @see UnaryOperator
 * @since 8.5.0
 */
@FunctionalInterface
public interface CharUnaryOperator extends UnaryOperator<Character>, java.util.function.IntUnaryOperator {
	/**
	 * Computes the operator on the given input.
	 *
	 * @param x the input.
	 * @return the output of the operator on the given input.
	 */
	char apply(char x);

	/**
	 * Returns a {@code UnaryOperator} that always returns the input unmodified.
	 * 
	 * @see java.util.function.UnaryOperator#identity()
	 */
	public static CharUnaryOperator identity() {
		// Java is smart enough to see this lambda is stateless and will return the same instance every
		// time.
		return i -> i;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This default implementation delegates to {@link #apply} after narrowing down the
	 *           argument to the actual key type, throwing an exception if the argument cannot be
	 *           represented in the restricted domain. This is done for interoperability with the Java 8
	 *           function environment. The use of this method discouraged, as unexpected errors can
	 *           occur.
	 *
	 * @throws IllegalArgumentException If the given operands are not an element of the key domain.
	 * @since 8.5.0
	 * @deprecated Please use {@link #apply}.
	 */
	@Deprecated
	@Override
	default int applyAsInt(final int x) {
		return apply(it.unimi.dsi.fastutil.SafeMath.safeIntToChar(x));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	@SuppressWarnings("boxing")
	default Character apply(final Character x) {
		return apply(x.charValue());
	}
}
