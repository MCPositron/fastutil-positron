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
package it.unimi.dsi.fastutil.ints;

import it.unimi.dsi.fastutil.objects.AbstractObjectSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.AbstractFloatCollection;
import it.unimi.dsi.fastutil.floats.FloatIterator;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.NoSuchElementException;

/**
 * A type-specific red-black tree map with a fast, small-footprint implementation.
 *
 * <p>
 * The iterators provided by the views of this class are type-specific
 * {@linkplain it.unimi.dsi.fastutil.BidirectionalIterator bidirectional iterators}. Moreover, the
 * iterator returned by {@code iterator()} can be safely cast to a type-specific
 * {@linkplain java.util.ListIterator list iterator}.
 *
 */
public class Int2FloatRBTreeMap extends AbstractInt2FloatSortedMap implements java.io.Serializable, Cloneable {
	/** A reference to the root entry. */
	protected transient Entry tree;
	/** Number of entries in this map. */
	protected int count;
	/** The first key in this map. */
	protected transient Entry firstEntry;
	/** The last key in this map. */
	protected transient Entry lastEntry;
	/** Cached set of entries. */
	protected transient ObjectSortedSet<Int2FloatMap.Entry> entries;
	/** Cached set of keys. */
	protected transient IntSortedSet keys;
	/** Cached collection of values. */
	protected transient FloatCollection values;
	/**
	 * The value of this variable remembers, after a {@code put()} or a {@code remove()}, whether the
	 * <em>domain</em> of the map has been modified.
	 */
	protected transient boolean modified;
	/** This map's comparator, as provided in the constructor. */
	protected Comparator<? super Integer> storedComparator;
	/**
	 * This map's actual comparator; it may differ from {@link #storedComparator} because it is always a
	 * type-specific comparator, so it could be derived from the former by wrapping.
	 */
	protected transient IntComparator actualComparator;
	private static final long serialVersionUID = -7046029254386353129L;
	{
		allocatePaths();
	}

	/**
	 * Creates a new empty tree map.
	 */
	public Int2FloatRBTreeMap() {
		tree = null;
		count = 0;
	}

	/**
	 * Generates the comparator that will be actually used.
	 *
	 * <p>
	 * When a given {@link Comparator} is specified and stored in {@link #storedComparator}, we must
	 * check whether it is type-specific. If it is so, we can used directly, and we store it in
	 * {@link #actualComparator}. Otherwise, we adapt it using a helper static method.
	 */
	private void setActualComparator() {
		actualComparator = IntComparators.asIntComparator(storedComparator);
	}

	/**
	 * Creates a new empty tree map with the given comparator.
	 *
	 * @param c a (possibly type-specific) comparator.
	 */
	public Int2FloatRBTreeMap(final Comparator<? super Integer> c) {
		this();
		storedComparator = c;
		setActualComparator();
	}

	/**
	 * Creates a new tree map copying a given map.
	 *
	 * @param m a {@link Map} to be copied into the new tree map.
	 */
	public Int2FloatRBTreeMap(final Map<? extends Integer, ? extends Float> m) {
		this();
		putAll(m);
	}

	/**
	 * Creates a new tree map copying a given sorted map (and its {@link Comparator}).
	 *
	 * @param m a {@link SortedMap} to be copied into the new tree map.
	 */
	public Int2FloatRBTreeMap(final SortedMap<Integer, Float> m) {
		this(m.comparator());
		putAll(m);
	}

	/**
	 * Creates a new tree map copying a given map.
	 *
	 * @param m a type-specific map to be copied into the new tree map.
	 */
	public Int2FloatRBTreeMap(final Int2FloatMap m) {
		this();
		putAll(m);
	}

	/**
	 * Creates a new tree map copying a given sorted map (and its {@link Comparator}).
	 *
	 * @param m a type-specific sorted map to be copied into the new tree map.
	 */
	public Int2FloatRBTreeMap(final Int2FloatSortedMap m) {
		this(m.comparator());
		putAll(m);
	}

	/**
	 * Creates a new tree map using the elements of two parallel arrays and the given comparator.
	 *
	 * @param k the array of keys of the new tree map.
	 * @param v the array of corresponding values in the new tree map.
	 * @param c a (possibly type-specific) comparator.
	 * @throws IllegalArgumentException if {@code k} and {@code v} have different lengths.
	 */
	public Int2FloatRBTreeMap(final int[] k, final float v[], final Comparator<? super Integer> c) {
		this(c);
		if (k.length != v.length) throw new IllegalArgumentException("The key array and the value array have different lengths (" + k.length + " and " + v.length + ")");
		for (int i = 0; i < k.length; i++) this.put(k[i], v[i]);
	}

	/**
	 * Creates a new tree map using the elements of two parallel arrays.
	 *
	 * @param k the array of keys of the new tree map.
	 * @param v the array of corresponding values in the new tree map.
	 * @throws IllegalArgumentException if {@code k} and {@code v} have different lengths.
	 */
	public Int2FloatRBTreeMap(final int[] k, final float v[]) {
		this(k, v, null);
	}

	/*
	 * The following methods implements some basic building blocks used by
	 * all accessors.  They are (and should be maintained) identical to those used in RBTreeSet.drv.
	 *
	 * The put()/remove() code is derived from Ben Pfaff's GNU libavl
	 * (https://adtinfo.org/). If you want to understand what's
	 * going on, you should have a look at the literate code contained therein
	 * first.
	 */
	/**
	 * Compares two keys in the right way.
	 *
	 * <p>
	 * This method uses the {@link #actualComparator} if it is non-{@code null}. Otherwise, it resorts
	 * to primitive type comparisons or to {@link Comparable#compareTo(Object) compareTo()}.
	 *
	 * @param k1 the first key.
	 * @param k2 the second key.
	 * @return a number smaller than, equal to or greater than 0, as usual (i.e., when k1 &lt; k2, k1 =
	 *         k2 or k1 &gt; k2, respectively).
	 */

	final int compare(final int k1, final int k2) {
		return actualComparator == null ? (Integer.compare((k1), (k2))) : actualComparator.compare(k1, k2);
	}

	/**
	 * Returns the entry corresponding to the given key, if it is in the tree; {@code null}, otherwise.
	 *
	 * @param k the key to search for.
	 * @return the corresponding entry, or {@code null} if no entry with the given key exists.
	 */
	final Entry findKey(final int k) {
		Entry e = tree;
		int cmp;
		while (e != null && (cmp = compare(k, e.key)) != 0) e = cmp < 0 ? e.left() : e.right();
		return e;
	}

