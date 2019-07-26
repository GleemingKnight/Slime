package me.hugmanrique.slime.core.tests;

import me.hugmanrique.slime.core.data.ProtoSlimeChunk;
import me.hugmanrique.slime.core.data.SlimeFile;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Parameter;

public class SlimeExtension implements ParameterResolver {

    private static final File SLIME_TEST_FILE = new File("src/test/resources/skyblock.slime");

    static {
        // Recalculating counts causes an "Accessed blocks before bootstrap" exception
        ProtoSlimeChunk.RECALC_BLOCK_COUNTS = false;
    }

    private final SlimeFile file;

    public SlimeExtension() throws IOException {
        file = SlimeFile.read(SLIME_TEST_FILE);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Parameter parameter = parameterContext.getParameter();

        return parameter.getType().equals(SlimeFile.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return file;
    }
}
