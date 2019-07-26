package me.hugmanrique.slime.core.tests;

import me.hugmanrique.slime.core.data.ProtoSlimeChunk;
import me.hugmanrique.slime.core.data.SlimeFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        // TODO Test bitset
        BitSet populated = file.getPopulatedChunks();

        assertEquals(file.getWidth() * file.getDepth() / 8, populated.length(), "BitSet should have an entry for each chunk");
        int[] shouldBePopulated = new int[] {}

    }
}
