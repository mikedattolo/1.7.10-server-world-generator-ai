package com.mikedattolo.worldbuilder.gui;

import com.mikedattolo.worldbuilder.WorldBuilderService;
import com.mikedattolo.worldbuilder.model.CLIOptions;
import com.mikedattolo.worldbuilder.model.GenerationPlan;
import com.mikedattolo.worldbuilder.model.GenerationMode;
import com.mikedattolo.worldbuilder.model.ProjectMetadata;
import com.mikedattolo.worldbuilder.minecraft.MinecraftSaveInstaller;
import com.mikedattolo.worldbuilder.prompt.PromptAssistant;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

public class WorldBuilderGuiApp {
    private final JFrame frame;
    private final JComboBox<String> mode;
    private final JComboBox<String> preset;
    private final JTextField projectName;
    private final JTextArea prompt;
    private final JTextField bbox;
    private final JTextField address;
    private final JTextField radius;
    private final JTextField worldSize;
    private final JTextField verticalScale;
    private final JTextField style;
    private final JTextField output;
    private final JTextArea preview;
    private final JTextArea status;
    private final JButton mapSelectButton;
    private final JButton improvePromptButton;
    private final JButton setupWizardButton;
    private final JButton applyPresetButton;
    private final JButton previewButton;
    private final JButton openOutputButton;
    private final JButton installWorldButton;
    private final JButton generateButton;
    private Path lastGeneratedWorld;

    public WorldBuilderGuiApp() {
        frame = new JFrame("RealWorldMapBuilder GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));
        frame.setJMenuBar(buildMenu());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        mode = new JComboBox<String>(new String[]{"PROMPT", "DEM"});
        preset = new JComboBox<String>(new String[]{
            "Custom",
            "Prompt: Abandoned Town",
            "Prompt: Forest Survival",
            "DEM: Suburban Sample"
        });
        projectName = new JTextField("RealWorldProject", 24);
        prompt = new JTextArea("Generate an abandoned suburban town with dense woods and a hospital", 3, 24);
        bbox = new JTextField("40.123,-74.123,40.130,-74.115", 24);
        address = new JTextField("", 24);
        radius = new JTextField("1000", 24);
        worldSize = new JTextField("2048", 24);
        verticalScale = new JTextField("1.0", 24);
        style = new JTextField("realistic", 24);
        output = new JTextField("config/worldgen/realworld", 24);
        mapSelectButton = new JButton("Select Area on Map");
        mapSelectButton.addActionListener(e -> selectAreaOnMap());
        improvePromptButton = new JButton("Improve Prompt");
        improvePromptButton.addActionListener(e -> improvePrompt());
        preview = new JTextArea(10, 60);
        preview.setEditable(false);
        preview.setBorder(BorderFactory.createTitledBorder("Plan Preview"));
        status = new JTextArea(10, 60);
        status.setEditable(false);
        status.setBorder(BorderFactory.createTitledBorder("Generation Log"));

        int row = 0;
        addField(form, gbc, row++, "Quick Preset", preset);
        addField(form, gbc, row++, "Mode", mode);
        addField(form, gbc, row++, "Project Name", projectName);
        addField(form, gbc, row++, "Prompt", withButton(new JScrollPane(prompt), improvePromptButton));
        addField(form, gbc, row++, "BBox (DEM)", withButton(bbox, mapSelectButton));
        addField(form, gbc, row++, "Address (DEM)", address);
        addField(form, gbc, row++, "Radius (meters)", radius);
        addField(form, gbc, row++, "World Size", worldSize);
        addField(form, gbc, row++, "Vertical Scale", verticalScale);
        addField(form, gbc, row++, "Style", style);
        addField(form, gbc, row++, "Output", output);

        setupWizardButton = new JButton("Setup Wizard");
        setupWizardButton.addActionListener(e -> runSetupWizard());
        applyPresetButton = new JButton("Apply Preset");
        applyPresetButton.addActionListener(e -> applySelectedPreset());
        previewButton = new JButton("Preview Plan");
        previewButton.addActionListener(e -> previewPlan());
        openOutputButton = new JButton("Open Output Folder");
        openOutputButton.addActionListener(e -> openOutputFolder());
        installWorldButton = new JButton("Install to Minecraft Saves");
        installWorldButton.addActionListener(e -> installGeneratedWorld());
        generateButton = new JButton("Generate Project");
        generateButton.addActionListener(e -> generate());

        JPanel actions = new JPanel(new BorderLayout(8, 0));
        actions.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JPanel leftActions = new JPanel(new BorderLayout(8, 0));
        leftActions.add(generateButton, BorderLayout.WEST);
        leftActions.add(previewButton, BorderLayout.CENTER);
        actions.add(leftActions, BorderLayout.WEST);
        JPanel rightActions = new JPanel(new BorderLayout(8, 0));
        rightActions.add(setupWizardButton, BorderLayout.WEST);
        JPanel utilityActions = new JPanel(new BorderLayout(8, 0));
        utilityActions.add(applyPresetButton, BorderLayout.WEST);
        utilityActions.add(openOutputButton, BorderLayout.CENTER);
        utilityActions.add(installWorldButton, BorderLayout.EAST);
        rightActions.add(utilityActions, BorderLayout.CENTER);
        actions.add(rightActions, BorderLayout.EAST);

        JSplitPane outputPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(preview),
            new JScrollPane(status));
        outputPane.setResizeWeight(0.45);
        outputPane.setDividerLocation(240);

        frame.add(form, BorderLayout.NORTH);
        frame.add(actions, BorderLayout.CENTER);
        frame.add(outputPane, BorderLayout.SOUTH);

        mode.addActionListener(e -> refreshModeFields());
        refreshModeFields();
        frame.pack();
        frame.setMinimumSize(new Dimension(840, 720));
        frame.setLocationRelativeTo(null);
    }

