package me.hugmanrique.slime;

import net.minecraft.server.v1_8_R3.*;

import java.io.File;

public class SlimeDataManager extends ServerNBTManager {

    public SlimeDataManager(File file, String s, boolean b) {
        super(file, s, b);
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
    public void saveWorldData(WorldData worldData, NBTTagCompound nbtTagCompound) {
        // Don't save world
    }
}
