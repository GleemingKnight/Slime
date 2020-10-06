package me.hugmanrique.slime.core;

import me.hugmanrique.slime.core.data.ProtoSlimeChunk;
import me.hugmanrique.slime.core.data.SlimeFile;
import net.minecraft.server.v1_12_R1.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * A Slime chunk loader based on "in-memory" worlds.
 */
public class SlimeChunkLoader implements IChunkLoader {

    public static final String CHUNKS_FILENAME = "chunks.slime";

    private final Map<ChunkCoordIntPair, ProtoSlimeChunk> protoChunks;
    private final Map<ChunkCoordIntPair, Chunk> loadedChunks;

    public SlimeChunkLoader(File worldDir) {
        requireNonNull(worldDir, "directory");
        File chunksFile = new File(worldDir, CHUNKS_FILENAME);

        this.loadedChunks = new ConcurrentHashMap<>(); // Paper loads chunks asynchronously

        try {
            SlimeFile file = SlimeFile.read(chunksFile);

            this.protoChunks = file.getProtoChunks();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the proto chunk at the specified coordinates
     * into a Minecraft chunk.
     *
     * @param world the world the chunk is in
     * @param coords the proto chunk coordinates
     * @return the loaded chunk, or {@code null} if not populated
     */
    private Chunk loadProtoChunk(World world, ChunkCoordIntPair coords) {
        ProtoSlimeChunk proto = protoChunks.remove(coords);

        if (proto == null) {
            return null;
        }

        Chunk loaded = proto.load(world);
        loadedChunks.put(coords, loaded);

        return loaded;
    }

    /**
     * Loads the chunk at the specified chunk coordinates.
     *
     * @param world the world
     * @param chunkX the chunk x-coordinate
     * @param chunkZ the chunk z-coordinate
     * @return the loaded chunk, or {@code null} if couldn't load
     */
    @Override
    public Chunk a(World world, int chunkX, int chunkZ) {
        world.timings.syncChunkLoadDataTimer.startTiming();

        ChunkCoordIntPair coords = new ChunkCoordIntPair(chunkX, chunkZ);
        Chunk chunk = loadedChunks.get(coords);

        if (chunk == null) {
            chunk = loadProtoChunk(world, coords);
        }

        world.timings.syncChunkLoadDataTimer.stopTiming();

        return chunk;
    }

    public void saveChunk(World world, Chunk chunk, boolean b) {

    }

    public void c() {

    }

    @Override
    public boolean chunkExists(int i, int i1) {
        return false;
    }

    @Override
    public void b() {
        // NOOP: saveExtraData
    }
}
