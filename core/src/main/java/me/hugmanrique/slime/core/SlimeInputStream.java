package me.hugmanrique.slime.core;

import com.github.luben.zstd.Zstd;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NibbleArray;

import java.io.*;
import java.util.BitSet;

public class SlimeInputStream extends DataInputStream {

    public SlimeInputStream(InputStream in) {
        super(in);
    }

    public int[] readIntArray(final int length) throws IOException {
        int[] arr = new int[length];

        for (int i = 0; i < length; i++) {
            arr[i] = readInt();
        }

        return arr;
    }

    public byte[] readByteArray(final int length) throws IOException {
        byte[] arr = new byte[length];

        int readByteCount = read(arr);

        if (readByteCount == -1) {
            throw new EOFException();
        }

        return arr;
    }

    public NibbleArray readNibbleArray(final int length) throws IOException {
        byte[] data = readByteArray(length);

        return new NibbleArray(data);
    }

    public BitSet readBitSet(final int byteCount) throws IOException {
        byte[] raw = readByteArray(byteCount);

        return BitSet.valueOf(raw);
    }

    /**
     * Reads a block of zstd-compressed data. This method
     * expects the following ints to be the compressed size,
     * and uncompressed size respectively.
     *
     * @return the uncompressed data
     * @throws IOException if the bytes cannot be read
     * @throws IllegalArgumentException if the uncompressed length doesn't match
     */
    public byte[] readCompressed() throws IOException {
        int compressedLength = readInt();
        int uncompressedLength = readInt();

        byte[] compressed = readByteArray(compressedLength);
        byte[] data = Zstd.decompress(compressed, uncompressedLength);

        if (data.length != uncompressedLength) {
            throw new IllegalArgumentException("Uncompressed length doesn't match");
        }

        return data;
    }

    /**
     * Reads and parses a block of zstd-compressed bytes as
     * an NBT named compound tag.
     *
     * @return the parsed named compound tag.
     * @throws IOException if the bytes cannot be read
     * @see #readCompressed() for requirements
     */
    public NBTTagCompound readCompressedCompound() throws IOException {
        byte[] data = readCompressed();

        return NBTCompressedStreamTools.a(new DataInputStream(
                new ByteArrayInputStream(data)));
    }

    /**
     * Skips a block of zstd-compressed data.
     *
     * @return the number of bytes skipped
     * @throws IOException if the bytes cannot be skipped
     * @see #readCompressed() for requirements
     */
    public long skipCompressed() throws IOException {
        int compressedLength = readInt();

        // Skip uncompressed length + compressed data
        return skip(4 + compressedLength);
    }
}
