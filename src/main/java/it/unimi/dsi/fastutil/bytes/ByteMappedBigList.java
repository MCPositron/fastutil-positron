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
package it.unimi.dsi.fastutil.bytes;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;

/**
 * A bridge between byte {@linkplain ByteBuffer buffers} and type-specific
 * {@linkplain it.unimi.dsi.fastutil.BigList big lists}.
 *
 * <p>
 * Java's {@linkplain FileChannel#map(MapMode, long, long) memory-mapping facilities} have the
 * severe limitation of mapping at most {@link Integer#MAX_VALUE} bytes, as they expose the content
 * of a file using a {@link MappedByteBuffer}. This class can
 * {@linkplain #map(FileChannel, ByteOrder, FileChannel.MapMode) expose a file of primitive types of
 * arbitrary length} as a {@link it.unimi.dsi.fastutil.BigList} that is actually based on an array
 * of {@link MappedByteBuffer}s, each mapping a <em>chunk</em> of {@link #CHUNK_SIZE} longs.
 *
 * <p>
 * Mapping can happen with a specified byte order: saving and mapping data in
 * {@linkplain ByteOrder#nativeOrder() native order} using methods from
 * {@link it.unimi.dsi.fastutil.io.BinIO} will enhance performance significantly.
 *
 * <p>
 * Instances of this class are not thread safe, but the {@link #copy()} method provides a
 * lightweight duplicate that can be read independently by another thread. Only chunks that are
 * actually used will be {@linkplain ByteBuffer#duplicate() duplicated} lazily. If you are
 * modifiying the content of list, however, you will need to provide external synchronization.
 * 
 * @author Sebastiano Vigna
 */
public class ByteMappedBigList extends AbstractByteBigList {
	/** The logarithm of the number of bytes of the primitive type of this list. */
	public static int LOG2_BYTES = Long.SIZE - 1 - Long.numberOfLeadingZeros(Byte.BYTES);
	/** @deprecated Use {@link #LOG2_BYTES}. */
	@Deprecated
	public static int LOG2_BITS = Long.SIZE - 1 - Long.numberOfLeadingZeros(Byte.BYTES);
	private static int CHUNK_SHIFT = 30 - LOG2_BYTES;
	/**
	 * The size in elements of a chunk created by
	 * {@link #map(FileChannel, ByteOrder, FileChannel.MapMode)}.
	 */
	public static final long CHUNK_SIZE = 1L << CHUNK_SHIFT;
	/** The mask used to compute the offset in the chunk in longs. */
	private static final long CHUNK_MASK = CHUNK_SIZE - 1;
	/** The underlying buffers. */
	private final ByteBuffer[] buffer;
	/**
	 * An array parallel to {@link #buffer} specifying which buffers do not need to be duplicated before
	 * being used.
	 */
	private final boolean[] readyToUse;
	/** The number of byte buffers. */
	private final int n;
	/** The overall size in elements. */
	private final long size;

	/**
	 * Creates a new mapped big list.
	 *
	 * @param buffer the underlying buffers.
	 * @param size the overall number of elements in the underlying buffers (i.e., the sum of the
	 *            capacities of the byte buffers divided by the size of an element in bytes).
	 * @param readyToUse an array parallel to {@code buffer} specifying which buffers do not need to be
	 *            {@linkplain ByteBuffer#duplicate() duplicated} before being used (the process will
	 *            happen lazily); the array will be used internally by the newly created mapped big
	 *            list.
	 */
	protected ByteMappedBigList(final ByteBuffer[] buffer, final long size, final boolean[] readyToUse) {
		this.buffer = buffer;
		this.n = buffer.length;
		this.size = size;
		this.readyToUse = readyToUse;
		for (int i = 0; i < n; i++) if (i < n - 1 && buffer[i].capacity() != CHUNK_SIZE) throw new IllegalArgumentException();
	}

