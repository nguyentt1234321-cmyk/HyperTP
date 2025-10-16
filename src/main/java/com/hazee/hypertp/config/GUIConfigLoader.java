package com.hazee.hypertp.config;

import com.hazee.hypertp.HyperTP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GUIConfigLoader {
    private final HyperTP plugin;
    private final Map<String, FileConfiguration> guiConfigs = new HashMap<>();

    public GUIConfigLoader(HyperTP plugin) {
        this.plugin = plugin;
    }

    public void loadGUIConfigs() {
        loadGUIConfig("homegui");
        loadGUIConfig("tpagui");
        loadGUIConfig("rtpgui");
    }

    private void loadGUIConfig(String guiName) {
        File guiFile = new File(plugin.getDataFolder() + "/gui", guiName + ".yml");
        if (!guiFile.exists()) {
            plugin.saveResource("gui/" + guiName + ".yml", false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
        guiConfigs.put(guiName, config);
    }

    public FileConfiguration getGUIConfig(String guiName) {
        return guiConfigs.get(guiName);
    }

    public void reloadGUIConfigs() {
        guiConfigs.clear();
        loadGUIConfigs();
    }
}