	/**
	 * Locates a key.
	 *
	 * @param k a key.
	 * @return the last entry on a search for the given key; this will be the given key, if it present;
	 *         otherwise, it will be either the smallest greater key or the greatest smaller key.
	 */
	final Entry locateKey(final int k) {
		Entry e = tree, last = tree;
		int cmp = 0;
		while (e != null && (cmp = compare(k, e.key)) != 0) {
			last = e;
			e = cmp < 0 ? e.left() : e.right();
		}
		return cmp == 0 ? e : last;
	}

	/**
	 * This vector remembers the path and the direction followed during the current insertion. It
	 * suffices for about 2<sup>32</sup> entries.
	 */
	private transient boolean dirPath[];
	private transient Entry nodePath[];

	private void allocatePaths() {
		dirPath = new boolean[64];
		nodePath = new Entry[64];
	}

	/**
	 * Adds an increment to value currently associated with a key.
	 *
	 * <p>
	 * Note that this method respects the {@linkplain #defaultReturnValue() default return value}
	 * semantics: when called with a key that does not currently appears in the map, the key will be
	 * associated with the default return value plus the given increment.
	 *
	 * @param k the key.
	 * @param incr the increment.
	 * @return the old value, or the {@linkplain #defaultReturnValue() default return value} if no value
	 *         was present for the given key.
	 */
	public float addTo(final int k, final float incr) {
		Entry e = add(k);
		final float oldValue = e.value;
		e.value += incr;
		return oldValue;
	}

	@Override
	public float put(final int k, final float v) {
		Entry e = add(k);
		final float oldValue = e.value;
		e.value = v;
		return oldValue;
	}

	/**
	 * Returns a node with key k in the balanced tree, creating one with defRetValue if necessary.
	 *
	 * @param k the key
	 * @return a node with key k. If a node with key k already exists, then that node is returned,
	 *         otherwise a new node with defRetValue is created ensuring that the tree is balanced after
	 *         creation of the node.
	 */
	private Entry add(final int k) {
		/* After execution of this method, modified is true iff a new entry has been inserted. */

		modified = false;
		int maxDepth = 0;
		Entry e;
		if (tree == null) { // The case of the empty tree is treated separately.
			count++;
			e = tree = lastEntry = firstEntry = new Entry(k, defRetValue);
		} else {
			Entry p = tree;
			int cmp, i = 0;
			while (true) {
				if ((cmp = compare(k, p.key)) == 0) {
					// We clean up the node path, or we could have stale references later.
					while (i-- != 0) nodePath[i] = null;
					return p;
				}
				nodePath[i] = p;
				if (dirPath[i++] = cmp > 0) {
					if (p.succ()) {
						count++;
						e = new Entry(k, defRetValue);
						if (p.right == null) lastEntry = e;
						e.left = p;
						e.right = p.right;
						p.right(e);
						break;
					}
					p = p.right;
				} else {
					if (p.pred()) {
						count++;
						e = new Entry(k, defRetValue);
						if (p.left == null) firstEntry = e;
						e.right = p;
						e.left = p.left;
						p.left(e);
						break;
					}
					p = p.left;
				}
			}
			modified = true;
			maxDepth = i--;
			while (i > 0 && !nodePath[i].black()) {
				if (!dirPath[i - 1]) {
					Entry y = nodePath[i - 1].right;
					if (!nodePath[i - 1].succ() && !y.black()) {
						nodePath[i].black(true);
						y.black(true);
						nodePath[i - 1].black(false);
						i -= 2;
					} else {
						Entry x;
						if (!dirPath[i]) y = nodePath[i];
						else {
							x = nodePath[i];
							y = x.right;
							x.right = y.left;
							y.left = x;
							nodePath[i - 1].left = y;
							if (y.pred()) {
								y.pred(false);
								x.succ(y);
							}
						}
						x = nodePath[i - 1];
						x.black(false);
						y.black(true);
						x.left = y.right;
						y.right = x;
						if (i < 2) tree = y;
						else {
							if (dirPath[i - 2]) nodePath[i - 2].right = y;
							else nodePath[i - 2].left = y;
						}
						if (y.succ()) {
							y.succ(false);
							x.pred(y);
						}
						break;
					}
				} else {
					Entry y = nodePath[i - 1].left;
					if (!nodePath[i - 1].pred() && !y.black()) {
						nodePath[i].black(true);
						y.black(true);
						nodePath[i - 1].black(false);
						i -= 2;
					} else {
						Entry x;
						if (dirPath[i]) y = nodePath[i];
						else {
							x = nodePath[i];
							y = x.left;
							x.left = y.right;
							y.right = x;
							nodePath[i - 1].right = y;
							if (y.succ()) {
								y.succ(false);
								x.pred(y);
							}
						}
						x = nodePath[i - 1];
						x.black(false);
						y.black(true);
						x.right = y.left;
						y.left = x;
						if (i < 2) tree = y;
						else {
							if (dirPath[i - 2]) nodePath[i - 2].right = y;
							else nodePath[i - 2].left = y;
						}
						if (y.pred()) {
							y.pred(false);
							x.succ(y);
						}
						break;
					}
				}
			}
		}
		tree.black(true);
		// We clean up the node path, or we could have stale references later.
		while (maxDepth-- != 0) nodePath[maxDepth] = null;
		return e;
	}
	/* After execution of this method, {@link #modified} is true iff an entry
	has been deleted. */

