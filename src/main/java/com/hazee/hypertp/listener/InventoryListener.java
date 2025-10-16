package com.hazee.hypertp.listener;

import com.hazee.hypertp.HyperTP;
import com.hazee.hypertp.gui.CustomGUI;
import com.hazee.hypertp.manager.CooldownManager;
import com.hazee.hypertp.manager.HomeManager;
import com.hazee.hypertp.manager.LanguageManager;
import com.hazee.hypertp.manager.TPARequestManager;
import com.hazee.hypertp.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.regex.Pattern;

public class InventoryListener implements Listener {
    private final HyperTP plugin;
    private final CustomGUI customGUI;
    private final Pattern COLOR_PATTERN = Pattern.compile("§[0-9a-fk-or]");

    public InventoryListener(HyperTP plugin) {
        this.plugin = plugin;
        this.customGUI = new CustomGUI(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        
        String inventoryTitle = event.getView().getTitle();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        
        event.setCancelled(true);
        
        if (inventoryTitle.contains("Homes") && !inventoryTitle.contains("Manage")) {
            handleHomeGUIClick(player, clickedItem, event.isShiftClick());
        }
        else if (inventoryTitle.contains("TPA")) {
            handleTPAGUIClick(player, clickedItem, event.getSlot());
        }
        else if (inventoryTitle.contains("Random Teleport")) {
            handleRTPGUIClick(player, clickedItem);
        }
        else if (inventoryTitle.contains("Manage Homes")) {
            handleHomeManagementGUIClick(player, clickedItem, event.isShiftClick());
        }
        
        handleNavigationClick(player, clickedItem, inventoryTitle);
    }

    private void handleHomeGUIClick(Player player, ItemStack item, boolean isShiftClick) {
        String homeName = extractHomeName(item);
        if (homeName != null) {
            HomeManager homeManager = plugin.getHomeManager();
            CooldownManager cooldownManager = plugin.getCooldownManager();
            LanguageManager languageManager = plugin.getLanguageManager();
            
            if (cooldownManager.hasCooldown(player.getUniqueId(), "teleport")) {
                int remaining = cooldownManager.getRemainingCooldown(player.getUniqueId(), "teleport");
                player.sendMessage(languageManager.getMessage(player, "cooldown-active")
                    .replace("{seconds}", String.valueOf(remaining)));
                player.closeInventory();
                return;
            }
            
            if (homeManager.hasHome(player, homeName)) {
                player.teleport(homeManager.getHome(player, homeName).toLocation());
                player.sendMessage(ColorUtil.translateColorCodes("&aTeleported to home &6" + homeName));
                cooldownManager.setCooldown(player.getUniqueId(), "teleport", 
                    plugin.getConfigLoader().getTeleportCooldown());
                player.closeInventory();
            }
        }
    }

    private void handleHomeManagementGUIClick(Player player, ItemStack item, boolean isShiftClick) {
        String homeName = extractHomeName(item);
        if (homeName != null) {
            HomeManager homeManager = plugin.getHomeManager();
            LanguageManager languageManager = plugin.getLanguageManager();
            
            if (isShiftClick) {
                if (homeManager.deleteHome(player, homeName)) {
                    player.sendMessage(languageManager.getMessage(player, "home-deleted")
                        .replace("{home}", homeName));
                    player.closeInventory();
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        customGUI.openHomeManagementGUI(player, 1);
                    }, 5L);
                }
            } else {
                CooldownManager cooldownManager = plugin.getCooldownManager();
                
                if (cooldownManager.hasCooldown(player.getUniqueId(), "teleport")) {
                    int remaining = cooldownManager.getRemainingCooldown(player.getUniqueId(), "teleport");
                    player.sendMessage(languageManager.getMessage(player, "cooldown-active")
                        .replace("{seconds}", String.valueOf(remaining)));
                    return;
                }
                
                player.teleport(homeManager.getHome(player, homeName).toLocation());
                player.sendMessage(ColorUtil.translateColorCodes("&aTeleported to home &6" + homeName));
                cooldownManager.setCooldown(player.getUniqueId(), "teleport", 
                    plugin.getConfigLoader().getTeleportCooldown());
                player.closeInventory();
            }
        }
    }

    private void handleTPAGUIClick(Player player, ItemStack item, int slot) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        
        String displayName = ColorUtil.translateColorCodes(meta.getDisplayName());
        
        if (item.getType().toString().contains("PLAYER_HEAD")) {
            String targetName = cleanDisplayName(meta.getDisplayName());
            Player target = Bukkit.getPlayer(targetName);
            
            if (target != null && !target.equals(player)) {
                TPARequestManager tpaManager = plugin.getTpaRequestManager();
                boolean isTpaHere = displayName.contains("TPA Here") || slot >= 36;
                
                if (tpaManager.sendRequest(player, target, isTpaHere)) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player, "tpa-request-sent")
                        .replace("{player}", target.getName()));
                    target.sendMessage(plugin.getLanguageManager().getMessage(target, "tpa-request-received")
                        .replace("{player}", player.getName()));
                    player.closeInventory();
                }
            }
        }
        
        if (displayName.contains("TPA Here")) {
            player.sendMessage(ColorUtil.translateColorCodes("&eClick on a player head to send TPA Here request"));
        } else if (displayName.contains("TPA To")) {
            player.sendMessage(ColorUtil.translateColorCodes("&eClick on a player head to send TPA To request"));
        }
    }

    private void handleRTPGUIClick(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        
        String displayName = ColorUtil.translateColorCodes(meta.getDisplayName());
        
        if (displayName.contains("Random Teleport")) {
            player.closeInventory();
            player.performCommand("rtp");
        }
    }

    private void handleNavigationClick(Player player, ItemStack item, String inventoryTitle) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        
        String displayName = ColorUtil.translateColorCodes(meta.getDisplayName());
        
        if (displayName.contains("Close")) {
            player.closeInventory();
        } else if (displayName.contains("Previous Page")) {
            customGUI.openGUI(player, CustomGUI.GUIType.HOME, 1);
        } else if (displayName.contains("Next Page")) {
            customGUI.openGUI(player, CustomGUI.GUIType.HOME, 2);
        } else if (displayName.contains("Back to Homes")) {
            customGUI.openGUI(player, CustomGUI.GUIType.HOME);
        }
    }

    private String extractHomeName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return null;
        
        String displayName = meta.getDisplayName();
        String cleanName = cleanDisplayName(displayName);
        
        if (!cleanName.isEmpty() && !cleanName.equals(" ") && 
            !cleanName.contains("Page") && !cleanName.contains("Close") && 
            !cleanName.contains("Previous") && !cleanName.contains("Next") &&
            !cleanName.contains("TPA") && !cleanName.contains("Random")) {
            return cleanName;
        }
        return null;
    }

    private String cleanDisplayName(String displayName) {
        return COLOR_PATTERN.matcher(displayName).replaceAll("").trim();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            customGUI.closeGUI(player);
        }
    }
}
