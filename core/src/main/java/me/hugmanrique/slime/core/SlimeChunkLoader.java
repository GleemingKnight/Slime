package me.hugmanrique.slime.core;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.v1_8_R3.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class SlimeChunkLoader implements IChunkLoader {

    private static final String CHUNKS_FILENAME = "chunks.slime";

    private static final short SLIME_HEADER = (short) 0xB10B;
    private static final Set<Integer> SUPPORTED_VERSIONS = ImmutableSet.of(1, 3);

    private final File directory;

    private final Map<ChunkCoordIntPair, ProtoSlimeChunk> protoChunks;
    private final Map<ChunkCoordIntPair, Chunk> loadedChunks;

    SlimeChunkLoader(File directory) {
        this.directory = requireNonNull(directory, "directory");
        this.protoChunks = new HashMap<>();
        this.loadedChunks = new HashMap<>(); // Chunk loads are always in main thread

        try {
            readChunksFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void readChunksFile() throws IOException {
        File chunksFile = new File(directory, CHUNKS_FILENAME);

        if (!chunksFile.exists()) {
            throw new IllegalStateException("Slime chunk file doesn't exist");
        }

        try (SlimeInputStream in = new SlimeInputStream(
                new FileInputStream(chunksFile))) {
            short header = in.readShort();

            checkArgument(header == SLIME_HEADER, "Expected Slime header, got %s", header);

            int version = in.read();
            checkArgument(SUPPORTED_VERSIONS.contains(version), "Unsupported Slime version %s", version);

            short minX = in.readShort(); // Chunk lowest x-coordinate
            short minZ = in.readShort(); // Chunk lowest z-coordinate

            int width = in.readShort(); // X-axis length
            int depth = in.readShort(); // Z-axis length

            int bitSetLength = ((width * depth) / 8) + 1;
            BitSet populatedChunks = in.readBitSet(bitSetLength);

            byte[] chunkData = in.readCompressed();

            NBTTagCompound tileEntities = in.readCompressedCompound();
            NBTTagCompound entities = null;

            if (version == 3) {
                boolean hasEntities = in.readBoolean();

                if (hasEntities) {
                    entities = in.readCompressedCompound();
                }

                NBTTagCompound extra = in.readCompressedCompound();

                // TODO Store extra data?
            }

            ProtoSlimeRegion region = new ProtoSlimeRegion(minX, minZ, width, depth, populatedChunks, chunkData);

            createProtoChunks(region);
            loadEntityData(tileEntities, entities);
        }
    }

    private void createProtoChunks(ProtoSlimeRegion region) throws IOException {
        SlimeInputStream stream = region.getDataStream();
        BitSet populated = region.getPopulated();

        for (int i = 0; i < populated.length(); i++) {
            if (!populated.get(i)) {
                // Non-populated, skip
                return;
            }

            ChunkCoordIntPair coords = region.getChunkCoords(i);

            ProtoSlimeChunk chunk = ProtoSlimeChunk.from(stream, coords);
            protoChunks.put(coords, chunk);
        }
    }

    private ProtoSlimeChunk getProtoChunkAt(int x, int z) {
        ChunkCoordIntPair coords = new ChunkCoordIntPair(x >> 4, z >> 4);

        return protoChunks.get(coords);
    }

    private void loadEntityData(NBTTagCompound tilesCompound, @Nullable NBTTagCompound entitiesCompound) {
        NBTTagList tileEntities = tilesCompound.getList("tiles", 10);
        NBTTagList entities;

        if (entitiesCompound != null) {
            entities = entitiesCompound.getList("entities", 10);
        } else {
            entities = new NBTTagList();
        }

        // Add each entity to its corresponding ProtoChunk
        for (int i = 0; i < entities.size(); i++) {
            NBTTagCompound compound = entities.get(i);

            NBTTagList position = compound.getList("Pos", 6);

            int x = (int) position.d(0);
            int z = (int) position.d(2);

            ProtoSlimeChunk chunk = getProtoChunkAt(x, z);

            if (chunk == null) {
                // TODO Warn?
                continue;
            }

            chunk.addEntity(compound);
        }

        // Add each tile entity to its corresponding ProtoChunk
        for (int i = 0; i < tileEntities.size(); i++) {
            NBTTagCompound compound = tileEntities.get(i);

            int x = compound.getInt("x");
            int z = compound.getInt("z");

            ProtoSlimeChunk chunk = getProtoChunkAt(x, z);

            if (chunk == null) {
                // TODO Warn?
                continue;
            }

            chunk.addTileEntity(compound);
        }
    }

    /**
     * Converts a proto chunk into a Minecraft chunk.
     *
     * @param world the world
     * @param coords the chunk coordinates
     * @return the loaded chunk, or {@code null} if couldn't load
     */
    private Chunk loadChunk(World world, ChunkCoordIntPair coords) {
        ProtoSlimeChunk proto = protoChunks.remove(coords);

        if (proto == null) {
            return null;
        }

        Chunk chunk = proto.toChunk(world);
        loadedChunks.put(coords, chunk);

        return chunk;
    }

    /**
     * Load the chunk at the specified coordinates.
     *
     * @param world the world
     * @param chunkX the chunk x-coordinate
     * @param chunkZ the chunk z-coordinate
     * @return the loaded chunk, or {@code null} if couldn't load
     * @throws IOException if an error occurs while reading files
     */
    @Override
    public Chunk a(World world, int chunkX, int chunkZ) throws IOException {
        world.timings.syncChunkLoadDataTimer.startTiming();

        ChunkCoordIntPair coords = new ChunkCoordIntPair(chunkX, chunkZ);
        Chunk chunk = loadedChunks.get(coords);

        if (chunk == null) {
            chunk = loadChunk(world, coords);
        }

        world.timings.syncChunkLoadDataTimer.stopTiming();
        return chunk;
    }

    /**
     * Writes the chunk to disk.
     *
     * @param world the world
     * @param chunk the chunk to save
     * @throws IOException if an error occurs while writing to disk
     * @throws ExceptionWorldConflict if a world conflict occurs
     */
    @Override
    public void a(World world, Chunk chunk) throws IOException, ExceptionWorldConflict {
        throw new UnsupportedOperationException("This loader does not support chunk saving");
    }


    @Override
    public void b(World world, Chunk chunk) throws IOException {

    }

    @Override
    public void a() {
        // NOOP: chunkTick
    }

    @Override
    public void b() {
        // NOOP: saveExtraData
    }
}
