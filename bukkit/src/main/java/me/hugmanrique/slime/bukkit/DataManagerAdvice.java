package me.hugmanrique.slime.bukkit;

import me.hugmanrique.slime.core.SlimeChunkLoader;
import net.bytebuddy.asm.Advice;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;

import java.io.File;
import java.lang.reflect.Constructor;

public class DataManagerAdvice {

    private static final String CHUNK_LOADER_CLASSNAME = "me.hugmanrique.slime.core.SlimeChunkLoader";

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

        File chunksFile = new File(worldDir, SlimeChunkLoader.CHUNKS_FILENAME);

        if (chunksFile.exists()) {
            ClassLoader pluginClassLoader = Bukkit.getPluginManager().getPlugin("Slime").getClass().getClassLoader();

            try {
                Class<?> loaderClass = Class.forName(CHUNK_LOADER_CLASSNAME, true, pluginClassLoader);
                Constructor<?> loaderConstructor = loaderClass.getConstructor(File.class);

                returned = (IChunkLoader) loaderConstructor.newInstance(worldDir);

                // Set empty chunk generator
                EmptyChunkGenerator.setGenerator(manager.getUUID());
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Fallback to Anvil region loader
        returned = new ChunkRegionLoader(worldDir);
    }
}
