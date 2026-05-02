package cc.cassian.clickthrough.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int version = 0;
    public boolean isActive = true;
    public boolean onlycontainers = true;
    public boolean sneaktodye = false;
    public boolean displayActiveTextAsTitle = true;
    public List<String> containers = Arrays.asList(
        "minecraft:ender_chest",
        "minecraft:composter",
        "minecraft:jukebox",
        "minecraft:beacon",
        "minecraft:grindstone",
        "minecraft:crafting_table"
    );

    private static File configFile;

    public static ModConfig load(Path configDir) {
        configFile = configDir.resolve("clickthrough.json").toFile();
        if (configFile.exists()) {
            try (Reader reader = new FileReader(configFile)) {
                return GSON.fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                System.err.println("[ClickThrough] Failed to load config, using defaults: " + e.getMessage());
            }
        }
        ModConfig config = new ModConfig();
        config.save();
        return config;
    }

    public void save() {
        if (configFile == null) return;
        try (Writer writer = new FileWriter(configFile)) {
            GSON.toJson(this, writer);
        } catch (Exception e) {
            System.err.println("[ClickThrough] Failed to save config: " + e.getMessage());
        }
    }
}
