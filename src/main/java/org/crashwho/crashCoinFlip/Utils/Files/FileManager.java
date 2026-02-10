package org.crashwho.crashCoinFlip.Utils.Files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.crashwho.crashCoinFlip.CrashCoinFlip;

import java.io.File;

public class FileManager {

    protected FileConfiguration data;
    protected final File file;

    public FileManager(CrashCoinFlip plugin, String name) {
        file = new File(plugin.getDataFolder(), name + ".yml");

        if (!file.exists()) {
            plugin.saveResource(name + ".yml", false);
        }

        data = YamlConfiguration.loadConfiguration(file);
    }

    public void reloadFile() {
        data = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getData() {
        return data;
    }

}