	@Override
	public float remove(final int k) {
		modified = false;
		if (tree == null) return defRetValue;
		Entry p = tree;
		int cmp;
		int i = 0;
		final int kk = k;
		while (true) {
			if ((cmp = compare(kk, p.key)) == 0) break;
			dirPath[i] = cmp > 0;
			nodePath[i] = p;
			if (dirPath[i++]) {
				if ((p = p.right()) == null) {
					// We clean up the node path, or we could have stale references later.
					while (i-- != 0) nodePath[i] = null;
					return defRetValue;
				}
			} else {
				if ((p = p.left()) == null) {
					// We clean up the node path, or we could have stale references later.
					while (i-- != 0) nodePath[i] = null;
					return defRetValue;
				}
			}
		}
		if (p.left == null) firstEntry = p.next();
		if (p.right == null) lastEntry = p.prev();
		if (p.succ()) {
			if (p.pred()) {
				if (i == 0) tree = p.left;
				else {
					if (dirPath[i - 1]) nodePath[i - 1].succ(p.right);
					else nodePath[i - 1].pred(p.left);
				}
			} else {
				p.prev().right = p.right;
				if (i == 0) tree = p.left;
				else {
					if (dirPath[i - 1]) nodePath[i - 1].right = p.left;
					else nodePath[i - 1].left = p.left;
				}
			}
		} else {
			boolean color;
			Entry r = p.right;
			if (r.pred()) {
				r.left = p.left;
				r.pred(p.pred());
				if (!r.pred()) r.prev().right = r;
				if (i == 0) tree = r;
				else {
					if (dirPath[i - 1]) nodePath[i - 1].right = r;
					else nodePath[i - 1].left = r;
				}
				color = r.black();
				r.black(p.black());
				p.black(color);
				dirPath[i] = true;
				nodePath[i++] = r;
			} else {
				Entry s;
				int j = i++;
				while (true) {
					dirPath[i] = false;
					nodePath[i++] = r;
					s = r.left;
					if (s.pred()) break;
					r = s;
				}
				dirPath[j] = true;
				nodePath[j] = s;
				if (s.succ()) r.pred(s);
				else r.left = s.right;
				s.left = p.left;
				if (!p.pred()) {
					p.prev().right = s;
					s.pred(false);
				}
				s.right(p.right);
				color = s.black();
				s.black(p.black());
				p.black(color);
				if (j == 0) tree = s;
				else {
					if (dirPath[j - 1]) nodePath[j - 1].right = s;
					else nodePath[j - 1].left = s;
				}
			}
		}
		int maxDepth = i;
		if (p.black()) {
			for (; i > 0; i--) {
				if (dirPath[i - 1] && !nodePath[i - 1].succ() || !dirPath[i - 1] && !nodePath[i - 1].pred()) {
					Entry x = dirPath[i - 1] ? nodePath[i - 1].right : nodePath[i - 1].left;
					if (!x.black()) {
						x.black(true);
						break;
					}
				}
				if (!dirPath[i - 1]) {
					Entry w = nodePath[i - 1].right;
					if (!w.black()) {
						w.black(true);
						nodePath[i - 1].black(false);
						nodePath[i - 1].right = w.left;
						w.left = nodePath[i - 1];
						if (i < 2) tree = w;
						else {
							if (dirPath[i - 2]) nodePath[i - 2].right = w;
							else nodePath[i - 2].left = w;
						}
						nodePath[i] = nodePath[i - 1];
						dirPath[i] = false;
						nodePath[i - 1] = w;
						if (maxDepth == i++) maxDepth++;
						w = nodePath[i - 1].right;
					}
					if ((w.pred() || w.left.black()) && (w.succ() || w.right.black())) {
						w.black(false);
					} else {
						if (w.succ() || w.right.black()) {
							Entry y = w.left;
							y.black(true);
							w.black(false);
							w.left = y.right;
							y.right = w;
							w = nodePath[i - 1].right = y;
							if (w.succ()) {
								w.succ(false);
								w.right.pred(w);
							}
						}
						w.black(nodePath[i - 1].black());
						nodePath[i - 1].black(true);
						w.right.black(true);
						nodePath[i - 1].right = w.left;
						w.left = nodePath[i - 1];
						if (i < 2) tree = w;
						else {
							if (dirPath[i - 2]) nodePath[i - 2].right = w;
							else nodePath[i - 2].left = w;
						}
						if (w.pred()) {
							w.pred(false);
							nodePath[i - 1].succ(w);
						}
						break;
					}
				} else {
					Entry w = nodePath[i - 1].left;
					if (!w.black()) {
						w.black(true);
						nodePath[i - 1].black(false);
						nodePath[i - 1].left = w.right;
						w.right = nodePath[i - 1];
						if (i < 2) tree = w;
						else {
							if (dirPath[i - 2]) nodePath[i - 2].right = w;
							else nodePath[i - 2].left = w;
						}
						nodePath[i] = nodePath[i - 1];
						dirPath[i] = true;
						nodePath[i - 1] = w;
						if (maxDepth == i++) maxDepth++;
						w = nodePath[i - 1].left;
					}
					if ((w.pred() || w.left.black()) && (w.succ() || w.right.black())) {
						w.black(false);
					} else {
						if (w.pred() || w.left.black()) {
							Entry y = w.right;
							y.black(true);
							w.black(false);
							w.right = y.left;
							y.left = w;
							w = nodePath[i - 1].left = y;
							if (w.pred()) {
								w.pred(false);
								w.left.succ(w);
							}
						}
						w.black(nodePath[i - 1].black());
						nodePath[i - 1].black(true);
						w.left.black(true);
						nodePath[i - 1].left = w.right;
						w.right = nodePath[i - 1];
						if (i < 2) tree = w;
						else {
							if (dirPath[i - 2]) nodePath[i - 2].right = w;
							else nodePath[i - 2].left = w;
						}
						if (w.succ()) {
							w.succ(false);
							nodePath[i - 1].pred(w);
						}
						break;
					}
				}
			}
			if (tree != null) tree.black(true);
		}
		modified = true;
		count--;
		// We clean up the node path, or we could have stale references later.
		while (maxDepth-- != 0) nodePath[maxDepth] = null;
		return p.value;
	}

	@Override
	public boolean containsValue(final float v) {
		final ValueIterator i = new ValueIterator();
		float ev;
		int j = count;
		while (j-- != 0) {
			ev = i.nextFloat();
			if ((Float.floatToIntBits(ev) == Float.floatToIntBits(v))) return true;
		}
		return false;
	}

	@Override
	public void clear() {
		count = 0;
		tree = null;
		entries = null;
		values = null;
		keys = null;
		firstEntry = lastEntry = null;
	}

	/**
	 * This class represent an entry in a tree map.
	 *
	 * <p>
	 * We use the only "metadata", i.e., {@link Entry#info}, to store information about color,
	 * predecessor status and successor status.
	 *
	 * <p>
	 * Note that since the class is recursive, it can be considered equivalently a tree.
	 */
	private static final class Entry extends AbstractInt2FloatMap.BasicEntry implements Cloneable {
		/** The the bit in this mask is true, the node is black. */
		private static final int BLACK_MASK = 1;
		/** If the bit in this mask is true, {@link #right} points to a successor. */
		private static final int SUCC_MASK = 1 << 31;
		/** If the bit in this mask is true, {@link #left} points to a predecessor. */
		private static final int PRED_MASK = 1 << 30;
		/** The pointers to the left and right subtrees. */
		Entry left, right;
		/**
		 * This integers holds different information in different bits (see {@link #SUCC_MASK} and
		 * {@link #PRED_MASK}.
		 */
		int info;

