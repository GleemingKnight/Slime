package me.hugmanrique.slime.bukkit;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class EmptyChunkGenerator extends ChunkGenerator {

    @Override
    public byte[][] generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
        // Leave all sections as null, meaning they aren't populated.
        return new byte[world.getMaxHeight() / 16][];
    }
}
