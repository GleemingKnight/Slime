package me.hugmanrique.slime;

import java.io.ByteArrayInputStream;
import java.util.BitSet;

public class ProtoSlimeRegion {

    /**
     * Lowest chunk x-coordinate
     */
    private final short minX;

    /**
     * Lowest chunk z-coordinate
     */
    private final short minZ;

    /**
     * X-axis length, in chunks
     */
    private final int width;

    /**
     * Z-axis length, in chunks
     */
    private final int depth;

    private final BitSet populated;
    private final byte[] data;

    public ProtoSlimeRegion(short minX, short minZ, int width, int depth, BitSet populated, byte[] data) {
        this.minX = minX;
        this.minZ = minZ;
        this.width = width;
        this.depth = depth;
        this.populated = populated;
        this.data = data;
    }

    public SlimeInputStream getDataStream() {
        return new SlimeInputStream(
                new ByteArrayInputStream(data));
    }

    public int getChunkZ(int bitIndex) {
        return bitIndex / width;
    }

    public int getChunkX(int bitIndex) {
        return bitIndex % width;
    }

    public short getMinX() {
        return minX;
    }

    public short getMinZ() {
        return minZ;
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public BitSet getPopulated() {
        return populated;
    }

    public byte[] getData() {
        return data;
    }
}
