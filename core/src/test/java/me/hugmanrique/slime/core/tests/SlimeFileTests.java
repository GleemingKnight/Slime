package me.hugmanrique.slime.core.tests;

import com.google.common.collect.ImmutableSet;
import me.hugmanrique.slime.core.data.ProtoSlimeChunk;
import me.hugmanrique.slime.core.data.SlimeFile;
import net.minecraft.server.v1_12_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.BitSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SlimeExtension.class)
class SlimeFileTests {

    private static SlimeFile file;

    @BeforeAll
    static void setUp(SlimeFile file) {
        SlimeFileTests.file = file;
    }

    @Test
    void checkHeader() {
        assertEquals(1, file.getVersion());

        assertEquals((short) 0xFFFB, file.getMinX(), "Lowest chunk X should be -5");
        assertEquals((short) 0xFFFF, file.getMinZ(), "Lowest chunk Z should be -1");
        assertEquals(6, file.getWidth());
        assertEquals(2, file.getDepth());
    }

    @Test
    void checkPopulatedChunksBitSet() {
        BitSet populated = file.getPopulatedChunks();

        assertEquals(2, populated.toByteArray().length, "BitSet should have appropriate length");
        Set<Integer> shouldBePopulated = ImmutableSet.of(0, 4, 5, 6, 10, 11);

        // Check BitSet entries
        for (int i = 0; i < 16; i++) {
            boolean expected = shouldBePopulated.contains(i);

            assertEquals(expected, populated.get(i), "Chunk " + i + " populated data should be " + expected);
        }
    }

    @Test
    void testProtoChunkReads() {
        ProtoSlimeChunk chunk = file.getProtoChunkAt(1, -3);

        assertNotNull(chunk); // Main island
        assertNotNull(file.getProtoChunkAt(-66, 0)); // Sand island
        assertNull(file.getProtoChunkAt(-28, -28)); // Inside region, but unpopulated
        assertNull(file.getProtoChunkAt(9999, 9999)); // Outside region

        assertEquals(new ChunkCoordIntPair(0, -1), chunk.getCoords());
    }

    @Test
    void checkEntities() {
        assertTrue(file.getEntities().isEmpty(), "Version 1 Slime file should have no entity data");
    }

    @Test
    void checkEntityData() {
        // Check lava and water buckets chest
        NBTTagCompound initialChest = file.getTileEntities().get(0);

        assertEquals("Chest", initialChest.getString("id"));
        assertEquals(4, initialChest.getInt("x"));
        assertEquals(67, initialChest.getInt("y"));
        assertEquals(-3, initialChest.getInt("z"));
        assertNotNull(initialChest.getList("Items", 10));
    }
}
