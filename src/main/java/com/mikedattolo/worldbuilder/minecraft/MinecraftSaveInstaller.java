package com.mikedattolo.worldbuilder.minecraft;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class MinecraftSaveInstaller {
    public Path defaultSavesDir() {
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.trim().isEmpty()) {
                return java.nio.file.Paths.get(appData, ".minecraft", "saves");
            }
        }
        if (os.contains("mac")) {
            return java.nio.file.Paths.get(home, "Library", "Application Support", "minecraft", "saves");
        }
        return java.nio.file.Paths.get(home, ".minecraft", "saves");
    }

    public Path install(Path generatedWorldDir, String worldName) throws IOException {
        if (!Files.exists(generatedWorldDir.resolve("level.dat"))) {
            throw new IOException("Generated world is missing level.dat: " + generatedWorldDir);
        }
        String safeName = sanitize(worldName == null || worldName.trim().isEmpty() ? "WorldBuilderWorld" : worldName.trim());
        Path savesDir = defaultSavesDir();
        Files.createDirectories(savesDir);
        Path destination = uniqueDestination(savesDir, safeName);
        copyDirectory(generatedWorldDir, destination);
        return destination;
    }

    private Path uniqueDestination(Path savesDir, String baseName) {
        Path destination = savesDir.resolve(baseName);
        int suffix = 2;
        while (Files.exists(destination)) {
            destination = savesDir.resolve(baseName + "-" + suffix);
            suffix++;
        }
        return destination;
    }

    private void copyDirectory(Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(destination.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, destination.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static String sanitize(String value) {
        String sanitized = value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return sanitized.isEmpty() ? "WorldBuilderWorld" : sanitized;
    }
}
