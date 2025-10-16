package com.hazee.hypertp.gui;

import com.hazee.hypertp.HyperTP;
import com.hazee.hypertp.config.HomeData;
import com.hazee.hypertp.manager.HomeManager;
import com.hazee.hypertp.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;

public class CustomGUI {
    private final HyperTP plugin;
    private final Map<UUID, Inventory> openGUIs = new HashMap<>();

    public CustomGUI(HyperTP plugin) {
        this.plugin = plugin;
    }

    public enum GUIType {
        HOME,
        TPA,
        RTP,
        HOME_MANAGEMENT
    }

    public void openGUI(Player player, GUIType guiType) {
        openGUI(player, guiType, 1);
    }

    public void openGUI(Player player, GUIType guiType, int page) {
        switch (guiType) {
            case HOME:
                openHomeGUI(player, page);
                break;
            case TPA:
                openTPAGUI(player);
                break;
            case RTP:
                openRTPGUI(player);
                break;
            case HOME_MANAGEMENT:
                openHomeManagementGUI(player, page);
                break;
        }
    }

    private void openHomeGUI(Player player, int page) {
        FileConfiguration guiConfig = plugin.getGuiConfigLoader().getGUIConfig("homegui");
        String title = getTitle(guiConfig, "&6Homes", page);
        int size = guiConfig.getInt("size", 54);
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        openGUIs.put(player.getUniqueId(), gui);
        
        setupBackground(gui, guiConfig);
        addHomeItems(player, gui, guiConfig, page);
        addNavigationItems(gui, guiConfig, page, getMaxHomePages(player), GUIType.HOME);
        
        player.openInventory(gui);
    }

    private void openTPAGUI(Player player) {
        FileConfiguration guiConfig = plugin.getGuiConfigLoader().getGUIConfig("tpagui");
        String title = getTitle(guiConfig, "&6TPA Requests");
        int size = guiConfig.getInt("size", 54);
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        openGUIs.put(player.getUniqueId(), gui);
        
        setupBackground(gui, guiConfig);
        addOnlinePlayers(player, gui, guiConfig);
        addTPAActionItems(gui, guiConfig);
        
        player.openInventory(gui);
    }

    private void openRTPGUI(Player player) {
        FileConfiguration guiConfig = plugin.getGuiConfigLoader().getGUIConfig("rtpgui");
        String title = getTitle(guiConfig, "&6Random Teleport");
        int size = guiConfig.getInt("size", 27);
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        openGUIs.put(player.getUniqueId(), gui);
        
        setupBackground(gui, guiConfig);
        addRTPItems(player, gui, guiConfig);
        
        player.openInventory(gui);
    }

    public void openHomeManagementGUI(Player player) {
        openHomeManagementGUI(player, 1);
    }

    public void openHomeManagementGUI(Player player, int page) {
        FileConfiguration guiConfig = plugin.getGuiConfigLoader().getGUIConfig("homegui");
        String title = getTitle(guiConfig, "&6Manage Homes", page);
        int size = guiConfig.getInt("size", 54);
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        openGUIs.put(player.getUniqueId(), gui);
        
        setupBackground(gui, guiConfig);
        addHomeManagementItems(player, gui, guiConfig, page);
        addNavigationItems(gui, guiConfig, page, getMaxHomePages(player), GUIType.HOME_MANAGEMENT);
        
        player.openInventory(gui);
    }

    private void addHomeItems(Player player, Inventory gui, FileConfiguration config, int page) {
        HomeManager homeManager = plugin.getHomeManager();
        List<String> homeNames = homeManager.getHomeNames(player);
        
        int itemsPerPage = 28;
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, homeNames.size());
        
        int slot = 10;
        int row = 0;
        