		Entry() {
			super((0), (0));
		}

		/**
		 * Creates a new entry with the given key and value.
		 *
		 * @param k a key.
		 * @param v a value.
		 */
		Entry(final int k, final float v) {
			super(k, v);
			info = SUCC_MASK | PRED_MASK;
		}

		/**
		 * Returns the left subtree.
		 *
		 * @return the left subtree ({@code null} if the left subtree is empty).
		 */
		Entry left() {
			return (info & PRED_MASK) != 0 ? null : left;
		}

		/**
		 * Returns the right subtree.
		 *
		 * @return the right subtree ({@code null} if the right subtree is empty).
		 */
		Entry right() {
			return (info & SUCC_MASK) != 0 ? null : right;
		}

		/**
		 * Checks whether the left pointer is really a predecessor.
		 * 
		 * @return true if the left pointer is a predecessor.
		 */
		boolean pred() {
			return (info & PRED_MASK) != 0;
		}

		/**
		 * Checks whether the right pointer is really a successor.
		 * 
		 * @return true if the right pointer is a successor.
		 */
		boolean succ() {
			return (info & SUCC_MASK) != 0;
		}

		/**
		 * Sets whether the left pointer is really a predecessor.
		 * 
		 * @param pred if true then the left pointer will be considered a predecessor.
		 */
		void pred(final boolean pred) {
			if (pred) info |= PRED_MASK;
			else info &= ~PRED_MASK;
		}

		/**
		 * Sets whether the right pointer is really a successor.
		 * 
		 * @param succ if true then the right pointer will be considered a successor.
		 */
		void succ(final boolean succ) {
			if (succ) info |= SUCC_MASK;
			else info &= ~SUCC_MASK;
		}

		/**
		 * Sets the left pointer to a predecessor.
		 * 
		 * @param pred the predecessr.
		 */
		void pred(final Entry pred) {
			info |= PRED_MASK;
			left = pred;
		}

		/**
		 * Sets the right pointer to a successor.
		 * 
		 * @param succ the successor.
		 */
		void succ(final Entry succ) {
			info |= SUCC_MASK;
			right = succ;
		}

		/**
		 * Sets the left pointer to the given subtree.
		 * 
		 * @param left the new left subtree.
		 */
		void left(final Entry left) {
			info &= ~PRED_MASK;
			this.left = left;
		}

		/**
		 * Sets the right pointer to the given subtree.
		 * 
		 * @param right the new right subtree.
		 */
		void right(final Entry right) {
			info &= ~SUCC_MASK;
			this.right = right;
		}

		/**
		 * Returns whether this node is black.
		 * 
		 * @return true iff this node is black.
		 */
		boolean black() {
			return (info & BLACK_MASK) != 0;
		}

		/**
		 * Sets whether this node is black.
		 * 
		 * @param black if true, then this node becomes black; otherwise, it becomes red..
		 */
		void black(final boolean black) {
			if (black) info |= BLACK_MASK;
			else info &= ~BLACK_MASK;
		}

		/**
		 * Computes the next entry in the set order.
		 *
		 * @return the next entry ({@code null}) if this is the last entry).
		 */
		Entry next() {
			Entry next = this.right;
			if ((info & SUCC_MASK) == 0) while ((next.info & PRED_MASK) == 0) next = next.left;
			return next;
		}

		/**
		 * Computes the previous entry in the set order.
		 *
		 * @return the previous entry ({@code null}) if this is the first entry).
		 */
		Entry prev() {
			Entry prev = this.left;
			if ((info & PRED_MASK) == 0) while ((prev.info & SUCC_MASK) == 0) prev = prev.right;
			return prev;
		}

		@Override
		public float setValue(final float value) {
			final float oldValue = this.value;
			this.value = value;
			return oldValue;
		}

		@Override

		public Entry clone() {
			Entry c;
			try {
				c = (Entry)super.clone();
			} catch (CloneNotSupportedException cantHappen) {
				throw new InternalError();
			}
			c.key = key;
			c.value = value;
			c.info = info;
			return c;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(final Object o) {
			if (!(o instanceof Map.Entry)) return false;
			Map.Entry<Integer, Float> e = (Map.Entry<Integer, Float>)o;
			return ((key) == ((e.getKey()).intValue())) && (Float.floatToIntBits(value) == Float.floatToIntBits((e.getValue()).floatValue()));
		}

		@Override
		public int hashCode() {
			return (key) ^ it.unimi.dsi.fastutil.HashCommon.float2int(value);
		}

		@Override
		public String toString() {
			return key + "=>" + value;
		}
		/*
		  public void prettyPrint() {
		  prettyPrint(0);
		  }
		
		  public void prettyPrint(int level) {
		  if (pred()) {
		  for (int i = 0; i < level; i++)
		  System.err.print("  ");
		  System.err.println("pred: " + left);
		  }
		  else if (left != null)
		  left.prettyPrint(level +1);
		  for (int i = 0; i < level; i++)
		  System.err.print("  ");
		  System.err.println(key + "=" + value + " (" + balance() + ")");
		  if (succ()) {
		  for (int i = 0; i < level; i++)
		  System.err.print("  ");
		  System.err.println("succ: " + right);
		  }
		  else if (right != null)
		  right.prettyPrint(level + 1);
		  }*/
	}
	/*
	  public void prettyPrint() {
	  System.err.println("size: " + count);
	  if (tree != null) tree.prettyPrint();
	  }*/

	@Override
	public boolean containsKey(final int k) {

		return findKey(k) != null;
	}

	@Override
	public int size() {
		return count;
	}

	@Override
	public boolean isEmpty() {
		return count == 0;
	}

	@Override
	public float get(final int k) {
		final Entry e = findKey(k);
		return e == null ? defRetValue : e.value;
	}

	@Override
	public int firstIntKey() {
		if (tree == null) throw new NoSuchElementException();
		return firstEntry.key;
	}

	@Override
	public int lastIntKey() {
		if (tree == null) throw new NoSuchElementException();
		return lastEntry.key;
	}

	/**
	 * An abstract iterator on the whole range.
	 *
	 * <p>
	 * This class can iterate in both directions on a threaded tree.
	 */
	private class TreeIterator {
		/**
		 * The entry that will be returned by the next call to {@link java.util.ListIterator#previous()} (or
		 * {@code null} if no previous entry exists).
		 */
		Entry prev;
		/**
		 * The entry that will be returned by the next call to {@link java.util.ListIterator#next()} (or
		 * {@code null} if no next entry exists).
		 */
		Entry next;
		/**
		 * The last entry that was returned (or {@code null} if we did not iterate or used
		 * {@link #remove()}).
		 */
		Entry curr;
		/**
		 * The current index (in the sense of a {@link java.util.ListIterator}). Note that this value is not
		 * meaningful when this {@link TreeIterator} has been created using the nonempty constructor.
		 */
		int index = 0;

