package me.hugmanrique.slime.core.tests;

import me.hugmanrique.slime.core.SlimeReaderUtil;
import me.hugmanrique.slime.core.data.ProtoSlimeChunk;
import me.hugmanrique.slime.core.data.SlimeFile;
import net.minecraft.server.v1_12_R1.ChunkSection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SlimeExtension.class)
class SlimeChunkSectionTests {

    private static ChunkSection section;

    @BeforeAll
    static void setUp(SlimeFile file) {
        ProtoSlimeChunk chunk = file.getProtoChunkAt(1, -3);

        section = chunk.getSections()[4];
    }

    private void assertBlockEquals(int expectedBlockId, int sectionX, int sectionY, int sectionZ) {
        assertBlockEquals(expectedBlockId, 0, sectionX, sectionY, sectionZ);
    }

    private void assertBlockEquals(int expectedBlockId, int expectedData, int sectionX, int sectionY, int sectionZ) {
        int nibbleIndex = SlimeReaderUtil.getBlockIndex(sectionX, sectionY, sectionZ);

        int expectedPackedId = SlimeReaderUtil.getBlockId(expectedBlockId, expectedData);
//        int actualPackedId = section.getBlocks().[nibbleIndex];

        assertEquals(expectedPackedId, 0);
    }

    @Test
    void checkYPos() {
        assertEquals(4, section.getYPosition() >> 4);
    }

    @Test
    void checkBlocks() {
        assertBlockEquals(2, 1, 2, 13); // Grass
        assertBlockEquals(3, 2, 1, 12); // Dirt
        assertBlockEquals(54, 4, 4, 3, 13); // Chest (facing west)
        assertBlockEquals(0, 0, 0, 0); // Air
    }

    @Test
    void checkSkylight() {
        assertEquals(0, section.b(1, 0, 13)); // inside block
        assertEquals(15, section.b(3, 3, 14)); // above island
    }

    @Test
    void checkBlockLight() {
        assertEquals(0, section.c(1, 0, 13)); // inside block
        assertEquals(0, section.c(3, 3, 14)); // above island
    }
}
