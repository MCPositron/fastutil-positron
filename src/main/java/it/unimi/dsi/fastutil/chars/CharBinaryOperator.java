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

import java.util.function.BinaryOperator;

/**
 * A type-specific {@link BinaryOperator}; provides methods operating both on objects and on
 * primitives.
 *
 * @see BinaryOperator
 * @since 8.5.0
 */
@FunctionalInterface
public interface CharBinaryOperator extends BinaryOperator<Character>, java.util.function.IntBinaryOperator {
	/**
	 * Computes the operator on the given inputs.
	 *
	 * @param x the first input.
	 * @param y the second input.
	 * @return the output of the operator on the given inputs.
	 */
	char apply(char x, char y);

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This default implementation delegates to {@link #apply} after narrowing down the
	 *           arguments to the actual key type, throwing an exception if the arguments cannot be
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
	default int applyAsInt(final int x, final int y) {
		return apply(it.unimi.dsi.fastutil.SafeMath.safeIntToChar(x), it.unimi.dsi.fastutil.SafeMath.safeIntToChar(y));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	@SuppressWarnings("boxing")
	default Character apply(final Character x, final Character y) {
		return apply(x.charValue(), y.charValue());
	}
}