		TreeIterator() {
			next = firstEntry;
		}

		TreeIterator(final int k) {
			if ((next = locateKey(k)) != null) {
				if (compare(next.key, k) <= 0) {
					prev = next;
					next = next.next();
				} else prev = next.prev();
			}
		}

		public boolean hasNext() {
			return next != null;
		}

		public boolean hasPrevious() {
			return prev != null;
		}

		void updateNext() {
			next = next.next();
		}

		Entry nextEntry() {
			if (!hasNext()) throw new NoSuchElementException();
			curr = prev = next;
			index++;
			updateNext();
			return curr;
		}

		void updatePrevious() {
			prev = prev.prev();
		}

		Entry previousEntry() {
			if (!hasPrevious()) throw new NoSuchElementException();
			curr = next = prev;
			index--;
			updatePrevious();
			return curr;
		}

		public int nextIndex() {
			return index;
		}

		public int previousIndex() {
			return index - 1;
		}

		public void remove() {
			if (curr == null) throw new IllegalStateException();
			/* If the last operation was a next(), we are removing an entry that preceeds
				   the current index, and thus we must decrement it. */
			if (curr == prev) index--;
			next = prev = curr;
			updatePrevious();
			updateNext();
			Int2FloatRBTreeMap.this.remove(curr.key);
			curr = null;
		}

		public int skip(final int n) {
			int i = n;
			while (i-- != 0 && hasNext()) nextEntry();
			return n - i - 1;
		}

		public int back(final int n) {
			int i = n;
			while (i-- != 0 && hasPrevious()) previousEntry();
			return n - i - 1;
		}
	}

	/**
	 * An iterator on the whole range.
	 *
	 * <p>
	 * This class can iterate in both directions on a threaded tree.
	 */
	private class EntryIterator extends TreeIterator implements ObjectListIterator<Int2FloatMap.Entry> {
		EntryIterator() {
		}

		EntryIterator(final int k) {
			super(k);
		}

		@Override
		public Int2FloatMap.Entry next() {
			return nextEntry();
		}

		@Override
		public Int2FloatMap.Entry previous() {
			return previousEntry();
		}
	}

	@Override

	public ObjectSortedSet<Int2FloatMap.Entry> int2FloatEntrySet() {
		if (entries == null) entries = new AbstractObjectSortedSet<Int2FloatMap.Entry>() {
			final Comparator<? super Int2FloatMap.Entry> comparator = (Int2FloatRBTreeMap.this.actualComparator == null ? (Comparator<Int2FloatMap.Entry>)(x, y) -> (Integer.compare((x.getIntKey()), (y.getIntKey()))) : (Comparator<Int2FloatMap.Entry>)(x, y) -> Int2FloatRBTreeMap.this.actualComparator.compare(x.getIntKey(), y.getIntKey()));

			@Override
			public Comparator<? super Int2FloatMap.Entry> comparator() {
				return comparator;
			}

			@Override
			public ObjectBidirectionalIterator<Int2FloatMap.Entry> iterator() {
				return new EntryIterator();
			}

			@Override
			public ObjectBidirectionalIterator<Int2FloatMap.Entry> iterator(final Int2FloatMap.Entry from) {
				return new EntryIterator(from.getIntKey());
			}

			@Override

			public boolean contains(final Object o) {
				if (o == null || !(o instanceof Map.Entry)) return false;
				final Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
				if (e.getKey() == null) return false;
				if (!(e.getKey() instanceof Integer)) return false;
				if (e.getValue() == null || !(e.getValue() instanceof Float)) return false;
				final Entry f = findKey(((Integer)(e.getKey())).intValue());
				return e.equals(f);
			}

			@Override

			public boolean remove(final Object o) {
				if (!(o instanceof Map.Entry)) return false;
				final Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
				if (e.getKey() == null) return false;
				if (!(e.getKey() instanceof Integer)) return false;
				if (e.getValue() == null || !(e.getValue() instanceof Float)) return false;
				final Entry f = findKey(((Integer)(e.getKey())).intValue());
				if (f == null || !(Float.floatToIntBits(f.getFloatValue()) == Float.floatToIntBits(((Float)(e.getValue())).floatValue()))) return false;
				Int2FloatRBTreeMap.this.remove(f.key);
				return true;
			}

			@Override
			public int size() {
				return count;
			}

			@Override
			public void clear() {
				Int2FloatRBTreeMap.this.clear();
			}

			@Override
			public Int2FloatMap.Entry first() {
				return firstEntry;
			}

			@Override
			public Int2FloatMap.Entry last() {
				return lastEntry;
			}

			@Override
			public ObjectSortedSet<Int2FloatMap.Entry> subSet(Int2FloatMap.Entry from, Int2FloatMap.Entry to) {
				return subMap(from.getIntKey(), to.getIntKey()).int2FloatEntrySet();
			}

			@Override
			public ObjectSortedSet<Int2FloatMap.Entry> headSet(Int2FloatMap.Entry to) {
				return headMap(to.getIntKey()).int2FloatEntrySet();
			}

			@Override
			public ObjectSortedSet<Int2FloatMap.Entry> tailSet(Int2FloatMap.Entry from) {
				return tailMap(from.getIntKey()).int2FloatEntrySet();
			}
		};
		return entries;
	}

	/**
	 * An iterator on the whole range of keys.
	 *
	 * <p>
	 * This class can iterate in both directions on the keys of a threaded tree. We simply override the
	 * {@link java.util.ListIterator#next()}/{@link java.util.ListIterator#previous()} methods (and
	 * possibly their type-specific counterparts) so that they return keys instead of entries.
	 */
	private final class KeyIterator extends TreeIterator implements IntListIterator {
		public KeyIterator() {
		}

		public KeyIterator(final int k) {
			super(k);
		}

		@Override
		public int nextInt() {
			return nextEntry().key;
		}

		@Override
		public int previousInt() {
			return previousEntry().key;
		}
	};

	/** A keyset implementation using a more direct implementation for iterators. */
	private class KeySet extends AbstractInt2FloatSortedMap.KeySet {
		@Override
		public IntBidirectionalIterator iterator() {
			return new KeyIterator();
		}

