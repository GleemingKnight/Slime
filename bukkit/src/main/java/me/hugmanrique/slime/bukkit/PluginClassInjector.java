package me.hugmanrique.slime.bukkit;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

/**
 * Injects plugin classes into a {@link ClassLoader}.
 *
 * @see ClassInjector
 */
public class PluginClassInjector {

    private final Set<Class<?>> classes;

    public PluginClassInjector() {
        this.classes = new HashSet<>();
    }

    public boolean addClass(Class<?> clazz) {
        return classes.add(clazz);
    }

    private Map<? extends TypeDescription, byte[]> getTypes() {
        return classes.stream().collect(
                toMap(TypeDescription.ForLoadedType::new, ClassFileLocator.ForClassLoader::read));
    }

    public void inject(ClassInjector.UsingInstrumentation.Target target) throws IOException {
        inject(target, ByteBuddyAgent.install());
    }

    public void inject(ClassInjector.UsingInstrumentation.Target target, Instrumentation instrumentation) throws IOException {
        File tempDir = Files.createTempDirectory("slimeInjector" + hashCode()).toFile();
        tempDir.deleteOnExit();

        inject(tempDir, target, instrumentation);
    }

    public void inject(File folder, ClassInjector.UsingInstrumentation.Target target, Instrumentation instrumentation) {
        ClassInjector injector = ClassInjector.UsingInstrumentation.of(folder, target, instrumentation);

        injector.inject(getTypes());
    }
}
