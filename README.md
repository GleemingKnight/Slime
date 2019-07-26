# :eight_spoked_asterisk: Slime

Slime is a Minecraft world file format described by the [Hypixel](https://hypixel.net) developers on one of their [dev blogs](https://hypixel.net/threads/dev-blog-5-storing-your-skyblock-island.2190753/). This format was originally designed to store SkyBlock player islands. As so, the main focus of this format is small worlds, guaranteeing a higher compression rate, and using less disk space than Minecraft's default [Region file format](https://www.mojang.com/2011/02/minecraft-save-file-format-in-beta-1-3/).

This projects provides a [CraftBukkit](https://www.spigotmc.org/) Slime chunk loader based on "in-memory" worlds, which don't use disk storage at all. This is extremely useful for minigame servers, where the maps tend to be small (they fit nicely in memory), and no world saving is needed.

## How does it work?

We "inject" the custom chunk loader by providing a [`ServerNBTManager`](core/src/main/java/me/hugmanrique/slime/core/SlimeDataManager.java) that overrides the `createChunkLoader`. This method returns a `SlimeChunkLoader`, which is where the main program logic lies.

When the server starts, this loader reads the Slime world file located at `<world-dir>/chunks.slime` and creates `ProtoSlimeChunk` instances, which contain the raw chunk information.
Whenever the chunk is requested by the server, this proto chunk is converted into a regular NMS `Chunk`, with all its entities, blocks, skylight information...

This approach was taken for two reasons:

1. Instantiating NMS `Chunk`s on startup uses much more CPU and memory than proto chunks. These only store the strictly necessary info in primitives.
2. On the other hand, waiting for a chunk load request to read the Slime file is too slow. Instead, the file reading is performed during startup. Converting a proto chunk into a NMS `Chunk` is really cheap, since the data is already in memory.

Once a chunk is loaded, it will be kept in memory until the server shuts down. Successive loads will return the cached chunk instead of performing the proto conversion.

## Limitations

There's limited available information about the Slime format, so some assumptions were made. For example, the blog post doesn't detail the storage structure of multiple(?) slime files. We assumed they all fit in a single file, `<world-dir>/chunks.slime`.

The provided [slime-tools](https://staticassets.hypixel.net/news/5d37b611d4298.slime-tools.jar) JAR contains a **saving** reference implementation, and seems to be for version 1, since it doesn't include entity information.
This project supports both version `1` and `3` ([spec](https://pastebin.com/raw/EVCNAmkw)) of the format.

## Current status

The main Slime chunk loader has been written and tested. We plan on releasing a Bukkit plugin that injects the custom `ServerNBTManager`.

## Credits

- The Hypixel team ([Minikloon](https://minikloon.net/blog/) in particular) for creating this world format.
- The JUnit team for creating the [JUnit 5](https://junit.org/junit5/) testing framework.
- The [Byte Buddy](https://bytebuddy.net/#/) project contributors for their code generation library.
- [Noobcrew](https://www.minecraftforum.net/members/Noobcrew) for creating the [SkyBlock](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/maps/1473433-surv-skyblock) map (which we run tests against).

## Contributing

Contributions are more than welcome. This project hasn't been fully tested, so please feel free to fix bugs, add documentation, tests... 

Note you will need to manually run [BuildTools](https://www.spigotmc.org/wiki/buildtools/) to install Spigot 1.8.8 locally. 

## License

[MIT](LICENSE) &copy; [Hugo Manrique](https://hugmanrique.me)