        for (int i = startIndex; i < endIndex; i++) {
            String homeName = homeNames.get(i);
            HomeData home = homeManager.getHome(player, homeName);
            
            ItemStack homeItem = createHomeItem(homeName, home, config, false);
            gui.setItem(slot, homeItem);
            
            slot++;
            row++;
            
            if (row % 7 == 0) {
                slot += 2;
            }
            
            if (slot >= 43) break;
        }
    }

    private void addHomeManagementItems(Player player, Inventory gui, FileConfiguration config, int page) {
        HomeManager homeManager = plugin.getHomeManager();
        List<String> homeNames = homeManager.getHomeNames(player);
        
        int itemsPerPage = 28;
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, homeNames.size());
        
        int slot = 10;
        int row = 0;
        
        for (int i = startIndex; i < endIndex; i++) {
            String homeName = homeNames.get(i);
            HomeData home = homeManager.getHome(player, homeName);
            
            ItemStack homeItem = createHomeItem(homeName, home, config, true);
            gui.setItem(slot, homeItem);
            
            slot++;
            row++;
            
            if (row % 7 == 0) {
                slot += 2;
            }
            
            if (slot >= 43) break;
        }
    }

    private void addOnlinePlayers(Player player, Inventory gui, FileConfiguration config) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.remove(player);
        
        int slot = 10;
        for (Player onlinePlayer : onlinePlayers) {
            if (slot >= 44) break;
            
            ItemStack playerHead = createPlayerHead(onlinePlayer, config);
            gui.setItem(slot, playerHead);
            
            slot++;
            if ((slot - 9) % 9 == 0) slot += 2;
        }
    }

    private void addTPAActionItems(Inventory gui, FileConfiguration config) {
        ItemStack tpaHereItem = createTPAItem(config, "tpa-here", "&bTPA Here", 
            Arrays.asList("&7Request player to", "&7teleport to you", "", "&eClick to select player"));
        gui.setItem(48, tpaHereItem);
        
        ItemStack tpaToItem = createTPAItem(config, "tpa-to", "&aTPA To", 
            Arrays.asList("&7Request to teleport", "&7to player", "", "&eClick to select player"));
        gui.setItem(50, tpaToItem);
        
        ItemStack closeItem = createCloseItem(config);
        gui.setItem(53, closeItem);
    }

    private void addRTPItems(Player player, Inventory gui, FileConfiguration config) {
        ItemStack rtpItem = createRTPItem(player, config);
        gui.setItem(13, rtpItem);
        
        ItemStack closeItem = createCloseItem(config);
        gui.setItem(26, closeItem);
    }

    private ItemStack createHomeItem(String homeName, HomeData home, FileConfiguration config, boolean management) {
        String itemPath = management ? "items.home-management" : "items.home";
        Material material = Material.getMaterial(config.getString(itemPath + ".material", "GRASS_BLOCK"));
        if (material == null) material = Material.GRASS_BLOCK;
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String displayName = ColorUtil.translateColorCodes(
            config.getString(itemPath + ".display-name", "&6{home}")
                .replace("{home}", homeName)
        );
        meta.setDisplayName(displayName);
        
        List<String> lore = new ArrayList<>();
        for (String line : config.getStringList(itemPath + ".lore")) {
            String formattedLine = line
                .replace("{world}", home.getWorld())
                .replace("{x}", String.format("%.0f", home.getX()))
                .replace("{y}", String.format("%.0f", home.getY()))
                .replace("{z}", String.format("%.0f", home.getZ()));
            lore.add(ColorUtil.translateColorCodes(formattedLine));
        }
        
        if (management) {
            lore.add("");
            lore.add(ColorUtil.translateColorCodes("&cShift + Click to delete"));
        } else {
            lore.add("");
            lore.add(ColorUtil.translateColorCodes("&eClick to teleport"));
        }
        
        meta.setLore(lore);
        
        if (config.contains(itemPath + ".custom-model-data")) {
            meta.setCustomModelData(config.getInt(itemPath + ".custom-model-data"));
        }
        
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPlayerHead(Player player, FileConfiguration config) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        meta.setOwningPlayer(player);
        meta.setDisplayName(ColorUtil.translateColorCodes("&a" + player.getName()));
        
        List<String> lore = Arrays.asList(
            "&7Click to send TPA request",
            "",
            "&eLeft-Click: &6TPA To",
            "&eRight-Click: &6TPA Here"
        ).stream().map(ColorUtil::translateColorCodes).collect(Collectors.toList());
        
        meta.setLore(lore);
        head.setItemMeta(meta);
        
        return head;
    }

    private ItemStack createTPAItem(FileConfiguration config, String itemType, String defaultName, List<String> defaultLore) {
        Material material = Material.getMaterial(config.getString("items." + itemType + ".material", "ENDER_PEARL"));
        if (material == null) material = Material.ENDER_PEARL;
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String displayName = ColorUtil.translateColorCodes(
            config.getString("items." + itemType + ".display-name", defaultName)
        );
        meta.setDisplayName(displayName);
        
        List<String> lore = config.getStringList("items." + itemType + ".lore");
        if (lore.isEmpty()) lore = defaultLore;
        
        meta.setLore(lore.stream().map(ColorUtil::translateColorCodes).collect(Collectors.toList()));
        
        if (config.contains("items." + itemType + ".custom-model-data")) {
            meta.setCustomModelData(config.getInt("items." + itemType + ".custom-model-data"));
        }
        
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createRTPItem(Player player, FileConfiguration config) {
        Material material = Material.getMaterial(config.getString("items.rtp.material", "COMPASS"));
        if (material == null) material = Material.COMPASS;
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String displayName = ColorUtil.translateColorCodes(
            config.getString("items.rtp.display-name", "&aRandom Teleport")
        );
        meta.setDisplayName(displayName);
        
        List<String> lore = new ArrayList<>();
        for (String line : config.getStringList("items.rtp.lore")) {
            String formattedLine = line.replace("{cooldown}", 
                String.valueOf(plugin.getConfigLoader().getRtpCooldown()));
            lore.add(ColorUtil.translateColorCodes(formattedLine));
        }
        
        lore.add("");
        lore.add(ColorUtil.translateColorCodes("&eClick to teleport randomly!"));
        
        meta.setLore(lore);
        
        if (config.contains("items.rtp.custom-model-data")) {
            meta.setCustomModelData(config.getInt("items.rtp.custom-model-data"));
        }
        
        item.setItemMeta(meta);
        return item;
    }

    private void addWorldSelectionItems(Inventory gui, FileConfiguration config) {
    }

    private ItemStack createCloseItem(FileConfiguration config) {
        Material material = Material.getMaterial(config.getString("items.close.material", "BARRIER"));
        if (material == null) material = Material.BARRIER;
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ColorUtil.translateColorCodes(
            config.getString("items.close.display-name", "&cClose")
        ));
        
        List<String> lore = config.getStringList("items.close.lore").stream()
            .map(ColorUtil::translateColorCodes)
            .collect(Collectors.toList());
        
        meta.setLore(lore);
        
        if (config.contains("items.close.custom-model-data")) {
            meta.setCustomModelData(config.getInt("items.close.custom-model-data"));
        }
        
        item.setItemMeta(meta);
        return item;
    }

    private void addNavigationItems(Inventory gui, FileConfiguration config, int currentPage, int maxPages, GUIType guiType) {
        if (currentPage > 1) {
            ItemStack prevPage = createNavigationItem(config, "previous-page", "&6Previous Page", 
                Arrays.asList("&7Go to page " + (currentPage - 1)));
            gui.setItem(45, prevPage);
        }
        
        ItemStack pageInfo = createNavigationItem(config, "page-info", "&ePage " + currentPage + "&7/&6" + maxPages, 
            Arrays.asList("&7Current page: &e" + currentPage, "&7Total pages: &6" + maxPages));
        gui.setItem(49, pageInfo);
        
        if (currentPage < maxPages) {
            ItemStack nextPage = createNavigationItem(config, "next-page", "&6Next Page", 
                Arrays.asList("&7Go to page " + (currentPage + 1)));
            gui.setItem(53, nextPage);
        }
        
        ItemStack closeItem = createCloseItem(config);
        gui.setItem(48, closeItem);
        
        if (guiType == GUIType.HOME_MANAGEMENT) {
            ItemStack backItem = createNavigationItem(config, "back-to-homes", "&aBack to Homes", 
                Arrays.asList("&7Return to homes list"));
            gui.setItem(50, backItem);
        }
    }

    private ItemStack createNavigationItem(FileConfiguration config, String itemType, String defaultName, List<String> defaultLore) {
        Material material = Material.getMaterial(config.getString("navigation." + itemType + ".material", "PAPER"));
        if (material == null) material = Material.PAPER;
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String displayName = ColorUtil.translateColorCodes(
            config.getString("navigation." + itemType + ".display-name", defaultName)
        );
        meta.setDisplayName(displayName);
        
        List<String> lore = config.getStringList("navigation." + itemType + ".lore");
        if (lore.isEmpty()) lore = defaultLore;
        
        meta.setLore(lore.stream().map(ColorUtil::translateColorCodes).collect(Collectors.toList()));
        
        if (config.contains("navigation." + itemType + ".custom-model-data")) {
            meta.setCustomModelData(config.getInt("navigation." + itemType + ".custom-model-data"));
        }
        
        item.setItemMeta(meta);
        return item;
    }

    private void setupBackground(Inventory gui, FileConfiguration config) {
        if (!config.contains("background")) return;
        
        Material bgMaterial = Material.getMaterial(config.getString("background.material", "GRAY_STAINED_GLASS_PANE"));
        if (bgMaterial == null) bgMaterial = Material.GRAY_STAINED_GLASS_PANE;
        
        ItemStack bgItem = new ItemStack(bgMaterial);
        ItemMeta meta = bgItem.getItemMeta();
        meta.setDisplayName(" ");
        bgItem.setItemMeta(meta);
        
        for (int slot : config.getIntegerList("background.slots")) {
            if (slot >= 0 && slot < gui.getSize()) {
                gui.setItem(slot, bgItem);
            }
        }
        
        if (config.contains("background.pattern")) {
            List<String> pattern = config.getStringList("background.pattern");
            setBackgroundPattern(gui, pattern, bgItem);
        }
    }

    private void setBackgroundPattern(Inventory gui, List<String> pattern, ItemStack bgItem) {
        for (int row = 0; row < pattern.size(); row++) {
            String rowPattern = pattern.get(row);
            for (int col = 0; col < rowPattern.length(); col++) {
                if (rowPattern.charAt(col) == 'X') {
                    int slot = row * 9 + col;
                    if (slot < gui.getSize()) {
                        gui.setItem(slot, bgItem);
                    }
                }
            }
        }
    }

    private String getTitle(FileConfiguration config, String defaultTitle, int page) {
        String title = config.getString("title", defaultTitle);
        title = title.replace("{page}", String.valueOf(page));
        return ColorUtil.translateColorCodes(title);
    }

    private String getTitle(FileConfiguration config, String defaultTitle) {
        return getTitle(config, defaultTitle, 1);
    }

    private int getMaxHomePages(Player player) {
        HomeManager homeManager = plugin.getHomeManager();
        int homeCount = homeManager.getHomeCount(player);
        int itemsPerPage = 28;
        return (int) Math.ceil((double) homeCount / itemsPerPage);
    }

    public void closeGUI(Player player) {
        openGUIs.remove(player.getUniqueId());
    }

    public Inventory getOpenGUI(Player player) {
        return openGUIs.get(player.getUniqueId());
    }

    public boolean hasOpenGUI(Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }
}
