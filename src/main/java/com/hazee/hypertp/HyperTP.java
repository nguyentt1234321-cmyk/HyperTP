package com.hazee.hypertp;

import com.hazee.hypertp.command.*;
import com.hazee.hypertp.config.ConfigLoader;
import com.hazee.hypertp.config.GUIConfigLoader;
import com.hazee.hypertp.listener.InventoryListener;
import com.hazee.hypertp.listener.PlayerListener;
import com.hazee.hypertp.manager.CooldownManager;
import com.hazee.hypertp.manager.HomeManager;
import com.hazee.hypertp.manager.LanguageManager;
import com.hazee.hypertp.manager.TPARequestManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class HyperTP extends JavaPlugin {
    private static HyperTP instance;
    private ConfigLoader configLoader;
    private GUIConfigLoader guiConfigLoader;
    private HomeManager homeManager;
    private CooldownManager cooldownManager;
    private TPARequestManager tpaRequestManager;
    private LanguageManager languageManager;
    private int autoSaveTaskId = -1;

    @Override
    public void onEnable() {
        instance = this;
        
        // Create data folder if not exists
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        // Initialize managers
        this.configLoader = new ConfigLoader(this);
        this.guiConfigLoader = new GUIConfigLoader(this);
        this.homeManager = new HomeManager(this);
        this.cooldownManager = new CooldownManager();
        this.tpaRequestManager = new TPARequestManager(this);
        this.languageManager = new LanguageManager(this);
        
        // Load configurations
        configLoader.loadConfig();
        guiConfigLoader.loadGUIConfigs();
        languageManager.loadLanguages();
        homeManager.loadHomes();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Start auto-save task if enabled
        startAutoSave();
        
        getLogger().info("=== HyperTP Enabled ===");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("Author: " + getDescription().getAuthors());
        getLogger().info("Default Language: " + languageManager.getDefaultLanguage());
        getLogger().info("Homes Loaded: " + homeManager.getTotalHomes());
        getLogger().info("=========================");
    }

    @Override
    public void onDisable() {
        // Stop auto-save task
        stopAutoSave();
        
        // Save homes
        homeManager.saveHomes();
        
        getLogger().info("HyperTP has been disabled! Data saved.");
    }

    private void registerCommands() {
        try {
            Objects.requireNonNull(getCommand("home")).setExecutor(new HomeCommands(this));
            Objects.requireNonNull(getCommand("sethome")).setExecutor(new HomeCommands(this));
            Objects.requireNonNull(getCommand("delhome")).setExecutor(new HomeCommands(this));
            Objects.requireNonNull(getCommand("homelist")).setExecutor(new HomeCommands(this));
            Objects.requireNonNull(getCommand("tpa")).setExecutor(new TeleportCommands(this));
            Objects.requireNonNull(getCommand("tpahere")).setExecutor(new TeleportCommands(this));
            Objects.requireNonNull(getCommand("tpaccept")).setExecutor(new TeleportCommands(this));
            Objects.requireNonNull(getCommand("tpdeny")).setExecutor(new TeleportCommands(this));
            Objects.requireNonNull(getCommand("back")).setExecutor(new TPCommand(this));
            Objects.requireNonNull(getCommand("rtp")).setExecutor(new TeleportCommands(this));
            Objects.requireNonNull(getCommand("hypertp")).setExecutor(new HyperTPCommand(this));
            
            getLogger().info("All commands registered successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to register commands: " + e.getMessage());
        }
    }

    private void registerListeners() {
        try {
            getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
            getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
            getLogger().info("All listeners registered successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to register listeners: " + e.getMessage());
        }
    }

    private void startAutoSave() {
        if (configLoader.isAutoSave()) {
            int interval = configLoader.getSaveInterval() * 20; // Convert seconds to ticks
            
            autoSaveTaskId = new BukkitRunnable() {
                @Override
                public void run() {
                    homeManager.saveHomes();
                    getLogger().info("Auto-save completed. Total homes: " + homeManager.getTotalHomes());
                }
            }.runTaskTimer(this, interval, interval).getTaskId();
            
            getLogger().info("Auto-save enabled with interval: " + configLoader.getSaveInterval() + " seconds");
        }
    }

    private void stopAutoSave() {
        if (autoSaveTaskId != -1) {
            getServer().getScheduler().cancelTask(autoSaveTaskId);
            autoSaveTaskId = -1;
            getLogger().info("Auto-save task stopped");
        }
    }

    public void reloadPlugin() {
        getLogger().info("Reloading HyperTP configuration...");
        
        try {
            // Reload configurations
            configLoader.reloadConfig();
            guiConfigLoader.reloadGUIConfigs();
            languageManager.reloadLanguages();
            homeManager.loadHomes();
            
            // Restart auto-save if settings changed
            stopAutoSave();
            startAutoSave();
            
            getLogger().info("HyperTP configuration reloaded successfully!");
        } catch (Exception e) {
            getLogger().severe("Error during reload: " + e.getMessage());
            throw new RuntimeException("Failed to reload HyperTP", e);
        }
    }

    // Static getter for instance
    public static HyperTP getInstance() {
        return instance;
    }

    // Manager getters
    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public GUIConfigLoader getGuiConfigLoader() {
        return guiConfigLoader;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public TPARequestManager getTpaRequestManager() {
        return tpaRequestManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }
}
