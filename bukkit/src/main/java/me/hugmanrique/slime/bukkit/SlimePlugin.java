package me.hugmanrique.slime.bukkit;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector.UsingInstrumentation.Target;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.StubMethod;
import net.minecraft.server.v1_8_R3.ServerNBTManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.function.UnaryOperator;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class SlimePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!injectGenerator()) {
            setEnabled(false);
            return;
        }

        addDataManagerIntercepts();
    }

    private <T> void redefine(Class<T> type, UnaryOperator<DynamicType.Builder<T>> operation) {
        DynamicType.Builder<T> builder = new ByteBuddy().redefine(type);
        ClassLoader systemClassLoader = ServerNBTManager.class.getClassLoader();

        operation
            .apply(builder)
            .make()
            .load(systemClassLoader, ClassReloadingStrategy.fromInstalledAgent());
    }

    private boolean injectGenerator() {
        try {
            PluginClassInjector injector = new PluginClassInjector();

            injector.addClass(EmptyChunkGenerator.class);

            injector.inject(Target.SYSTEM);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void addDataManagerIntercepts() {
        getLogger().info("Adding ServerNBTManager intercepts");

        ByteBuddyAgent.install();

        redefine(ServerNBTManager.class, builder -> builder
                .method(named("createChunkLoader"))
                    .intercept(Advice.to(DataManagerAdvice.class).wrap(StubMethod.INSTANCE))
                .method(named("saveWorldData").and(takesArguments(2)))
                    .intercept(StubMethod.INSTANCE));

        /*ClassLoader systemClassLoader = ServerNBTManager.class.getClassLoader();

        new ByteBuddy()
                .redefine(ServerNBTManager.class)
                .method(named("createChunkLoader"))
                    .intercept(Advice.to(DataManagerAdvice.class)
                        .wrap(StubMethod.INSTANCE))
                .method(named("saveWorldData").and(takesArguments(2)))
                    .intercept(StubMethod.INSTANCE)
                .make()
                .load(systemClassLoader, ClassReloadingStrategy.fromInstalledAgent());*/
    }
}
