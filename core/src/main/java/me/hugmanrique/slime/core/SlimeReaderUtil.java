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
     * Converts the specified block and block data arrays to
     * internal block IDs.
     *
     * @param blocks the block array
     * @param data the block data array
     * @return the converted block IDs
     */
    public static char[] getBlockIds(final byte[] blocks, final NibbleArray data) {
        char[] blockIds = new char[blocks.length];

        for (int i = 0; i < blockIds.length; i++) {
            int x = i & 0xF;
            int y = i >> 8 & 0xF;
            int z = i >> 4 & 0xF;

            int id = blocks[i] & 0xFF;
            int blockData = data.a(x, y, z);

            int packed = id << 4 | blockData;

            if (Block.d.a(packed) == null) {
                // Convert old blocks
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

            blockIds[i] = (char) packed;
        }

        return blockIds;
    }
}
