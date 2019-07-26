package me.hugmanrique.slime.core;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.NibbleArray;

/**
 * Provides utilities to manage data in a Slime stream.
 */
public final class SlimeReaderUtil {

    private SlimeReaderUtil() {
        throw new AssertionError();
    }

    /**
     * Gets the block array index of the block specified
     * by the chunk section coordinates.
     *
     * @param x the chunk section x-coordinate
     * @param y the chunk section y-coordinate
     * @param z the chunk section z-coordinate
     * @return the index
     */
    public static int getBlockIndex(int x, int y, int z) {
        return x | y << 8 | z << 4;
    }

    /**
     * Converts the specified block and block data arrays to
     * internal block IDs.
     *
     * @param blockIds the array to write the ids to
     * @param blocks the block array
     * @param data the block data array
     */
    public static void readBlockIds(final char[] blockIds, final byte[] blocks, final NibbleArray data) {
        for (int i = 0; i < blockIds.length; i++) {
            int x = i & 0xF;
            int y = i >> 8 & 0xF;
            int z = i >> 4 & 0xF;

            int id = blocks[i] & 0xFF;
            int blockData = data.a(x, y, z);

            blockIds[i] = getBlockId(id, blockData);
        }
    }

    public static char getBlockId(int id, int blockData) {
        int packed = id << 4 | blockData;

        if (Block.d.a(packed) == null) {
            // Convert old block
            Block block = Block.getById(id);

            if (block != null) {
                try {
                    blockData = block.toLegacyData(block.fromLegacyData(blockData));
                } catch (Exception ignored) {
                    blockData = block.toLegacyData(block.getBlockData());
                }

                // Recompute packed ID
                packed = id << 4 | blockData;
            }
        }

        return (char) packed;
    }
}