    private JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenu run = new JMenu("Run");
        JMenu presets = new JMenu("Presets");
        JMenu help = new JMenu("Help");

        JMenuItem setupWizard = new JMenuItem("Setup Wizard");
        setupWizard.addActionListener(e -> runSetupWizard());
        JMenuItem previewPlan = new JMenuItem("Preview Plan");
        previewPlan.addActionListener(e -> previewPlan());
        JMenuItem selectMapArea = new JMenuItem("Select DEM Area on Map");
        selectMapArea.addActionListener(e -> selectAreaOnMap());
        JMenuItem improvePrompt = new JMenuItem("Improve Prompt");
        improvePrompt.addActionListener(e -> improvePrompt());
        JMenuItem generate = new JMenuItem("Generate");
        generate.addActionListener(e -> generate());
        JMenuItem openOutput = new JMenuItem("Open Output Folder");
        openOutput.addActionListener(e -> openOutputFolder());
        JMenuItem installWorld = new JMenuItem("Install to Minecraft Saves");
        installWorld.addActionListener(e -> installGeneratedWorld());
        JMenuItem applyPreset = new JMenuItem("Apply Selected Preset");
        applyPreset.addActionListener(e -> applySelectedPreset());
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> frame.dispose());
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "RealWorldMapBuilder GUI\nForge 1.7.10 world preprocessor",
                "About", JOptionPane.INFORMATION_MESSAGE));

        run.add(setupWizard);
        run.add(selectMapArea);
        run.add(improvePrompt);
        run.add(previewPlan);
        run.add(generate);
        run.add(openOutput);
        run.add(installWorld);
        presets.add(applyPreset);
        file.add(exit);
        help.add(about);
        bar.add(file);
        bar.add(run);
        bar.add(presets);
        bar.add(help);
        return bar;
    }

    private void selectAreaOnMap() {
        String selectedBbox = BrowserMapSelector.choose(frame, bbox.getText().trim());
        if (selectedBbox == null || selectedBbox.trim().isEmpty()) {
            return;
        }
        mode.setSelectedItem("DEM");
        bbox.setText(selectedBbox.trim());
        address.setText("");
        refreshModeFields();
        appendStatus("Selected DEM area on map: " + selectedBbox);
        previewPlan();
    }

    private void improvePrompt() {
        mode.setSelectedItem("PROMPT");
        String improved = new PromptAssistant().improvePrompt(prompt.getText(), style.getText().trim());
        prompt.setText(improved);
        refreshModeFields();
        appendStatus("Prompt improved for style=" + style.getText().trim());
        previewPlan();
    }

    private void runSetupWizard() {
        String[] modes = new String[]{"PROMPT", "DEM"};
        String selectedMode = (String) JOptionPane.showInputDialog(
                frame,
                "Step 1 of 6: Choose generation mode",
                "Setup Wizard",
                JOptionPane.QUESTION_MESSAGE,
                null,
                modes,
                String.valueOf(mode.getSelectedItem()));
        if (selectedMode == null) {
            return;
        }
        mode.setSelectedItem(selectedMode);

        String chosenProject = promptText(
                "Step 2 of 6: Project name",
                projectName.getText().trim().isEmpty() ? "RealWorldProject" : projectName.getText().trim());
        if (chosenProject == null) {
            return;
        }
        projectName.setText(chosenProject.trim());

        String[] styles = new String[]{"realistic", "apocalypse", "futuristic", "historic"};
        String selectedStyle = (String) JOptionPane.showInputDialog(
                frame,
                "Step 3 of 6: Style",
                "Setup Wizard",
                JOptionPane.QUESTION_MESSAGE,
                null,
                styles,
                style.getText().trim().isEmpty() ? "realistic" : style.getText().trim());
        if (selectedStyle == null) {
            return;
        }
        style.setText(selectedStyle);

        String chosenSize = promptText("Step 4 of 6: World size (recommended 1024-4096)", worldSize.getText().trim());
        if (chosenSize == null) {
            return;
        }
        if (!isInteger(chosenSize) || Integer.parseInt(chosenSize) <= 0) {
            JOptionPane.showMessageDialog(frame, "World size must be a positive whole number.", "Setup Wizard", JOptionPane.ERROR_MESSAGE);
            return;
        }
        worldSize.setText(chosenSize.trim());

        String chosenOutput = promptText("Step 5 of 6: Output folder", output.getText().trim());
        if (chosenOutput == null) {
            return;
        }
        output.setText(chosenOutput.trim());

        if ("PROMPT".equals(selectedMode)) {
            String chosenPrompt = promptText(
                    "Step 6 of 6: Describe your world",
                    prompt.getText().trim().isEmpty()
                            ? "Generate an abandoned suburban town with dense woods and a hospital"
                            : prompt.getText().trim());
            if (chosenPrompt == null) {
                return;
            }
            prompt.setText(chosenPrompt.trim());
        } else {
            JOptionPane.showMessageDialog(frame,
                    "Step 6 of 6: Drag a rectangle on the map, then choose Use Selected Area.",
                    "Setup Wizard",
                    JOptionPane.INFORMATION_MESSAGE);
            String chosenBbox = BrowserMapSelector.choose(frame, bbox.getText().trim());
            if (chosenBbox == null || chosenBbox.trim().isEmpty()) {
                return;
            }
            bbox.setText(chosenBbox.trim());
        }

        refreshModeFields();
        appendStatus("Setup Wizard completed for mode=" + selectedMode + ".");
        previewPlan();
    }

    private String promptText(String title, String initialValue) {
        return (String) JOptionPane.showInputDialog(
                frame,
                title,
                "Setup Wizard",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                initialValue);
    }

    private static boolean isInteger(String value) {
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private void applySelectedPreset() {
        String selected = String.valueOf(preset.getSelectedItem());
        if ("Prompt: Abandoned Town".equals(selected)) {
            mode.setSelectedItem("PROMPT");
            projectName.setText("AbandonedTownProject");
            prompt.setText("Generate an abandoned suburban town with a hospital, rail line and dense woods");
            style.setText("apocalypse");
            output.setText("config/worldgen/prompt/abandoned-town");
        } else if ("Prompt: Forest Survival".equals(selected)) {
            mode.setSelectedItem("PROMPT");
            projectName.setText("ForestSurvivalProject");
            prompt.setText("Generate a rugged survival map with forests, rivers, bridges and sparse settlements");
            style.setText("realistic");
            output.setText("config/worldgen/prompt/forest-survival");
        } else if ("DEM: Suburban Sample".equals(selected)) {
            mode.setSelectedItem("DEM");
            projectName.setText("DemSuburbanProject");
            bbox.setText("40.123,-74.123,40.130,-74.115");
            address.setText("");
            radius.setText("1000");
            worldSize.setText("2048");
            verticalScale.setText("1.2");
            style.setText("realistic");
            output.setText("config/worldgen/realworld/suburban-sample");
        }
        refreshModeFields();
        appendStatus("Applied preset: " + selected);
        if (!"Custom".equals(selected)) {
            previewPlan();
        }
    }

    private void openOutputFolder() {
        String outPath = output.getText().trim();
        if (outPath.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Output path is empty.", "Open Output", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File dir = new File(outPath);
        if (!dir.exists()) {
            JOptionPane.showMessageDialog(frame, "Output folder does not exist yet. Generate first.", "Open Output", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(frame, "Desktop folder opening is not supported on this system.", "Open Output", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Desktop.getDesktop().open(dir);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Could not open output folder: " + ex.getMessage(),
                    "Open Output",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void installGeneratedWorld() {
        Path worldDir = lastGeneratedWorld;
        if (worldDir == null) {
            String outPath = output.getText().trim();
            if (!outPath.isEmpty()) {
                worldDir = new File(outPath, "minecraft_world").toPath();
            }
        }
        if (worldDir == null || !worldDir.toFile().exists()) {
            JOptionPane.showMessageDialog(frame, "Generate a world before installing it.", "Install World", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Path installed = new MinecraftSaveInstaller().install(worldDir, projectName.getText());
            appendStatus("Installed Minecraft save: " + installed);
            JOptionPane.showMessageDialog(frame, "Installed Minecraft save:\n" + installed, "Install World", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Could not install world: " + ex.getMessage(),
                    "Install World",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshModeFields() {
        boolean promptMode = "PROMPT".equals(String.valueOf(mode.getSelectedItem()));
        prompt.setEnabled(promptMode);
        bbox.setEnabled(!promptMode);
        address.setEnabled(!promptMode);
        radius.setEnabled(!promptMode);
    }

    private void generate() {
        final CLIOptions options;
        try {
            options = readOptions();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        generateButton.setEnabled(false);
        appendStatus("Starting generation for mode=" + options.mode + " style=" + options.style);

        new SwingWorker<WorldBuilderService.GenerationResult, Void>() {
            @Override
            protected WorldBuilderService.GenerationResult doInBackground() throws Exception {
                return new WorldBuilderService().generate(options);
            }

            @Override
            protected void done() {
                generateButton.setEnabled(true);
                try {
                    WorldBuilderService.GenerationResult result = get();
                    appendStatus("Project generated: " + result.metadata.projectName);
                    appendStatus("Output: " + result.outputPath);
                    lastGeneratedWorld = result.outputPath.resolve("minecraft_world");
                    appendStatus("Minecraft world: " + lastGeneratedWorld);
                    appendStatus("World size: " + result.metadata.worldSize + "x" + result.metadata.worldSize);
                    appendStatus("Theme: " + result.plan.theme + " Terrain: " + result.plan.terrain);
                    appendStatus("Structures: " + result.plan.structures.toString());
                    appendStatus("---");
                } catch (Exception ex) {
                    appendStatus("Generation failed: " + ex.getMessage());
                    JOptionPane.showMessageDialog(frame,
                            "Generation failed: " + ex.getMessage(),
                            "Generation Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void previewPlan() {
        final CLIOptions options;
        try {
            options = readOptions();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            GenerationPlan plan = WorldBuilderService.buildPlan(options);
            ProjectMetadata metadata = WorldBuilderService.buildMetadata(options);
            StringBuilder sb = new StringBuilder();
            sb.append("Mode: ").append(options.mode).append("\n");
            sb.append("Theme: ").append(plan.theme).append("\n");
            sb.append("Terrain: ").append(plan.terrain).append("\n");
            sb.append("Roads: ").append(plan.roads).append("\n");
            sb.append("Vegetation: ").append(plan.vegetation).append("\n");
            sb.append("Style: ").append(plan.style).append("\n");
            sb.append("Estimated Features: ").append(metadata.estimatedFeatures.toString()).append("\n");
            sb.append("Structures: ").append(plan.structures.toString()).append("\n");
            sb.append("Special Features: ").append(plan.specialFeatures.toString()).append("\n");
            preview.setText(sb.toString());
            appendStatus("Preview refreshed for mode=" + options.mode + " style=" + options.style);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(frame,
                    "Preview failed: " + ex.getMessage(),
                    "Preview Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private CLIOptions readOptions() {
        CLIOptions options = new CLIOptions();
        options.mode = GenerationMode.valueOf(String.valueOf(mode.getSelectedItem()).toUpperCase(Locale.ROOT));
        options.projectName = projectName.getText().trim();
        options.prompt = prompt.getText().trim();
        options.bbox = emptyToNull(bbox.getText());
        options.address = emptyToNull(address.getText());
        options.radius = Integer.valueOf(radius.getText().trim());
        options.worldSize = Integer.parseInt(worldSize.getText().trim());
        options.verticalScale = Double.parseDouble(verticalScale.getText().trim());
        options.style = style.getText().trim();
        options.output = output.getText().trim();

        if (options.mode == GenerationMode.PROMPT && (options.prompt == null || options.prompt.isEmpty())) {
            throw new IllegalArgumentException("Prompt mode requires a prompt.");
        }
        if (options.mode == GenerationMode.DEM && options.bbox == null && options.address == null) {
            throw new IllegalArgumentException("DEM mode requires either bbox or address.");
        }
        if (options.worldSize <= 0) {
            throw new IllegalArgumentException("World size must be positive.");
        }
        if (options.output == null || options.output.isEmpty()) {
            throw new IllegalArgumentException("Output path is required.");
        }
        return options;
    }

    private static String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void appendStatus(String line) {
        status.append(line + "\n");
    }

    private static void addField(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component input) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(input, gbc);
    }

    private static JPanel withButton(java.awt.Component input, JButton button) {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.add(input, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new WorldBuilderGuiApp().show();
            }
        });
    }
}