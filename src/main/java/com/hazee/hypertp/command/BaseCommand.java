package com.hazee.hypertp.command;

import com.hazee.hypertp.HyperTP;
import com.hazee.hypertp.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCommand implements CommandExecutor, TabCompleter {
    protected final HyperTP plugin;

    public BaseCommand(HyperTP plugin) {
        this.plugin = plugin;
    }

    protected boolean checkPermission(Player player, String permission) {
        if (player == null) return false;
        
        if (!plugin.getConfigLoader().isUsePermission()) {
            return true;
        }
        
        if (!player.hasPermission(permission)) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "no-permission"));
            return false;
        }
        return true;
    }

    protected boolean isPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColorCodes("&cThis command can only be used by players."));
            return false;
        }
        return true;
    }

    protected boolean isPlayerWithPermission(CommandSender sender, String permission) {
        if (!isPlayer(sender)) return false;
        Player player = (Player) sender;
        return checkPermission(player, permission);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }

    protected List<String> getHomeTabComplete(Player player, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> homeNames = plugin.getHomeManager().getHomeNames(player);
            for (String home : homeNames) {
                if (home.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(home);
                }
            }
        }
        return completions;
    }

    protected List<String> getPlayerTabComplete(String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partialName)) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }

    protected void sendMessage(Player player, String key) {
        player.sendMessage(plugin.getLanguageManager().getMessage(player, key));
    }

    protected void sendMessage(Player player, String key, String... replacements) {
        String message = plugin.getLanguageManager().getMessage(player, key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        player.sendMessage(message);
    }

    protected void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player) {
            sender.sendMessage(message);
        } else {
            sender.sendMessage(ColorUtil.stripColorCodes(message));
        }
    }

    protected boolean isFeatureEnabled(String feature) {
        switch (feature.toLowerCase()) {
            case "homes":
                return plugin.getConfigLoader().isHomesEnabled();
            case "tpa":
                return plugin.getConfigLoader().isTpaEnabled();
            case "rtp":
                return plugin.getConfigLoader().isRtpEnabled();
            case "back":
                return plugin.getConfigLoader().isBackEnabled();
            case "gui":
                return plugin.getConfigLoader().isGuiEnabled();
            default:
                return true;
        }
    }

    protected void sendFeatureDisabled(Player player, String feature) {
        player.sendMessage(plugin.getLanguageManager().getMessage(player, "feature-disabled"));
    }

    protected boolean checkCooldown(Player player, String cooldownType) {
        if (plugin.getCooldownManager().hasCooldown(player.getUniqueId(), cooldownType)) {
            int remaining = plugin.getCooldownManager().getRemainingCooldown(player.getUniqueId(), cooldownType);
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "cooldown-active")
                .replace("{seconds}", String.valueOf(remaining)));
            return false;
        }
        return true;
    }

    protected boolean checkRtpCooldown(Player player) {
        return checkCooldown(player, "rtp");
    }

    protected boolean checkTeleportCooldown(Player player) {
        return checkCooldown(player, "teleport");
    }

    protected boolean checkGuiCooldown(Player player) {
        return checkCooldown(player, "gui");
    }

    protected boolean checkBackCooldown(Player player) {
        return checkCooldown(player, "back");
    }

    protected void setCooldown(Player player, String cooldownType) {
        int cooldownTime = 0;
        switch (cooldownType.toLowerCase()) {
            case "teleport":
                cooldownTime = plugin.getConfigLoader().getTeleportCooldown();
                break;
            case "back":
                cooldownTime = plugin.getConfigLoader().getBackCooldown();
                break;
            case "gui":
                cooldownTime = plugin.getConfigLoader().getGuiCooldown();
                break;
            case "rtp":
                cooldownTime = plugin.getConfigLoader().getRtpCooldown();
                break;
        }
        
        if (cooldownTime > 0) {
            plugin.getCooldownManager().setCooldown(player.getUniqueId(), cooldownType, cooldownTime);
        }
    }

    protected void setRtpCooldown(Player player) {
        setCooldown(player, "rtp");
    }

    protected void setTeleportCooldown(Player player) {
        setCooldown(player, "teleport");
    }

    protected void setGuiCooldown(Player player) {
        setCooldown(player, "gui");
    }

    protected void setBackCooldown(Player player) {
        setCooldown(player, "back");
    }

    protected boolean isValidHomeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.matches("^[a-zA-Z0-9_]{1,16}$");
    }

    protected String getPermission(String command) {
        return "hypertp." + command.toLowerCase();
    }

    protected boolean isRtpEnabledForPlayerWorld(Player player) {
        String worldName = player.getWorld().getName();
        boolean enabled = plugin.getConfigLoader().isRtpEnabledForWorld(worldName);
        
        if (!enabled) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "rtp-world-disabled")
                .replace("{world}", worldName));
        }
        
        return enabled;
    }

    protected String getRtpRangeInfo(Player player) {
        String worldName = player.getWorld().getName();
        var range = plugin.getConfigLoader().getRtpRange(worldName);
        return "&eRange: &f" + range.getMinDistance() + " - " + range.getMaxDistance() + " blocks";
    }

    protected String getRtpCooldownInfo() {
        int cooldown = plugin.getConfigLoader().getRtpCooldown();
        return "&eCooldown: &f" + cooldown + " seconds";
    }

    protected void sendUsage(Player player, String usage) {
        player.sendMessage(plugin.getLanguageManager().getMessage(player, "invalid-arguments")
            .replace("{usage}", usage));
    }

    protected boolean canSetMoreHomes(Player player) {
        int currentHomes = plugin.getHomeManager().getHomeCount(player);
        int maxHomes = plugin.getConfigLoader().getMaxHomes();
        
        if (currentHomes >= maxHomes) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "home-limit-reached")
                .replace("{max}", String.valueOf(maxHomes)));
            return false;
        }
        
        return true;
    }

    protected boolean homeExists(Player player, String homeName) {
        boolean exists = plugin.getHomeManager().hasHome(player, homeName);
        if (!exists) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "home-not-found")
                .replace("{home}", homeName));
        }
        return exists;
    }

    protected boolean homeNotExists(Player player, String homeName) {
        boolean exists = plugin.getHomeManager().hasHome(player, homeName);
        if (exists) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "home-already-exists")
                .replace("{home}", homeName));
        }
        return !exists;
    }

    protected Player findPlayer(String playerName) {
        Player target = plugin.getServer().getPlayer(playerName);
        if (target == null) {
            return null;
        }
        return target;
    }

    protected boolean isValidTarget(Player player, String targetName) {
        Player target = findPlayer(targetName);
        
        if (target == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "player-not-found"));
            return false;
        }
        
        if (target.equals(player)) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "self-teleport"));
            return false;
        }
        
        return true;
    }

    protected String getHomeCountInfo(Player player) {
        int current = plugin.getHomeManager().getHomeCount(player);
        int max = plugin.getConfigLoader().getMaxHomes();
        return "&7Homes: &e" + current + "&7/&6" + max;
    }

    protected String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        } else {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return minutes + "m " + remainingSeconds + "s";
        }
    }

    protected void sendHomeSuccess(Player player, String operation, String homeName) {
        String messageKey = "home-" + operation;
        player.sendMessage(plugin.getLanguageManager().getMessage(player, messageKey)
            .replace("{home}", homeName));
    }

    protected void sendTpaSuccess(Player player, String targetName) {
        player.sendMessage(plugin.getLanguageManager().getMessage(player, "tpa-request-sent")
            .replace("{player}", targetName));
    }

    protected boolean hasPendingTpaRequest(Player player) {
        boolean hasRequest = plugin.getTpaRequestManager().hasPendingRequest(player.getUniqueId());
        if (!hasRequest) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "tpa-no-request"));
        }
        return hasRequest;
    }
}
