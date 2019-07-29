package me.hugmanrique.slime.bukkit;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.StubMethod;
import net.minecraft.server.v1_8_R3.ServerNBTManager;
import org.bukkit.plugin.java.JavaPlugin;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class SlimePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        injectDataManagerMethods();
    }

    private void injectDataManagerMethods() {
        getLogger().info("Adding ServerNBTManager intercepts");

        ByteBuddyAgent.install();

        ClassLoader systemClassLoader = ServerNBTManager.class.getClassLoader();

        new ByteBuddy()
                .redefine(ServerNBTManager.class)
                .method(named("createChunkLoader"))
                    .intercept(Advice.to(DataManagerAdvice.class)
                        .wrap(StubMethod.INSTANCE))
                .method(named("saveWorldData").and(takesArguments(2)))
                    .intercept(StubMethod.INSTANCE)
                .make()
                .load(systemClassLoader, ClassReloadingStrategy.fromInstalledAgent());
    }
}
