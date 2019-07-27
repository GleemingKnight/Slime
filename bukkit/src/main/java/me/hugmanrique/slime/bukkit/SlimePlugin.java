package me.hugmanrique.slime.bukkit;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.minecraft.server.v1_8_R3.ServerNBTManager;
import org.bukkit.plugin.java.JavaPlugin;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class SlimePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        injectDataManagerMethods();
    }

    private void injectDataManagerMethods() {
        new ByteBuddy()
            .redefine(ServerNBTManager.class)
            .method(named("createChunkLoader"))
                .intercept(MethodDelegation.to(DataManagerIncercepts.class))
            .method(named("saveWorldData"))
                .intercept(MethodDelegation.to(DataManagerIncercepts.class))
            .make()
            .load(ServerNBTManager.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    }
}
