package com.hazee.hypertp.config;

import com.hazee.hypertp.HyperTP;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {
    private final HyperTP plugin;
    private FileConfiguration config;
    
    private int teleportCooldown;
    private int backCooldown;
    private int guiCooldown;
    private int rtpCooldown;
    private int maxHomes;
    private boolean usePermission;
    private String defaultLanguage;
    private boolean autoSave;
    private int saveInterval;
    private int rtpMinDistance;
    private int rtpMaxDistance;
    private boolean rtpSafeTeleport;
    private String rtpWorld;
    private int rtpMaxAttempts;
    private boolean rtpRequireSafeLocation;
    private Map<String, RTPRange> rtpRanges = new HashMap<>();
    private int tpaRequestTimeout;
    private boolean tpaRequireAccept;
    private boolean homesEnabled;
    private boolean tpaEnabled;
    private boolean rtpEnabled;
    private boolean backEnabled;
    private boolean guiEnabled;

    public ConfigLoader(HyperTP plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        teleportCooldown = config.getInt("cooldowns.teleport", 5);
        backCooldown = config.getInt("cooldowns.back", 10);
        guiCooldown = config.getInt("cooldowns.gui", 3);
        rtpCooldown = config.getInt("cooldowns.rtp", 30);
        maxHomes = config.getInt("settings.max-homes", 5);
        usePermission = config.getBoolean("settings.use-permissions", true);
        defaultLanguage = config.getString("settings.default-language", "en");
        autoSave = config.getBoolean("settings.auto-save", true);
        saveInterval = config.getInt("settings.save-interval", 300);
        loadRTPRanges();
        rtpMinDistance = config.getInt("rtp.min-distance", 100);
        rtpMaxDistance = config.getInt("rtp.max-distance", 5000);
        rtpSafeTeleport = config.getBoolean("rtp.safe-teleport", true);
        rtpWorld = config.getString("rtp.world", "world");
        rtpMaxAttempts = config.getInt("rtp.max-attempts", 10);
        rtpRequireSafeLocation = config.getBoolean("rtp.require-safe-location", true);
        tpaRequestTimeout = config.getInt("tpa.request-timeout", 30);
        tpaRequireAccept = config.getBoolean("tpa.require-accept", true);
        homesEnabled = config.getBoolean("features.homes", true);
        tpaEnabled = config.getBoolean("features.tpa", true);
        rtpEnabled = config.getBoolean("features.rtp", true);
        backEnabled = config.getBoolean("features.back", true);
        guiEnabled = config.getBoolean("features.gui", true);
    }

    private void loadRTPRanges() {
        rtpRanges.clear();
        
        if (config.contains("rtp.ranges")) {
            for (String worldName : config.getConfigurationSection("rtp.ranges").getKeys(false)) {
                String path = "rtp.ranges." + worldName;
                int min = config.getInt(path + ".min-distance", 100);
                int max = config.getInt(path + ".max-distance", 5000);
                int centerX = config.getInt(path + ".center-x", 0);
                int centerZ = config.getInt(path + ".center-z", 0);
                boolean enabled = config.getBoolean(path + ".enabled", true);
                
                rtpRanges.put(worldName, new RTPRange(min, max, centerX, centerZ, enabled));
            }
        } else {
            rtpRanges.put("world", new RTPRange(100, 5000, 0, 0, true));
            rtpRanges.put("world_nether", new RTPRange(50, 1000, 0, 0, true));
            rtpRanges.put("world_the_end", new RTPRange(200, 3000, 0, 0, true));
        }
    }

    public int getTeleportCooldown() { return teleportCooldown; }
    public int getBackCooldown() { return backCooldown; }
    public int getGuiCooldown() { return guiCooldown; }
    public int getRtpCooldown() { return rtpCooldown; }
    public int getMaxHomes() { return maxHomes; }
    public boolean isUsePermission() { return usePermission; }
    public String getDefaultLanguage() { return defaultLanguage; }
    public boolean isAutoSave() { return autoSave; }
    public int getSaveInterval() { return saveInterval; }
    public int getRtpMinDistance() { return rtpMinDistance; }
    public int getRtpMaxDistance() { return rtpMaxDistance; }
    public boolean isRtpSafeTeleport() { return rtpSafeTeleport; }
    public String getRtpWorld() { return rtpWorld; }
    public int getRtpMaxAttempts() { return rtpMaxAttempts; }
    public boolean isRtpRequireSafeLocation() { return rtpRequireSafeLocation; }
    public Map<String, RTPRange> getRtpRanges() { return new HashMap<>(rtpRanges); }
    public RTPRange getRtpRange(String worldName) {
        return rtpRanges.getOrDefault(worldName, 
            new RTPRange(rtpMinDistance, rtpMaxDistance, 0, 0, true));
    }
    public boolean isRtpEnabledForWorld(String worldName) {
        RTPRange range = rtpRanges.get(worldName);
        return range != null && range.isEnabled();
    }
    public int getTpaRequestTimeout() { return tpaRequestTimeout; }
    public boolean isTpaRequireAccept() { return tpaRequireAccept; }
    public boolean isHomesEnabled() { return homesEnabled; }
    public boolean isTpaEnabled() { return tpaEnabled; }
    public boolean isRtpEnabled() { return rtpEnabled; }
    public boolean isBackEnabled() { return backEnabled; }
    public boolean isGuiEnabled() { return guiEnabled; }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }

    public static class RTPRange {
        private final int minDistance;
        private final int maxDistance;
        private final int centerX;
        private final int centerZ;
        private final boolean enabled;

        public RTPRange(int minDistance, int maxDistance, int centerX, int centerZ, boolean enabled) {
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.enabled = enabled;
        }

        public int getMinDistance() { return minDistance; }
        public int getMaxDistance() { return maxDistance; }
        public int getCenterX() { return centerX; }
        public int getCenterZ() { return centerZ; }
        public boolean isEnabled() { return enabled; }
    }
}
