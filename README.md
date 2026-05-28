# 1.7.10-server-world-generator-ai

External-first architecture for Minecraft Forge 1.7.10 world generation:

- `com.mikedattolo.worldbuilder`: external `RealWorldMapBuilder` CLI that prepares files before game launch.
- `com.mikedattolo.worldgen`: Minecraft-side loaders and chunk generator manager that consume prepared files.

## Build and test

```bash
mvn test
```

## Quick Start (Easiest)

- Double-click `start-worldbuilder.bat` on Windows.
- Run `./start-worldbuilder.sh` on Linux/macOS.
- Or run the app with no args and it opens GUI mode:

```bash
java -cp target/classes com.mikedattolo.worldbuilder.RealWorldMapBuilderApp
```

- Need command help?

```bash
java -cp target/classes com.mikedattolo.worldbuilder.RealWorldMapBuilderApp --help
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

## Run GUI Menu

```bash
mvn -q -DskipTests package
java -cp target/classes com.mikedattolo.worldbuilder.gui.WorldBuilderGuiApp
```

The GUI provides a menu-driven desktop workflow for switching between `PROMPT` and `DEM` modes, editing generation inputs, previewing AI-inferred plans, and running exports without manually typing CLI flags.

For first-time users, use the **Setup Wizard** button (or **Run > Setup Wizard**) to walk through a guided 6-step configuration with recommended defaults.

## Easy Setup And Launch Scripts

Windows (`.bat`, auto-installs Java/Maven with winget if missing):

```bat
start-worldbuilder.bat
```

Tip: if you run from an existing terminal and do not want the final pause, use:

```bat
start-worldbuilder.bat --no-wait
```

Linux/macOS (`.sh`, validates Java/Maven and launches GUI when a desktop display is available):

```bash
./start-worldbuilder.sh
```

Tip: skip final pause when running from your own terminal:

```bash
./start-worldbuilder.sh --no-wait
```

## AI Prompt Feature

Prompt mode now performs richer intent extraction from natural-language prompts, including:

- Theme inference (`abandoned`, `futuristic`, `historic`, `realistic`)
- Terrain and vegetation inference
- Structure extraction (`hospital`, `city`, `military`, `farmland`, etc.)
- Special feature extraction (`bridge crossings`, `rail line`, `river corridor`, fog behavior)

This logic remains deterministic and local so world generation is reproducible.
