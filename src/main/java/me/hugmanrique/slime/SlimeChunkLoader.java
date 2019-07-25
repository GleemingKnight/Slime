package me.hugmanrique.slime;

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

                // TODO Store data
            }

            ProtoSlimeRegion region = new ProtoSlimeRegion(minX, minZ, width, depth, populatedChunks, chunkData);

            createProtoChunks(region);
            setEntityData(tileEntities, entities);
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

            int chunkX = region.getChunkX(i);
            int chunkZ = region.getChunkZ(i);

            ChunkCoordIntPair coords = new ChunkCoordIntPair(chunkX, chunkZ);
            ProtoSlimeChunk chunk = ProtoSlimeChunk.from(stream, coords);

            protoChunks.put(coords, chunk);
        }
    }

    private ProtoSlimeChunk getProtoChunkAt(int x, int z) {
        ChunkCoordIntPair coords = new ChunkCoordIntPair(x >> 4, z >> 4);

        return protoChunks.get(coords);
    }

    private void setEntityData(NBTTagCompound tilesCompound, @Nullable NBTTagCompound entitiesCompound) {
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

    /*private BitSet getPopulatedChunksMask(DataInputStream in, int width, int depth) throws IOException {
        int maskByteCount = ((width * depth) / 8) + 1;

        byte[] rawPopulated = new byte[maskByteCount];
        in.read(rawPopulated);

        return BitSet.valueOf(rawPopulated);
    }

    private byte[] readCompressed(DataInputStream in) throws IOException {
        int compressedSize = in.readInt();
        int uncompressedSize = in.readInt();

        byte[] compressedData = new byte[compressedSize];
        in.read(compressedData);

        byte[] data = Zstd.decompress(compressedData, compressedSize);

        checkArgument(data.length == uncompressedSize, "Uncompressed size doesn't match");

        return data;
    }

    private void readChunkData(DataInputStream in, World world, int chunkX, int chunkZ) throws IOException {
        Chunk chunk = new Chunk(world, chunkX, chunkZ);

        // Read heightmap
        for (int i = 0; i < chunk.heightMap.length; i++) {
            chunk.heightMap[i] = in.readInt();
        }

        // Read biome data
        in.read(chunk.getBiomeIndex());

        // Read sections
        byte[] rawPopulated = new byte[16];
        in.read(rawPopulated);

        BitSet populatedSections = BitSet.valueOf(rawPopulated);
        ChunkSection[] sections = new ChunkSection[16];

        for (int i = 0; i < 16; i++) {
            if (!populatedSections.get(i)) {
                continue;
            }

            ChunkSection section = new ChunkSection(i << 4, true);

            // Block light
            byte[] rawBlockLight = new byte[2048];
            in.read(rawBlockLight);

            NibbleArray blockLight = new NibbleArray(rawBlockLight);

            // Blocks
            byte[] blocks = new byte[4096];
            in.read(blocks);

            // Data
            byte[] rawData = new byte[2048];
            in.read(rawData);

            NibbleArray data = new NibbleArray(rawData);

            // Skylight
            byte[] rawSkylight = new byte[2048];
            in.read(rawSkylight);

            NibbleArray skyLight = new NibbleArray(rawSkylight);

            int hypixelBlocksLength = in.readShort();

            // The data format of Hypixel blocks is not documented
            in.skipBytes(hypixelBlocksLength);

            // Convert block data to IDs
            char[] blockIds = new char[blocks.length];

            for (int j = 0; j < blockIds.length; j++) {
                int i1 = j & 15;
                int j1 = j >> 8 & 15;
                int k1 = j >> 4 & 15;
                int id = blocks[j] & 255;
                /*
                int blockData = data.a(i1, j1, k1);
                int packed = id << 4 | blockData;

                if (Block.d.a(packed) == null) {
                    Block block = Block.getById(id);

                    if (block != null) {
                        try {
                            blockData = block.toLegacyData(block.fromLegacyData(blockData));
                        } catch (Exception e) {
                            blockData = block.toLegacyData(block.getBlockData());
                        }

                        packed = id << 4 | blockData;
                    }
                }*/

                /*blockIds[j] = (char) packed;
            }

            section.a(blockIds);

            section.a(blockLight);
            section.b(skyLight);

            section.recalcBlockCounts();
            sections[i] = section;
        }
    }*/

    /*private void loadChunks() throws IOException {
        File chunksFile = new File(directory, CHUNKS_FILENAME);

        if (!chunksFile.exists()) {
            throw new IllegalStateException("Slime chunk file does not exist");
        }

        try (DataInputStream in = new DataInputStream(
                new FileInputStream(chunksFile))) {

            short header = in.readShort();
            checkArgument(header == SLIME_HEADER, "Expected Slime header %s, got %s", header, SLIME_HEADER);

            int version = in.read();
            checkArgument(version == SLIME_VERSION, "Unsupported Slime version %s", version);

            short minX = in.readShort(); // Chunk lowest x-coordinate
            short minZ = in.readShort(); // Chunk lowest z-coordinate
            int width = in.readShort(); // Length in x axis
            int depth = in.readShort(); // Length in z axis

            BitSet populatedChunks = getPopulatedChunksMask(in, width, depth);

            byte[] chunkData = readCompressed(in);
            byte[] tileEntities = readCompressed(in);

            ByteArrayInputStream chunkStream = new ByteArrayInputStream(chunkData);

            readChunkData(new DataInputStream(chunkStream));







        }
    }*/

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
        //world.timings.syncChunkLoadDataTimer.startTiming();

        return null;
    }

    @Override
    public void a(World world, Chunk chunk) throws IOException, ExceptionWorldConflict {
        throw new UnsupportedOperationException("This loader does not support chunk saving");
    }

    @Override
    public void b(World world, Chunk chunk) throws IOException {

    }

    @Override
    public void a() {

    }

    @Override
    public void b() {
        // Save all is a NOOP
    }
}
