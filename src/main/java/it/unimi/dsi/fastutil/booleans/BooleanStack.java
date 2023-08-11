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
package it.unimi.dsi.fastutil.booleans;

import it.unimi.dsi.fastutil.Stack;

/**
 * A type-specific {@link Stack}; provides some additional methods that use polymorphism to avoid
 * (un)boxing.
 */
public interface BooleanStack extends Stack<Boolean> {
	/**
	 * Pushes the given object on the stack.
	 * 
	 * @param k the object to push on the stack.
	 * @see Stack#push(Object)
	 */
	void push(boolean k);

	/**
	 * Pops the top off the stack.
	 *
	 * @return the top of the stack.
	 * @see Stack#pop()
	 */
	boolean popBoolean();

	/**
	 * Peeks at the top of the stack (optional operation).
	 * 
	 * @return the top of the stack.
	 * @see Stack#top()
	 */
	boolean topBoolean();

	/**
	 * Peeks at an element on the stack (optional operation).
	 * 
	 * @param i an index from the stop of the stack (0 represents the top).
	 * @return the {@code i}-th element on the stack.
	 * @see Stack#peek(int)
	 */
	boolean peekBoolean(int i);

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default void push(Boolean o) {
		push(o.booleanValue());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Boolean pop() {
		return Boolean.valueOf(popBoolean());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Boolean top() {
		return Boolean.valueOf(topBoolean());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Boolean peek(final int i) {
		return Boolean.valueOf(peekBoolean(i));
	}
}
