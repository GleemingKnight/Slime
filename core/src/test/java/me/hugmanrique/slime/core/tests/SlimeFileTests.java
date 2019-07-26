package me.hugmanrique.slime.core.tests;

import com.google.common.collect.ImmutableSet;
import me.hugmanrique.slime.core.data.ProtoSlimeChunk;
import me.hugmanrique.slime.core.data.SlimeFile;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Set;

import static me.hugmanrique.slime.core.tests.BlockAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

class SlimeFileTests {

    private static File SLIME_TEST_FILE = new File("src/test/resources/skyblock.slime");

    @BeforeAll
    static void initAll() {
        // Recalculating counts causes an "Accessed blocks before bootstrap" exception
        ProtoSlimeChunk.RECALC_BLOCK_COUNTS = false;
    }

    @Test
    void testFileRead() throws IOException {
        SlimeFile file = SlimeFile.read(SLIME_TEST_FILE);

        assertEquals(1, file.getVersion());

        assertEquals((short) 0xFFFB, file.getMinX(), "Lowest chunk X should be -5");
        assertEquals((short) 0xFFFF, file.getMinZ(), "Lowest chunk Z should be -1");
        assertEquals(6, file.getWidth());
        assertEquals(2, file.getDepth());

        BitSet populated = file.getPopulatedChunks();

        assertEquals(2, populated.toByteArray().length, "BitSet should have appropriate length");
        Set<Integer> shouldBePopulated = ImmutableSet.of(0, 4, 5, 6, 10, 11);

        // Check BitSet entries
        for (int i = 0; i < 16; i++) {
            boolean expected = shouldBePopulated.contains(i);

            assertEquals(expected, populated.get(i), "Chunk " + i + " populated data should be " + expected);
        }

        assertTrue(file.getEntities().isEmpty(), "Version 1 Slime file should have no entity data");

        // Check lava and water buckets chest
        NBTTagCompound initialChest = file.getTileEntities().get(0);

        assertEquals("Chest", initialChest.getString("id"));
        assertEquals(4, initialChest.getInt("x"));
        assertEquals(67, initialChest.getInt("y"));
        assertEquals(-3, initialChest.getInt("z"));
        assertNotNull(initialChest.getList("Items", 10));

        // Check proto chunks
        //
        // As no block data has been initialized, we must
        // be careful not to call methods that throw
        // "Accessed blocks before bootstrap" exceptions.

        ProtoSlimeChunk mainIsland = file.getProtoChunkAt(1, -3);

        assertNotNull(mainIsland); // Main island
        assertNotNull(file.getProtoChunkAt(-66, 0)); // Sand island
        assertNull(file.getProtoChunkAt(-28, -28)); // Inside region, but unpopulated
        assertNull(file.getProtoChunkAt(9999, 9999)); // Outside region

        assertEquals(new ChunkCoordIntPair(0, -1), mainIsland.getCoords());

        // Check biomes
        assertBiome(13, mainIsland, 1, 15); // Ice Mountains
        assertBiome(13, mainIsland, 4, 12); // Ice Mountains

        // Check heightmap
        assertHeight(67, mainIsland, 4, 13);
        assertHeight(67, mainIsland, 3, 12);
        assertHeight(0, mainIsland, 2, 11); // Void

        // Check section data
        ChunkSection[] sections = mainIsland.getSections();

        assertEquals(16, sections.length, "Chunk sections array length is correct");
        assertNull(sections[0], "First section should be non-populated");

        ChunkSection island = sections[4];

        assertNotNull(island, "Island section should be populated");
        assertEquals(4, island.getYPosition() >> 4);

        // Check blocks
        assertBlockEquals(2, island, 1, 2, 13); // Grass
        assertBlockEquals(3, island, 2, 1, 12); // Dirt
        assertBlockEquals(54, 4, island, 4, 3, 13); // Chest (facing west)
        assertBlockEquals(0, island, 0, 0, 0); // Air

        // Check skylight
        assertEquals(0, island.d(1, 0, 13)); // inside block
        assertEquals(15, island.d(3, 3, 14)); // above island

        // Check block light
        assertEquals(0, island.e(1, 0, 13)); // inside block
        assertEquals(0, island.e(3, 3, 14)); // above island
    }
}
