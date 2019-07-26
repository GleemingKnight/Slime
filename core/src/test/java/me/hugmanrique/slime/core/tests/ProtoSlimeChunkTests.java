package me.hugmanrique.slime.core.tests;

import me.hugmanrique.slime.core.data.ProtoSlimeChunk;
import me.hugmanrique.slime.core.data.SlimeFile;
import net.minecraft.server.v1_8_R3.ChunkSection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SlimeExtension.class)
class ProtoSlimeChunkTests {

    private static ProtoSlimeChunk chunk;

    @BeforeAll
    static void setUp(SlimeFile file) {
        chunk = file.getProtoChunkAt(1, -3);
    }

    private void assertHeight(int expectedHeight, int chunkX, int chunkZ) {
        byte x = (byte) chunkX;
        byte z = (byte) chunkZ;

        int actualHeight = chunk.getHeightMap()[z << 4 | x];

        assertEquals(expectedHeight, actualHeight);
    }

    @SuppressWarnings("SameParameterValue")
    private void assertBiome(int expectedBiome, int chunkX, int chunkZ) {
        byte x = (byte) chunkX;
        byte z = (byte) chunkZ;

        int actualBiome = chunk.getBiomes()[z << 4 | x];

        assertEquals(expectedBiome, actualBiome);
    }

    @Test
    void testSectionReads() {
        ChunkSection[] sections = chunk.getSections();

        assertEquals(16, sections.length, "Chunk sections array length is correct");
        assertNull(sections[0], "First section should be non-populated");
        assertNotNull(sections[4], "Island section should be populated");
    }

    @Test
    void checkBiomes() {
        assertBiome(13, 1, 15); // Ice Mountains
        assertBiome(13, 4, 12); // Ice Mountains
    }

    @Test
    void checkHeightMap() {
        assertHeight(67, 4, 13);
        assertHeight(67, 3, 12);
        assertHeight(0, 2, 11); // void
    }
}
