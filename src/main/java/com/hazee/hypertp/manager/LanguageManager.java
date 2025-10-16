package com.hazee.hypertp.manager;

import com.hazee.hypertp.HyperTP;
import com.hazee.hypertp.util.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final HyperTP plugin;
    private final Map<String, FileConfiguration> languages = new HashMap<>();
    private String defaultLanguage = "en";

    public LanguageManager(HyperTP plugin) {
        this.plugin = plugin;
    }

    /**
     * Load languages from the plugin data folder /langs/*.yml
     * Uses config.yml 'lang' value as default language.
     */
    public void loadLanguages() {
        File langsDir = new File(plugin.getDataFolder(), "langs");
        if (!langsDir.exists()) {
            plugin.saveResource("langs/en.yml", false);
            plugin.saveResource("langs/vn.yml", false);
        }

        // Load available language files
        File[] files = langsDir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return;

        languages.clear();
        for (File f : files) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            String name = f.getName().replace(".yml", "");
            languages.put(name, cfg);
        }

        // Set default language from config
        defaultLanguage = plugin.getConfig().getString("lang", defaultLanguage);
        if (!languages.containsKey(defaultLanguage)) {
            plugin.getLogger().warning("Configured language '" + defaultLanguage + "' not found, falling back to 'en'");
            defaultLanguage = "en";
        }

        plugin.getLogger().info("Default Language: " + defaultLanguage);
    }

    /**
     * Get a message by key for a player. Currently per-player language is not supported,
     * so it uses the configured default language.
     */
    public String getMessage(Player player, String key) {
        String lang = plugin.getConfig().getString("lang", defaultLanguage);
        FileConfiguration cfg = languages.getOrDefault(lang, languages.get(defaultLanguage));
        if (cfg == null) return ColorUtil.format(key);

        String val = cfg.getString(key);
        if (val == null) {
            // try root lookup with dots as path
            val = cfg.getString(key.replace(".", "."));
        }
        if (val == null) {
            // fallback: return the key itself to make missing keys visible
            return ColorUtil.format("&cMissing language key: " + key);
        }
        return ColorUtil.format(val);
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public boolean isLanguageSupported(String lang) {
        return languages.containsKey(lang);
    }

    public void reloadLanguages() {
        languages.clear();
        loadLanguages();
    }
}
