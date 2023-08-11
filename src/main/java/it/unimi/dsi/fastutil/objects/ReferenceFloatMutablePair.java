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
package it.unimi.dsi.fastutil.objects;

/**
 * A type-specific mutable {@link it.unimi.dsi.fastutil.Pair Pair}; provides some additional methods
 * that use polymorphism to avoid (un)boxing.
 */
public class ReferenceFloatMutablePair<K> implements ReferenceFloatPair<K>, java.io.Serializable {
	private static final long serialVersionUID = 0L;
	protected K left;
	protected float right;

	/**
	 * Creates a new type-specific mutable {@link it.unimi.dsi.fastutil.Pair Pair} with given left and
	 * right value.
	 * 
	 * @param left the left value.
	 * @param right the right value.
	 */
	public ReferenceFloatMutablePair(final K left, final float right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * Returns a new type-specific mutable {@link it.unimi.dsi.fastutil.Pair Pair} with given left and
	 * right value.
	 * 
	 * @param left the left value.
	 * @param right the right value.
	 *
	 * @implSpec This factory method delegates to the constructor.
	 */
	public static <K> ReferenceFloatMutablePair<K> of(final K left, final float right) {
		return new ReferenceFloatMutablePair<K>(left, right);
	}

	@Override
	public K left() {
		return left;
	}

	@Override
	public ReferenceFloatMutablePair<K> left(final K l) {
		left = l;
		return this;
	}

	@Override
	public float rightFloat() {
		return right;
	}

	@Override
	public ReferenceFloatMutablePair<K> right(final float r) {
		right = r;
		return this;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other instanceof ReferenceFloatPair) {
			return left == ((ReferenceFloatPair)other).left() && right == ((ReferenceFloatPair)other).rightFloat();
		}
		if (other instanceof it.unimi.dsi.fastutil.Pair) {
			return left == ((it.unimi.dsi.fastutil.Pair)other).left() && java.util.Objects.equals(Float.valueOf(right), ((it.unimi.dsi.fastutil.Pair)other).right());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (System.identityHashCode(left)) * 19 + it.unimi.dsi.fastutil.HashCommon.float2int(right);
	}

	/**
	 * Returns a string representation of this pair in the form &lt;<var>l</var>,<var>r</var>&gt;.
	 *
	 * @return a string representation of this pair in the form &lt;<var>l</var>,<var>r</var>&gt;.
	 */
	@Override
	public String toString() {
		return "<" + left() + "," + rightFloat() + ">";
	}
}
