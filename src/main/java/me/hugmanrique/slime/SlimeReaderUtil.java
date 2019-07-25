package me.hugmanrique.slime;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.NibbleArray;

/**
 * Provides utilities to manage data in a Slime stream.
 */
final class SlimeReaderUtil {

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
    static char[] getBlockIds(final byte[] blocks, final NibbleArray data) {
        char[] blockIds = new char[blocks.length];

        for (int i = 0; i < blockIds.length; i++) {
            // TODO Figure out what each value does
            int i1 = i & 0xF;
            int j1 = i >> 8 & 0xF;
            int k1 = i >> 4 & 0xF;

            int id = blocks[i] & 0xFF;
            int blockData = data.a(i1, j1, k1);

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
