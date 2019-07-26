package me.hugmanrique.slime.core.data;

import com.google.common.collect.ImmutableSet;
import me.hugmanrique.slime.core.SlimeInputStream;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * Represents a Slime world file.
 */
public class SlimeFile {

    private static final short FILE_HEADER = (short) 0xB10B;
    private static final Set<Integer> SUPPORTED_VERSIONS = ImmutableSet.of(1, 3);

    public static SlimeFile read(File file) throws IOException {
        requireNonNull(file, "file");
        checkArgument(file.exists(), "Slime world file doesn't exist");

        try (SlimeInputStream in = new SlimeInputStream(
                new FileInputStream(file))) {

            short header = in.readShort();
            checkArgument(header == FILE_HEADER, "Expected Slime header, got %s", header);

            int version = in.read();
            checkArgument(SUPPORTED_VERSIONS.contains(version), "Unsupported Slime version %s", version);

            // Lowest chunk coordinates
            short minX = in.readShort();
            short minZ = in.readShort();

            // X-axis and Z-axis length respectively
            int width = in.readShort();
            int depth = in.readShort();

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

                // Skip extra data
                in.skipCompressed();
            }

            return new SlimeFile(
                version,
                minX,
                minZ,
                width,
                depth,
                populatedChunks,
                chunkData,
                entities != null ? entities.getList("entities", 10) : null,
                tileEntities != null ? tileEntities.getList("tiles", 10) : null
            );
        }
    }

    private final int version;

    /**
     * Lowest chunk x-coordinate
     */
    private final short minX;

    /**
     * Lowest chunk z-coordinate
     */
    private final short minZ;

    /**
     * X-axis length, in chunks
     */
    private final int width;

    /**
     * Z-axis length, in chunks
     */
    private final int depth;

    private final BitSet populatedChunks;
    private final byte[] chunkData;

    private final NBTTagList entities;
    private final NBTTagList tileEntities;

    private Map<ChunkCoordIntPair, ProtoSlimeChunk> protoChunks;

    private SlimeFile(int version, short minX, short minZ, int width, int depth, BitSet populatedChunks, byte[] chunkData, NBTTagList entities, NBTTagList tileEntities) throws IOException {
        this.version = version;
        this.minX = minX;
        this.minZ = minZ;
        this.width = width;
        this.depth = depth;
        this.populatedChunks = requireNonNull(populatedChunks, "populatedChunks");
        this.chunkData = requireNonNull(chunkData, "chunkData");
        this.entities = entities != null ? entities : new NBTTagList();
        this.tileEntities = tileEntities != null ? tileEntities : new NBTTagList();
        this.protoChunks = new HashMap<>();

        createProtoChunks();
    }

    private ChunkCoordIntPair getChunkCoords(int bitIndex) {
        return new ChunkCoordIntPair(
                minX + (bitIndex / width) * width + bitIndex % width, minZ + bitIndex / width);
    }

    /**
     * Gets the proto chunk at the specified block coordinates.
     *
     * @param x the x-coordinate
     * @param z the z-coordinate
     * @return the proto chunk, or {@code null} if not populated
     */
    public ProtoSlimeChunk getProtoChunkAt(int x, int z) {
        ChunkCoordIntPair coords = new ChunkCoordIntPair(x >> 4, z >> 4);

        return protoChunks.get(coords);
    }

    private void createProtoChunks() throws IOException {
        SlimeInputStream stream = new SlimeInputStream(
                new ByteArrayInputStream(chunkData));

        for (int i = 0; i < populatedChunks.length(); i++) {
            if (!populatedChunks.get(i)) {
                // Non-populated chunk
                continue;
            }

            ChunkCoordIntPair coords = getChunkCoords(i);
            ProtoSlimeChunk chunk = ProtoSlimeChunk.read(stream, coords);

            protoChunks.put(coords, chunk);
        }

        loadEntities();
    }

    private void loadEntities() {
        // Add each entity to its proto chunk
        for (int i = 0; i < entities.size(); i++) {
            NBTTagCompound entityData = entities.get(i);
            NBTTagList position = entityData.getList("Pos", 6);

            int x = (int) position.d(0);
            int z = (int) position.d(2);

            ProtoSlimeChunk chunk = getProtoChunkAt(x, z);

            if (chunk != null) {
                chunk.addEntity(entityData);
            }
        }

        // Add each tile entity to its proto chunk
        for (int i = 0; i < tileEntities.size(); i++) {
            NBTTagCompound tileData = tileEntities.get(i);

            int x = tileData.getInt("x");
            int z = tileData.getInt("z");

            ProtoSlimeChunk chunk = getProtoChunkAt(x, z);

            if (chunk != null) {
                chunk.addTileEntity(tileData);
            }
        }
    }

    public Map<ChunkCoordIntPair, ProtoSlimeChunk> getProtoChunks() {
        return protoChunks;
    }

    public int getVersion() {
        return version;
    }

    public short getMinX() {
        return minX;
    }

    public short getMinZ() {
        return minZ;
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public BitSet getPopulatedChunks() {
        return populatedChunks;
    }

    public byte[] getChunkData() {
        return chunkData;
    }

    public NBTTagList getEntities() {
        return entities;
    }

    public NBTTagList getTileEntities() {
        return tileEntities;
    }
}
