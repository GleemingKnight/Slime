package me.hugmanrique.slime.core.tests;

import me.hugmanrique.slime.core.data.ProtoSlimeChunk;
import net.minecraft.server.v1_8_R3.ChunkSection;

import static me.hugmanrique.slime.core.SlimeReaderUtil.getBlockId;
import static me.hugmanrique.slime.core.SlimeReaderUtil.getBlockIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class BlockAssertions {

    static void assertBlockEquals(int expectedBlockId, ChunkSection section, int sectionX, int sectionY, int sectionZ) {
        assertBlockEquals(expectedBlockId, 0, section, sectionX, sectionY, sectionZ);
    }

    static void assertBlockEquals(int expectedBlockId, int expectedData, ChunkSection section, int sectionX, int sectionY, int sectionZ) {
        int nibbleIndex = getBlockIndex(sectionX, sectionY, sectionZ);
        int expectedPackedId = getBlockId(expectedBlockId, expectedData);

        int actualId = section.getIdArray()[nibbleIndex];

        assertEquals(expectedPackedId, actualId, "Block should correct packed ID");
    }

    static void assertBiome(int expectedBiome, ProtoSlimeChunk protoChunk, int sectionX, int sectionZ) {
        byte x = (byte) sectionX;
        byte z = (byte) sectionZ;

        int actual = protoChunk.getBiomes()[z << 4 | x];

        assertEquals(actual, expectedBiome, "Column should have correct biome ID");
    }
}