		@Override
		public IntBidirectionalIterator iterator(final int from) {
			return new KeyIterator(from);
		}
	}

	/**
	 * Returns a type-specific sorted set view of the keys contained in this map.
	 *
	 * <p>
	 * In addition to the semantics of {@link java.util.Map#keySet()}, you can safely cast the set
	 * returned by this call to a type-specific sorted set interface.
	 *
	 * @return a type-specific sorted set view of the keys contained in this map.
	 */
	@Override
	public IntSortedSet keySet() {
		if (keys == null) keys = new KeySet();
		return keys;
	}

	/**
	 * An iterator on the whole range of values.
	 *
	 * <p>
	 * This class can iterate in both directions on the values of a threaded tree. We simply override
	 * the {@link java.util.ListIterator#next()}/{@link java.util.ListIterator#previous()} methods (and
	 * possibly their type-specific counterparts) so that they return values instead of entries.
	 */
	private final class ValueIterator extends TreeIterator implements FloatListIterator {
		@Override
		public float nextFloat() {
			return nextEntry().value;
		}

		@Override
		public float previousFloat() {
			return previousEntry().value;
		}
	};

	/**
	 * Returns a type-specific collection view of the values contained in this map.
	 *
	 * <p>
	 * In addition to the semantics of {@link java.util.Map#values()}, you can safely cast the
	 * collection returned by this call to a type-specific collection interface.
	 *
	 * @return a type-specific collection view of the values contained in this map.
	 */
	@Override
	public FloatCollection values() {
		if (values == null) values = new AbstractFloatCollection() {
			@Override
			public FloatIterator iterator() {
				return new ValueIterator();
			}

			@Override
			public boolean contains(final float k) {
				return containsValue(k);
			}

			@Override
			public int size() {
				return count;
			}

			@Override
			public void clear() {
				Int2FloatRBTreeMap.this.clear();
			}
		};
		return values;
	}

	@Override
	public IntComparator comparator() {
		return actualComparator;
	}

	@Override
	public Int2FloatSortedMap headMap(int to) {
		return new Submap((0), true, to, false);
	}

	@Override
	public Int2FloatSortedMap tailMap(int from) {
		return new Submap(from, false, (0), true);
	}

	@Override
	public Int2FloatSortedMap subMap(int from, int to) {
		return new Submap(from, false, to, false);
	}

	/**
	 * A submap with given range.
	 *
	 * <p>
	 * This class represents a submap. One has to specify the left/right limits (which can be set to
	 * -&infin; or &infin;). Since the submap is a view on the map, at a given moment it could happen
	 * that the limits of the range are not any longer in the main map. Thus, things such as
	 * {@link java.util.SortedMap#firstKey()} or {@link java.util.Collection#size()} must be always
	 * computed on-the-fly.
	 */
	private final class Submap extends AbstractInt2FloatSortedMap implements java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		/** The start of the submap range, unless {@link #bottom} is true. */
		int from;
		/** The end of the submap range, unless {@link #top} is true. */
		int to;
		/** If true, the submap range starts from -&infin;. */
		boolean bottom;
		/** If true, the submap range goes to &infin;. */
		boolean top;
		/** Cached set of entries. */
		protected transient ObjectSortedSet<Int2FloatMap.Entry> entries;
		/** Cached set of keys. */
		protected transient IntSortedSet keys;
		/** Cached collection of values. */
		protected transient FloatCollection values;

		/**
		 * Creates a new submap with given key range.
		 *
		 * @param from the start of the submap range.
		 * @param bottom if true, the first parameter is ignored and the range starts from -&infin;.
		 * @param to the end of the submap range.
		 * @param top if true, the third parameter is ignored and the range goes to &infin;.
		 */
		public Submap(final int from, final boolean bottom, final int to, final boolean top) {
			if (!bottom && !top && Int2FloatRBTreeMap.this.compare(from, to) > 0) throw new IllegalArgumentException("Start key (" + from + ") is larger than end key (" + to + ")");
			this.from = from;
			this.bottom = bottom;
			this.to = to;
			this.top = top;
			this.defRetValue = Int2FloatRBTreeMap.this.defRetValue;
		}

		@Override
		public void clear() {
			final SubmapIterator i = new SubmapIterator();
			while (i.hasNext()) {
				i.nextEntry();
				i.remove();
			}
		}

		/**
		 * Checks whether a key is in the submap range.
		 * 
		 * @param k a key.
		 * @return true if is the key is in the submap range.
		 */
		final boolean in(final int k) {
			return (bottom || Int2FloatRBTreeMap.this.compare(k, from) >= 0) && (top || Int2FloatRBTreeMap.this.compare(k, to) < 0);
		}

		@Override
		public ObjectSortedSet<Int2FloatMap.Entry> int2FloatEntrySet() {
			if (entries == null) entries = new AbstractObjectSortedSet<Int2FloatMap.Entry>() {
				@Override
				public ObjectBidirectionalIterator<Int2FloatMap.Entry> iterator() {
					return new SubmapEntryIterator();
				}

				@Override
				public ObjectBidirectionalIterator<Int2FloatMap.Entry> iterator(final Int2FloatMap.Entry from) {
					return new SubmapEntryIterator(from.getIntKey());
				}

				@Override
				public Comparator<? super Int2FloatMap.Entry> comparator() {
					return Int2FloatRBTreeMap.this.int2FloatEntrySet().comparator();
				}

				@Override

				public boolean contains(final Object o) {
					if (!(o instanceof Map.Entry)) return false;
					final Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
					if (e.getKey() == null || !(e.getKey() instanceof Integer)) return false;
					if (e.getValue() == null || !(e.getValue() instanceof Float)) return false;
					final Int2FloatRBTreeMap.Entry f = findKey(((Integer)(e.getKey())).intValue());
					return f != null && in(f.key) && e.equals(f);
				}

				@Override

				public boolean remove(final Object o) {
					if (!(o instanceof Map.Entry)) return false;
					final Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
					if (e.getKey() == null || !(e.getKey() instanceof Integer)) return false;
					if (e.getValue() == null || !(e.getValue() instanceof Float)) return false;
					final Int2FloatRBTreeMap.Entry f = findKey(((Integer)(e.getKey())).intValue());
					if (f != null && in(f.key)) Submap.this.remove(f.key);
					return f != null;
				}

				@Override
				public int size() {
					int c = 0;
					for (Iterator<?> i = iterator(); i.hasNext(); i.next()) c++;
					return c;
				}

				@Override
				public boolean isEmpty() {
					return !new SubmapIterator().hasNext();
				}

				@Override
				public void clear() {
					Submap.this.clear();
				}

				@Override
				public Int2FloatMap.Entry first() {
					return firstEntry();
				}

				@Override
				public Int2FloatMap.Entry last() {
					return lastEntry();
				}

				@Override
				public ObjectSortedSet<Int2FloatMap.Entry> subSet(Int2FloatMap.Entry from, Int2FloatMap.Entry to) {
					return subMap(from.getIntKey(), to.getIntKey()).int2FloatEntrySet();
				}

				@Override
				public ObjectSortedSet<Int2FloatMap.Entry> headSet(Int2FloatMap.Entry to) {
					return headMap(to.getIntKey()).int2FloatEntrySet();
				}

				@Override
				public ObjectSortedSet<Int2FloatMap.Entry> tailSet(Int2FloatMap.Entry from) {
					return tailMap(from.getIntKey()).int2FloatEntrySet();
				}
			};
			return entries;
		}