	/**
	 * Creates a new mapped big list by read-only mapping a given file channel using the standard Java
	 * (i.e., {@link DataOutput}) byte order ({@link ByteOrder#BIG_ENDIAN}).
	 *
	 * @param fileChannel the file channel that will be mapped.
	 * @return a new read-only mapped big list over the contents of {@code fileChannel}.
	 *
	 * @see #map(FileChannel, ByteOrder, MapMode)
	 */
	public static ByteMappedBigList map(final FileChannel fileChannel) throws IOException {
		return map(fileChannel, ByteOrder.BIG_ENDIAN);
	}

	/**
	 * Creates a new mapped big list by read-only mapping a given file channel.
	 *
	 * @param fileChannel the file channel that will be mapped.
	 * @param byteOrder a prescribed byte order.
	 * @return a new read-only mapped big list over the contents of {@code fileChannel}.
	 *
	 * @see #map(FileChannel, ByteOrder, MapMode)
	 */
	public static ByteMappedBigList map(final FileChannel fileChannel, final ByteOrder byteOrder) throws IOException {
		return map(fileChannel, byteOrder, MapMode.READ_ONLY);
	}

	/**
	 * Creates a new mapped big list by mapping a given file channel.
	 *
	 * @param fileChannel the file channel that will be mapped.
	 * @param byteOrder a prescribed byte order.
	 * @param mapMode the mapping mode: usually {@link MapMode#READ_ONLY}, but if intend to make the
	 *            list {@linkplain #set mutable}, you have to pass {@link MapMode#READ_WRITE}.
	 * @return a new mapped big list over the contents of {@code fileChannel}.
	 */
	public static ByteMappedBigList map(final FileChannel fileChannel, final ByteOrder byteOrder, final MapMode mapMode) throws IOException {
		final long size = fileChannel.size() / Byte.BYTES;
		final int chunks = (int)((size + (CHUNK_SIZE - 1)) / CHUNK_SIZE);
		final ByteBuffer[] buffer = new ByteBuffer[chunks];
		for (int i = 0; i < chunks; i++) buffer[i] = fileChannel.map(mapMode, i * CHUNK_SIZE * Byte.BYTES, Math.min(CHUNK_SIZE, size - i * CHUNK_SIZE) * Byte.BYTES);
		final boolean[] readyToUse = new boolean[chunks];
		Arrays.fill(readyToUse, true);
		return new ByteMappedBigList(buffer, size, readyToUse);
	}

	private ByteBuffer ByteBuffer(final int n) {
		if (readyToUse[n]) return buffer[n];
		readyToUse[n] = true;
		return buffer[n] = buffer[n].duplicate();
	}

	/**
	 * Returns a lightweight duplicate that can be read independently by another thread.
	 *
	 * <p>
	 * Only chunks that are actually used will be {@linkplain ByteBuffer#duplicate() duplicated} lazily.
	 * 
	 * @return a lightweight duplicate that can be read independently by another thread.
	 */
	public ByteMappedBigList copy() {
		return new ByteMappedBigList(buffer.clone(), size, new boolean[n]);
	}

	@Override
	public byte getByte(final long index) {
		return ByteBuffer((int)(index >>> CHUNK_SHIFT)).get((int)(index & CHUNK_MASK));
	}

	@Override
	public void getElements(long from, byte a[], int offset, int length) {
		int chunk = (int)(from >>> CHUNK_SHIFT);
		int displ = (int)(from & CHUNK_MASK);
		while (length > 0) {
			ByteBuffer b = ByteBuffer(chunk);
			final int l = Math.min(b.capacity() - displ, length);
			if (l == 0) throw new ArrayIndexOutOfBoundsException();
			// TODO: use chaining switching to Java 9+
			b.position(displ);
			b.get(a, offset, l);
			if ((displ += l) == CHUNK_SIZE) {
				displ = 0;
				chunk++;
			}
			offset += l;
			length -= l;
		}
	}

	@Override
	public byte set(final long index, final byte value) {
		final ByteBuffer b = ByteBuffer((int)(index >>> CHUNK_SHIFT));
		final int i = (int)(index & CHUNK_MASK);
		final byte previousValue = b.get(i);
		b.put(i, value);
		return previousValue;
	}

	@Override
	public long size64() {
		return size;
	}
}
