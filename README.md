# 1.7.10-server-world-generator-ai

External-first architecture for Minecraft Forge 1.7.10 world generation:

- `com.mikedattolo.worldbuilder`: external `RealWorldMapBuilder` CLI that prepares files before game launch.
- `com.mikedattolo.worldgen`: Minecraft-side loaders and chunk generator manager that consume prepared files.

## Build and test

```bash
mvn test
```

## Run CLI (DEM mode)

```bash
mvn -q -DskipTests package
java -cp target/classes com.mikedattolo.worldbuilder.RealWorldMapBuilderApp --bbox "40.123,-74.123,40.130,-74.115" --size 2048 --verticalScale 1.5 --style apocalypse --output config/worldgen/realworld/
```

## Run CLI (Prompt mode)

```bash
java -cp target/classes com.mikedattolo.worldbuilder.RealWorldMapBuilderApp --prompt "Generate an abandoned suburban town with dense woods and a hospital" --style apocalypse --output config/worldgen/prompt/
```
