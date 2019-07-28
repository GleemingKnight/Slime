package me.hugmanrique.slime.bukkit;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.StubMethod;
import net.minecraft.server.v1_8_R3.ServerNBTManager;
import org.bukkit.plugin.java.JavaPlugin;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class SlimePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        injectDataManagerMethods();
    }

    private void injectDataManagerMethods() {
        // TODO Attempt to redefine without loading the agent;
        // I believe CraftServer doesn't instantiate ServerNBTManager
        // after STARTUP plugins have loaded.

        ByteBuddyAgent.install();

        getLogger().info("Injecting ServerNBTManager...");

        new ByteBuddy()
                .redefine(ServerNBTManager.class)
                .method(named("createChunkLoader"))
                .intercept(Advice.to(DataManagerAdvice.class)
                        .wrap(StubMethod.INSTANCE))
                .make()
                .load(ServerNBTManager.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

        // Create system classloaded class
        /*Class<?> clazz = new ByteBuddy()
                .subclass(Object.class)
                .defineMethod("createChunkLoader", IChunkLoader.class, Visibility.PUBLIC, Ownership.STATIC)
                .withParameters(WorldProvider.class)
                .intercept(MethodDelegation.to(DataManagerIncercepts.class))
                .make()
                .load(ServerNBTManager.class.getClassLoader())
                .getLoaded();

        ByteBuddyAgent.install();

        new ByteBuddy()
            .redefine(ServerNBTManager.class)
            .method(named("createChunkLoader"))
                .intercept(MethodDelegation.to(clazz))
            .method(named("saveWorldData").and(takesArguments(2)))
                .intercept(StubMethod.INSTANCE)
            /*.method(named("saveWorldData").and(takesArguments(1)))
                .intercept(MethodDelegation.to(DataManagerIncercepts.class))*/
            /*.make()
            .load(ServerNBTManager.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());*/
    }
}
