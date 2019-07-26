package me.hugmanrique.slime.core;

import net.minecraft.server.v1_8_R3.*;

import java.io.File;

/**
 * A ServerNBTManager that provides {@link SlimeChunkLoader}s.
 */
public class SlimeDataManager extends ServerNBTManager {

    public SlimeDataManager(File worldContainer, String worldName, boolean flag) {
        super(worldContainer, worldName, flag);
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public IChunkLoader createChunkLoader(WorldProvider provider) {
        File container = getDirectory();
        File worldDir;

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

    @Override
    public void saveWorldData(WorldData worldData, NBTTagCompound nbtData) {
        // Don't save world
    }
}
