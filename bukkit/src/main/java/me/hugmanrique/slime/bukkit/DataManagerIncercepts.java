package me.hugmanrique.slime.bukkit;

import me.hugmanrique.slime.core.SlimeChunkLoader;
import net.minecraft.server.v1_8_R3.*;

import java.io.File;

/**
 * Provides {@link ServerNBTManager} method overrides.
 */
public class DataManagerIncercepts {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static IChunkLoader createChunkLoader(WorldProvider provider) {
        File container = SlimePlugin.worldContainer; // TODO Fix, should be manager.getDirectory();
        File worldDir = container;

        if (provider instanceof WorldProviderHell) {
            worldDir = new File(container, "DIM-1");
        } else if (provider instanceof WorldProviderTheEnd) {
            worldDir = new File(container, "DIM1");
        }

        worldDir.mkdirs();

        File chunksFile = new File(worldDir, SlimeChunkLoader.CHUNKS_FILENAME);

        if (!chunksFile.exists()) {
            // Fallback to default region loader
            return new ChunkRegionLoader(worldDir);
        }

        return new SlimeChunkLoader(worldDir);
    }

    public static void saveWorldData(WorldData worldData, NBTTagCompound nbtData) {
        // NOOP: saveWorldData
    }

    public static void saveWorldData(WorldData worldData) {
        // NOOP: saveWorldData
    }
}