		private class KeySet extends AbstractInt2FloatSortedMap.KeySet {
			@Override
			public IntBidirectionalIterator iterator() {
				return new SubmapKeyIterator();
			}

			@Override
			public IntBidirectionalIterator iterator(final int from) {
				return new SubmapKeyIterator(from);
			}
		}

		@Override
		public IntSortedSet keySet() {
			if (keys == null) keys = new KeySet();
			return keys;
		}

		@Override
		public FloatCollection values() {
			if (values == null) values = new AbstractFloatCollection() {
				@Override
				public FloatIterator iterator() {
					return new SubmapValueIterator();
				}

				@Override
				public boolean contains(final float k) {
					return containsValue(k);
				}

				@Override
				public int size() {
					return Submap.this.size();
				}

				@Override
				public void clear() {
					Submap.this.clear();
				}
			};
			return values;
		}

		@Override

		public boolean containsKey(final int k) {

			return in(k) && Int2FloatRBTreeMap.this.containsKey(k);
		}

		@Override
		public boolean containsValue(final float v) {
			final SubmapIterator i = new SubmapIterator();
			float ev;
			while (i.hasNext()) {
				ev = i.nextEntry().value;
				if ((Float.floatToIntBits(ev) == Float.floatToIntBits(v))) return true;
			}
			return false;
		}

		@Override

		public float get(final int k) {
			final Int2FloatRBTreeMap.Entry e;
			final int kk = k;
			return in(kk) && (e = findKey(kk)) != null ? e.value : this.defRetValue;
		}

		@Override
		public float put(final int k, final float v) {
			modified = false;
			if (!in(k)) throw new IllegalArgumentException("Key (" + k + ") out of range [" + (bottom ? "-" : String.valueOf(from)) + ", " + (top ? "-" : String.valueOf(to)) + ")");
			final float oldValue = Int2FloatRBTreeMap.this.put(k, v);
			return modified ? this.defRetValue : oldValue;
		}

		@Override

		public float remove(final int k) {
			modified = false;
			if (!in(k)) return this.defRetValue;
			final float oldValue = Int2FloatRBTreeMap.this.remove(k);
			return modified ? oldValue : this.defRetValue;
		}

		@Override
		public int size() {
			final SubmapIterator i = new SubmapIterator();
			int n = 0;
			while (i.hasNext()) {
				n++;
				i.nextEntry();
			}
			return n;
		}

		@Override
		public boolean isEmpty() {
			return !new SubmapIterator().hasNext();
		}

		@Override
		public IntComparator comparator() {
			return actualComparator;
		}

		@Override
		public Int2FloatSortedMap headMap(final int to) {
			if (top) return new Submap(from, bottom, to, false);
			return compare(to, this.to) < 0 ? new Submap(from, bottom, to, false) : this;
		}

		@Override
		public Int2FloatSortedMap tailMap(final int from) {
			if (bottom) return new Submap(from, false, to, top);
			return compare(from, this.from) > 0 ? new Submap(from, false, to, top) : this;
		}

		@Override
		public Int2FloatSortedMap subMap(int from, int to) {
			if (top && bottom) return new Submap(from, false, to, false);
			if (!top) to = compare(to, this.to) < 0 ? to : this.to;
			if (!bottom) from = compare(from, this.from) > 0 ? from : this.from;
			if (!top && !bottom && from == this.from && to == this.to) return this;
			return new Submap(from, false, to, false);
		}

		/**
		 * Locates the first entry.
		 *
		 * @return the first entry of this submap, or {@code null} if the submap is empty.
		 */
		public Int2FloatRBTreeMap.Entry firstEntry() {
			if (tree == null) return null;
			// If this submap goes to -infinity, we return the main map first entry; otherwise, we locate the
			// start of the map.
			Int2FloatRBTreeMap.Entry e;
			if (bottom) e = firstEntry;
			else {
				e = locateKey(from);
				// If we find either the start or something greater we're OK.
				if (compare(e.key, from) < 0) e = e.next();
			}
			// Finally, if this submap doesn't go to infinity, we check that the resulting key isn't greater
			// than the end.
			if (e == null || !top && compare(e.key, to) >= 0) return null;
			return e;
		}

		/**
		 * Locates the last entry.
		 *
		 * @return the last entry of this submap, or {@code null} if the submap is empty.
		 */
		public Int2FloatRBTreeMap.Entry lastEntry() {
			if (tree == null) return null;
			// If this submap goes to infinity, we return the main map last entry; otherwise, we locate the end
			// of the map.
			Int2FloatRBTreeMap.Entry e;
			if (top) e = lastEntry;
			else {
				e = locateKey(to);
				// If we find something smaller than the end we're OK.
				if (compare(e.key, to) >= 0) e = e.prev();
			}
			// Finally, if this submap doesn't go to -infinity, we check that the resulting key isn't smaller
			// than the start.
			if (e == null || !bottom && compare(e.key, from) < 0) return null;
			return e;
		}

		@Override
		public int firstIntKey() {
			Int2FloatRBTreeMap.Entry e = firstEntry();
			if (e == null) throw new NoSuchElementException();
			return e.key;
		}

		@Override
		public int lastIntKey() {
			Int2FloatRBTreeMap.Entry e = lastEntry();
			if (e == null) throw new NoSuchElementException();
			return e.key;
		}

		/**
		 * An iterator for subranges.
		 *
		 * <p>
		 * This class inherits from {@link TreeIterator}, but overrides the methods that update the pointer
		 * after a {@link java.util.ListIterator#next()} or {@link java.util.ListIterator#previous()}. If we
		 * would move out of the range of the submap we just overwrite the next or previous entry with
		 * {@code null}.
		 */
		private class SubmapIterator extends TreeIterator {
			SubmapIterator() {
				next = firstEntry();
			}

