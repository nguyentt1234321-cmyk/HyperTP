package com.hazee.hypertp.manager;

import com.hazee.hypertp.HyperTP;
import com.hazee.hypertp.config.HomeData;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HomeManager {
    private final HyperTP plugin;
    private final Map<UUID, Map<String, HomeData>> playerHomes = new HashMap<>();

    private File homesFile;
    private FileConfiguration homesConfig;

    public HomeManager(HyperTP plugin) {
        this.plugin = plugin;
        this.homesFile = new File(plugin.getDataFolder(), "homes.yml");
        if (!homesFile.exists()) {
            plugin.saveResource("homes.yml", false);
        }
        this.homesConfig = YamlConfiguration.loadConfiguration(homesFile);
    }

    /**
     * Load homes from homes.yml into memory.
     */
    public void loadHomes() {
        playerHomes.clear();
        if (!homesFile.exists()) {
            try {
                homesFile.getParentFile().mkdirs();
                homesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create homes.yml: " + e.getMessage());
            }
        }
        homesConfig = YamlConfiguration.loadConfiguration(homesFile);

        for (String uuidKey : homesConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidKey);
                Map<String, HomeData> map = new HashMap<>();
                if (homesConfig.isConfigurationSection(uuidKey)) {
                    for (String homeName : homesConfig.getConfigurationSection(uuidKey).getKeys(false)) {
                        String base = uuidKey + "." + homeName + ".";
                        String creator = homesConfig.getString(base + "creator", "unknown");
                        String world = homesConfig.getString(base + "world", "world");
                        double x = homesConfig.getDouble(base + "x", 0);
                        double y = homesConfig.getDouble(base + "y", 0);
                        double z = homesConfig.getDouble(base + "z", 0);
                        float yaw = (float) homesConfig.getDouble(base + "yaw", 0);
                        float pitch = (float) homesConfig.getDouble(base + "pitch", 0);
                        HomeData hd = new HomeData(homeName, world, x, y, z, yaw, pitch);
                        map.put(homeName, hd);
                    }
                }
                playerHomes.put(uuid, map);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid UUID key in homes.yml: " + uuidKey);
            }
        }

        plugin.getLogger().info("Homes Loaded: " + getTotalHomes());
    }

    /**
     * Save in-memory homes into homes.yml
     */
    public void saveHomes() {
        FileConfiguration cfg = homesConfig != null ? homesConfig : YamlConfiguration.loadConfiguration(homesFile);
        // Clear existing
        for (String key : new HashSet<>(cfg.getKeys(false))) {
            cfg.set(key, null);
        }

        for (Map.Entry<UUID, Map<String, HomeData>> e : playerHomes.entrySet()) {
            String uuid = e.getKey().toString();
            for (Map.Entry<String, HomeData> he : e.getValue().entrySet()) {
                HomeData hd = he.getValue();
                String base = uuid + "." + he.getKey() + ".";
                cfg.set(base + "creator", hd.getName()); // Note: HomeData currently stores name in 'name' field; store creator in creator field if available
                cfg.set(base + "world", hd.getWorld());
                cfg.set(base + "x", hd.getX());
                cfg.set(base + "y", hd.getY());
                cfg.set(base + "z", hd.getZ());
                cfg.set(base + "yaw", hd.getYaw());
                cfg.set(base + "pitch", hd.getPitch());
            }
        }

        try {
            cfg.save(homesFile);
            this.homesConfig = cfg;
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save homes.yml: " + e.getMessage());
        }
    }

    /**
     * Set a home for a player. If home with same name exists, return false.
     */
    public boolean setHome(Player player, String homeName) {
        UUID uuid = player.getUniqueId();
        Map<String, HomeData> homes = playerHomes.computeIfAbsent(uuid, k -> new HashMap<>());

        if (homes.containsKey(homeName)) {
            return false; // already exists
        }

        Location loc = player.getLocation();
        HomeData hd = new HomeData(homeName, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        homes.put(homeName, hd);
        saveHomes();
        return true;
    }

    public boolean deleteHome(Player player, String homeName) {
        UUID uuid = player.getUniqueId();
        Map<String, HomeData> homes = playerHomes.get(uuid);
        if (homes == null || !homes.containsKey(homeName)) return false;
        homes.remove(homeName);
        saveHomes();
        return true;
    }

    public HomeData getHome(Player player, String homeName) {
        Map<String, HomeData> homes = playerHomes.get(player.getUniqueId());
        return homes != null ? homes.get(homeName) : null;
    }

    public boolean hasHome(Player player, String homeName) {
        Map<String, HomeData> homes = playerHomes.get(player.getUniqueId());
        return homes != null && homes.containsKey(homeName);
    }

    public List<String> getHomeNames(Player player) {
        Map<String, HomeData> homes = playerHomes.get(player.getUniqueId());
        return homes != null ? new ArrayList<>(homes.keySet()) : new ArrayList<>();
    }

    public int getHomeCount(Player player) {
        Map<String, HomeData> homes = playerHomes.get(player.getUniqueId());
        return homes != null ? homes.size() : 0;
    }

    public int getTotalHomes() {
        int total = 0;
        for (Map<String, HomeData> m : playerHomes.values()) total += m.size();
        return total;
    }

    public void clearAllHomes(Player player) {
        playerHomes.remove(player.getUniqueId());
        saveHomes();
    }

    public List<HomeData> getAllPlayerHomes(Player player) {
        Map<String, HomeData> homes = playerHomes.get(player.getUniqueId());
        return homes != null ? new ArrayList<>(homes.values()) : new ArrayList<>();
    }
}
