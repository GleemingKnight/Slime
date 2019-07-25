package me.hugmanrique.slime;

import net.minecraft.server.v1_8_R3.NibbleArray;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
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
}
