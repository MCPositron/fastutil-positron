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
package it.unimi.dsi.fastutil.floats;

/**
 * A type-specific immutable {@link it.unimi.dsi.fastutil.Pair Pair}; provides some additional
 * methods that use polymorphism to avoid (un)boxing.
 */
public class FloatCharImmutablePair implements FloatCharPair, java.io.Serializable {
	private static final long serialVersionUID = 0L;
	protected final float left;
	protected final char right;

	/**
	 * Creates a new type-specific immutable {@link it.unimi.dsi.fastutil.Pair Pair} with given left and
	 * right value.
	 * 
	 * @param left the left value.
	 * @param right the right value.
	 */
	public FloatCharImmutablePair(final float left, final char right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * Returns a new type-specific immutable {@link it.unimi.dsi.fastutil.Pair Pair} with given left and
	 * right value.
	 * 
	 * @param left the left value.
	 * @param right the right value.
	 *
	 * @implSpec This factory method delegates to the constructor.
	 */
	public static FloatCharImmutablePair of(final float left, final char right) {
		return new FloatCharImmutablePair(left, right);
	}

	@Override
	public float leftFloat() {
		return left;
	}

	@Override
	public char rightChar() {
		return right;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other instanceof FloatCharPair) {
			return left == ((FloatCharPair)other).leftFloat() && right == ((FloatCharPair)other).rightChar();
		}
		if (other instanceof it.unimi.dsi.fastutil.Pair) {
			return java.util.Objects.equals(Float.valueOf(left), ((it.unimi.dsi.fastutil.Pair)other).left()) && java.util.Objects.equals(Character.valueOf(right), ((it.unimi.dsi.fastutil.Pair)other).right());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return it.unimi.dsi.fastutil.HashCommon.float2int(left) * 19 + (right);
	}

	/**
	 * Returns a string representation of this pair in the form &lt;<var>l</var>,<var>r</var>&gt;.
	 *
	 * @return a string representation of this pair in the form &lt;<var>l</var>,<var>r</var>&gt;.
	 */
	@Override
	public String toString() {
		return "<" + leftFloat() + "," + rightChar() + ">";
	}
}