			SubmapIterator(final int k) {
				this();
				if (next != null) {
					if (!bottom && compare(k, next.key) < 0) prev = null;
					else if (!top && compare(k, (prev = lastEntry()).key) >= 0) next = null;
					else {
						next = locateKey(k);
						if (compare(next.key, k) <= 0) {
							prev = next;
							next = next.next();
						} else prev = next.prev();
					}
				}
			}

			@Override
			void updatePrevious() {
				prev = prev.prev();
				if (!bottom && prev != null && Int2FloatRBTreeMap.this.compare(prev.key, from) < 0) prev = null;
			}

			@Override
			void updateNext() {
				next = next.next();
				if (!top && next != null && Int2FloatRBTreeMap.this.compare(next.key, to) >= 0) next = null;
			}
		}

		private class SubmapEntryIterator extends SubmapIterator implements ObjectListIterator<Int2FloatMap.Entry> {
			SubmapEntryIterator() {
			}

			SubmapEntryIterator(final int k) {
				super(k);
			}

			@Override
			public Int2FloatMap.Entry next() {
				return nextEntry();
			}

			@Override
			public Int2FloatMap.Entry previous() {
				return previousEntry();
			}
		}

		/**
		 * An iterator on a subrange of keys.
		 *
		 * <p>
		 * This class can iterate in both directions on a subrange of the keys of a threaded tree. We simply
		 * override the {@link java.util.ListIterator#next()}/{@link java.util.ListIterator#previous()}
		 * methods (and possibly their type-specific counterparts) so that they return keys instead of
		 * entries.
		 */
		private final class SubmapKeyIterator extends SubmapIterator implements IntListIterator {
			public SubmapKeyIterator() {
				super();
			}

			public SubmapKeyIterator(int from) {
				super(from);
			}

			@Override
			public int nextInt() {
				return nextEntry().key;
			}

			@Override
			public int previousInt() {
				return previousEntry().key;
			}
		};

		/**
		 * An iterator on a subrange of values.
		 *
		 * <p>
		 * This class can iterate in both directions on the values of a subrange of the keys of a threaded
		 * tree. We simply override the
		 * {@link java.util.ListIterator#next()}/{@link java.util.ListIterator#previous()} methods (and
		 * possibly their type-specific counterparts) so that they return values instead of entries.
		 */
		private final class SubmapValueIterator extends SubmapIterator implements FloatListIterator {
			@Override
			public float nextFloat() {
				return nextEntry().value;
			}

			@Override
			public float previousFloat() {
				return previousEntry().value;
			}
		};
	}

	/**
	 * Returns a deep copy of this tree map.
	 *
	 * <p>
	 * This method performs a deep copy of this tree map; the data stored in the set, however, is not
	 * cloned. Note that this makes a difference only for object keys.
	 *
	 * @return a deep copy of this tree map.
	 */
	@Override

	public Int2FloatRBTreeMap clone() {
		Int2FloatRBTreeMap c;
		try {
			c = (Int2FloatRBTreeMap)super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new InternalError();
		}
		c.keys = null;
		c.values = null;
		c.entries = null;
		c.allocatePaths();
		if (count != 0) {
			// Also this apparently unfathomable code is derived from GNU libavl.
			Entry e, p, q, rp = new Entry(), rq = new Entry();
			p = rp;
			rp.left(tree);
			q = rq;
			rq.pred(null);
			while (true) {
				if (!p.pred()) {
					e = p.left.clone();
					e.pred(q.left);
					e.succ(q);
					q.left(e);
					p = p.left;
					q = q.left;
				} else {
					while (p.succ()) {
						p = p.right;
						if (p == null) {
							q.right = null;
							c.tree = rq.left;
							c.firstEntry = c.tree;
							while (c.firstEntry.left != null) c.firstEntry = c.firstEntry.left;
							c.lastEntry = c.tree;
							while (c.lastEntry.right != null) c.lastEntry = c.lastEntry.right;
							return c;
						}
						q = q.right;
					}
					p = p.right;
					q = q.right;
				}
				if (!p.succ()) {
					e = p.right.clone();
					e.succ(q.right);
					e.pred(q);
					q.right(e);
				}
			}
		}
		return c;
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		int n = count;
		EntryIterator i = new EntryIterator();
		Entry e;
		s.defaultWriteObject();
		while (n-- != 0) {
			e = i.nextEntry();
			s.writeInt(e.key);
			s.writeFloat(e.value);
		}
	}

	/**
	 * Reads the given number of entries from the input stream, returning the corresponding tree.
	 *
	 * @param s the input stream.
	 * @param n the (positive) number of entries to read.
	 * @param pred the entry containing the key that preceeds the first key in the tree.
	 * @param succ the entry containing the key that follows the last key in the tree.
	 */

	private Entry readTree(final java.io.ObjectInputStream s, final int n, final Entry pred, final Entry succ) throws java.io.IOException, ClassNotFoundException {
		if (n == 1) {
			final Entry top = new Entry(s.readInt(), s.readFloat());
			top.pred(pred);
			top.succ(succ);
			top.black(true);
			return top;
		}
		if (n == 2) {
			/* We handle separately this case so that recursion will
				 *always* be on nonempty subtrees. */
			final Entry top = new Entry(s.readInt(), s.readFloat());
			top.black(true);
			top.right(new Entry(s.readInt(), s.readFloat()));
			top.right.pred(top);
			top.pred(pred);
			top.right.succ(succ);
			return top;
		}
		// The right subtree is the largest one.
		final int rightN = n / 2, leftN = n - rightN - 1;
		final Entry top = new Entry();
		top.left(readTree(s, leftN, pred, top));
		top.key = s.readInt();
		top.value = s.readFloat();
		top.black(true);
		top.right(readTree(s, rightN, top, succ));
		if (n + 2 == ((n + 2) & -(n + 2))) top.right.black(false); // Quick test for determining whether n + 2 is a
																	// power of 2.
		return top;
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		/* The storedComparator is now correctly set, but we must restore
		   on-the-fly the actualComparator. */
		setActualComparator();
		allocatePaths();
		if (count != 0) {
			tree = readTree(s, count, null, null);
			Entry e;
			e = tree;
			while (e.left() != null) e = e.left();
			firstEntry = e;
			e = tree;
			while (e.right() != null) e = e.right();
			lastEntry = e;
		}
	}
}
