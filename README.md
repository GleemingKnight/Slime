# :eight_spoked_asterisk: Slime

Slime is a Minecraft world file format described by the [Hypixel](https://hypixel.net) developers on one of their [dev blogs](https://hypixel.net/threads/dev-blog-5-storing-your-skyblock-island.2190753/). This format was originally designed to store SkyBlock player islands. As so, the main focus of this format is small worlds, guaranteeing a higher compression rate, and using less disk space than Minecraft's default [Region file format](https://www.mojang.com/2011/02/minecraft-save-file-format-in-beta-1-3/).

This projects provides a [CraftBukkit](https://www.spigotmc.org/) Slime chunk loader based on "in-memory" worlds, which don't use disk storage at all. This is extremely useful for minigame servers, where the maps tend to be small (they fit nicely in memory), and no world saving is needed.

## How does it work?

We "inject" the custom chunk loader by providing a [`ServerNBTManager`](src/main/java/me/hugmanrique/slime/SlimeDataManager.java) that overrides the `createChunkLoader`. This method returns a `SlimeChunkLoader`, which is where the main program logic lies.

When the server starts, this loader reads the Slime world file located at `<world-dir>/chunks.slime` and creates `ProtoSlimeChunk` instances, which contain the raw chunk information.
Whenever the chunk is requested by the server, this proto chunk is converted into a regular NMS `Chunk`, with all its entities, blocks, skylight information...

This approach was taken for two reasons:

1. Instantiating NMS `Chunk`s on startup uses much more CPU and memory than proto chunks. These only store the strictly necessary info in primitives.
2. On the other hand, waiting for a chunk load request to read the Slime file is too slow. Instead, the file reading is performed during startup. Converting a proto chunk into a NMS `Chunk` is really cheap, since the data is already in memory.

## Limitations

There's limited available information about the Slime format, so some assumptions were made. For example, the blog post doesn't detail the storage structure of multiple(?) slime files. We assumed they all fit in a single file, `<world-dir>/chunks.slime`.

The provided [slime-tools](https://staticassets.hypixel.net/news/5d37b611d4298.slime-tools.jar) contains a **saving** reference implementation, and seems to be for version 1, since it doesn't include entity information.
This project supports both version `1` and `3` ([spec](https://pastebin.com/raw/EVCNAmkw)) of the format.

## How do I use this?

This project is under heavy development and testing, so no CraftBukkit/Spigot/Paper patches are available as of now.

## Can I contribute?

Yes! We need to perform extensive testing before this project becomes stable. Please, feel free to fix bugs, add documentation, tests...

Note you will need to compile the Spigot 1.8.8 server via [BuildTools](https://www.spigotmc.org/wiki/buildtools/) to get a copy of the `spigot` dependency. 

## License

[MIT](LICENSE) &copy; [Hugo Manrique](https://hugmanrique.me)
