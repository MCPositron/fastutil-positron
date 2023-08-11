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

import java.util.Comparator;

/**
 * A type-specific {@link Comparator}; provides methods to compare two primitive types both as
 * objects and as primitive types.
 *
 * <p>
 * Note that {@code fastutil} provides a corresponding abstract class that can be used to implement
 * this interface just by specifying the type-specific comparator.
 *
 * @see Comparator
 */
@FunctionalInterface
public interface ShortComparator extends Comparator<Short> {
	/**
	 * Compares its two primitive-type arguments for order. Returns a negative integer, zero, or a
	 * positive integer as the first argument is less than, equal to, or greater than the second.
	 *
	 * @see java.util.Comparator
	 * @return a negative integer, zero, or a positive integer as the first argument is less than, equal
	 *         to, or greater than the second.
	 */
	int compare(short k1, short k2);

	@Override
	default ShortComparator reversed() {
		return ShortComparators.oppositeComparator(this);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @implSpec This implementation delegates to the corresponding type-specific method.
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default int compare(Short ok1, Short ok2) {
		return compare(ok1.shortValue(), ok2.shortValue());
	}

	/**
	 * Return a new comparator that first uses this comparator, then uses the second comparator if this
	 * comparator compared the two elements as equal.
	 *
	 * @see Comparator#thenComparing(Comparator)
	 */
	default ShortComparator thenComparing(ShortComparator second) {
		return (ShortComparator & java.io.Serializable)(k1, k2) -> {
			int comp = compare(k1, k2);
			return comp == 0 ? second.compare(k1, k2) : comp;
		};
	}

	@Override
	default Comparator<Short> thenComparing(Comparator<? super Short> second) {
		if (second instanceof ShortComparator) return thenComparing((ShortComparator)second);
		return Comparator.super.thenComparing(second);
	}
}
