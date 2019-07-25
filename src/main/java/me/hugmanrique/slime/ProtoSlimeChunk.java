package me.hugmanrique.slime;

import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.NibbleArray;
import net.minecraft.server.v1_8_R3.World;

import java.io.IOException;
import java.util.BitSet;

/**
 * Represents a chunk that hasn't been loaded yet.
 */
public class ProtoSlimeChunk {

    private static final int HEIGHTMAP_LENGTH = 256;
    private static final int BIOMES_LENGTH = 256;
    private static final int SECTIONS_PER_CHUNK = 16;
    private static final int BLOCK_LIGHT_LENGTH = 2048;
    private static final int BLOCKS_LENGTH = 4096;
    private static final int BLOCK_DATA_LENGTH = 2048;
    private static final int SKYLIGHT_LENGTH = 2048;

    static ProtoSlimeChunk from(SlimeInputStream in, int chunkX, int chunkZ) throws IOException {
        int[] heightMap = in.readIntArray(HEIGHTMAP_LENGTH);
        byte[] biomes = in.readByteArray(BIOMES_LENGTH);

        // Read sections
        BitSet populatedSections = in.readBitSet(SECTIONS_PER_CHUNK);
        ChunkSection[] sections = new ChunkSection[SECTIONS_PER_CHUNK];

        for (int y = 0; y < SECTIONS_PER_CHUNK; y++) {
            if (!populatedSections.get(y)) {
                // Non-populated, leave as null
                continue;
            }

            int yPos = y << 4;
            ChunkSection section = new ChunkSection(yPos, true); // skylight

            NibbleArray blockLight = in.readNibbleArray(BLOCK_LIGHT_LENGTH);

            byte[] blocks = in.readByteArray(BLOCKS_LENGTH);
            NibbleArray data = in.readNibbleArray(BLOCK_DATA_LENGTH);
            char[] blockIds = SlimeReaderUtil.getBlockIds(blocks, data);

            NibbleArray skyLight = in.readNibbleArray(SKYLIGHT_LENGTH);

            section.a(blockIds);
            section.a(blockLight);
            section.b(skyLight);

            section.recalcBlockCounts();
            sections[y] = section;
        }

        return new ProtoSlimeChunk(chunkX, chunkZ, sections, biomes, heightMap);
    }

    private final int x;
    private final int z;

    private final ChunkSection[] sections;

    private final byte[] biomes;
    private final int[] heightMap;

    private ProtoSlimeChunk(int x, int z, ChunkSection[] sections, byte[] biomes, int[] heightMap) {
        this.x = x;
        this.z = z;
        this.sections = sections;
        this.biomes = biomes;
        this.heightMap = heightMap;
    }

    public Chunk toChunk(World world) {
        Chunk chunk = new Chunk(world, x, z);

        chunk.a(heightMap);
        chunk.d(true); // TerrainPopulated
        chunk.e(true); // LightPopulated
        chunk.c(0); // InhabitedTime

        chunk.a(sections);
        chunk.a(biomes);

        return chunk;
    }
}
