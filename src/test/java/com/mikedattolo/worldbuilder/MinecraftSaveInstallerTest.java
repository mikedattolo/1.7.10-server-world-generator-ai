package com.mikedattolo.worldbuilder;

import com.mikedattolo.worldbuilder.minecraft.MinecraftSaveInstaller;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftSaveInstallerTest {
    @Test
    void installsGeneratedWorldIntoMinecraftSavesDirectory() throws Exception {
        Path fakeHome = Files.createTempDirectory("minecraft-home");
        String oldHome = System.getProperty("user.home");
        System.setProperty("user.home", fakeHome.toString());
        try {
            Path generated = Files.createTempDirectory("generated-world");
            Files.write(generated.resolve("level.dat"), new byte[]{1, 2, 3});
            Files.createDirectories(generated.resolve("region"));
            Files.write(generated.resolve("region/r.0.0.mca"), new byte[]{4, 5, 6});

            Path installed = new MinecraftSaveInstaller().install(generated, "Bad:/World*Name");

            assertTrue(Files.exists(installed.resolve("level.dat")));
            assertTrue(Files.exists(installed.resolve("region/r.0.0.mca")));
            assertTrue(installed.getFileName().toString().contains("Bad__World_Name"));
        } finally {
            System.setProperty("user.home", oldHome);
        }
    }
}
