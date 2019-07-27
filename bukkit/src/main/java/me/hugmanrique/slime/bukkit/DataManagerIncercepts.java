package me.hugmanrique.slime.bukkit;

import me.hugmanrique.slime.core.SlimeChunkLoader;
import net.bytebuddy.implementation.bind.annotation.This;
import net.minecraft.server.v1_8_R3.*;

import java.io.File;

/**
 * Provides {@link ServerNBTManager} method overrides.
 */
public class DataManagerIncercepts {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static IChunkLoader interceptCreate(@This ServerNBTManager manager, WorldProvider provider) {
        File container = manager.getDirectory();
        File worldDir;

        // TODO Gracefully fallback to default ChunkRegionLoader

        if (provider instanceof WorldProviderHell) {
            worldDir = new File(container, "DIM-1");
            worldDir.mkdirs();

            return new SlimeChunkLoader(worldDir);
        } else if (provider instanceof WorldProviderTheEnd) {
            worldDir = new File(container, "DIM1");
            worldDir.mkdirs();

            return new SlimeChunkLoader(worldDir);
        }

        return new SlimeChunkLoader(container);
    }

    public static void interceptSave(WorldData worldData, NBTTagCompound nbtData) {
        // NOOP: saveWorldData
    }
}
