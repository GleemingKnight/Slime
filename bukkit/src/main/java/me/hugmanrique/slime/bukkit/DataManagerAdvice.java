package me.hugmanrique.slime.bukkit;

import me.hugmanrique.slime.core.SlimeChunkLoader;
import net.bytebuddy.asm.Advice;
import net.minecraft.server.v1_8_R3.*;

import java.io.File;

public class DataManagerAdvice {

    @Advice.OnMethodExit
    public static void createChunkLoader(WorldProvider provider, @Advice.Return(readOnly = false) IChunkLoader returned, @Advice.This ServerNBTManager manager) {
        File container = manager.getDirectory();
        File worldDir = container;

        if (provider instanceof WorldProviderHell) {
            worldDir = new File(container, "DIM-1");
        } else if (provider instanceof WorldProviderTheEnd) {
            worldDir = new File(container, "DIM1");
        }

        //noinspection ResultOfMethodCallIgnored
        worldDir.mkdirs();

        System.out.println("Loading Slime world " + container.getName());

        File chunksFile = new File(worldDir, SlimeChunkLoader.CHUNKS_FILENAME);

        if (chunksFile.exists()) {
            returned = new SlimeChunkLoader(worldDir);
        } else {
            System.out.println("Cannot find Slime chunks file, falling back to Anvil region files");

            returned = new ChunkRegionLoader(worldDir);
        }
    }
}
