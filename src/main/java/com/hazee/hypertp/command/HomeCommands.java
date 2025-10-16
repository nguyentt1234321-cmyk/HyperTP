package com.hazee.hypertp.command;

import com.hazee.hypertp.HyperTP;
import com.hazee.hypertp.gui.GUIManager;
import com.hazee.hypertp.manager.CooldownManager;
import com.hazee.hypertp.manager.HomeManager;
import com.hazee.hypertp.manager.LanguageManager;
import com.hazee.hypertp.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HomeCommands extends BaseCommand {
    private final HomeManager homeManager;
    private final LanguageManager languageManager;
    private final CooldownManager cooldownManager;
    private final GUIManager guiManager;

    public HomeCommands(HyperTP plugin) {
        super(plugin);
        this.homeManager = plugin.getHomeManager();
        this.languageManager = plugin.getLanguageManager();
        this.cooldownManager = plugin.getCooldownManager();
        this.guiManager = new GUIManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.translateColorCodes("&cThis command can only be used by players."));
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "home":
                handleHome(player, args);
                break;
            case "sethome":
                handleSetHome(player, args);
                break;
            case "delhome":
                handleDeleteHome(player, args);
                break;
            case "homelist":
                handleHomeList(player);
                break;
        }

        return true;
    }

    private void handleHome(Player player, String[] args) {
        if (!isFeatureEnabled("homes")) {
            sendFeatureDisabled(player, "homes");
            return;
        }

        if (args.length == 0) {
            if (!isFeatureEnabled("gui")) {
                sendUsage(player, "/home <name>");
                return;
            }
            
            if (!checkGuiCooldown(player)) {
                return;
            }
            
            guiManager.openHomeGUI(player);
            setGuiCooldown(player);
        } else {
            String homeName = args[0];
            if (!homeExists(player, homeName)) {
                return;
            }

            if (!checkTeleportCooldown(player)) {
                return;
            }

            teleportToHome(player, homeName);
            setTeleportCooldown(player);
        }
    }

    private void handleSetHome(Player player, String[] args) {
        if (!isFeatureEnabled("homes")) {
            sendFeatureDisabled(player, "homes");
            return;
        }

        if (args.length == 0) {
            sendUsage(player, "/sethome <name>");
            return;
        }

        String homeName = args[0];
        
        if (!isValidHomeName(homeName)) {
            player.sendMessage(ColorUtil.translateColorCodes("&cInvalid home name. Use only letters, numbers, and underscores (max 16 characters)."));
            return;
        }
        
        if (!canSetMoreHomes(player)) {
            return;
        }

        if (homeManager.setHome(player, homeName)) {
            sendHomeSuccess(player, "set", homeName);
        } else {
            player.sendMessage(languageManager.getMessage(player, "home-already-exists")
                .replace("{home}", homeName));
        }
    }

    private void handleDeleteHome(Player player, String[] args) {
        if (!isFeatureEnabled("homes")) {
            sendFeatureDisabled(player, "homes");
            return;
        }

        if (args.length == 0) {
            sendUsage(player, "/delhome <name>");
            return;
        }

        String homeName = args[0];
        if (homeManager.deleteHome(player, homeName)) {
            sendHomeSuccess(player, "deleted", homeName);
        } else {
            player.sendMessage(languageManager.getMessage(player, "home-not-found")
                .replace("{home}", homeName));
        }
    }

    private void handleHomeList(Player player) {
        if (!isFeatureEnabled("homes") || !isFeatureEnabled("gui")) {
            sendFeatureDisabled(player, "homes");
            return;
        }

        if (!checkGuiCooldown(player)) {
            return;
        }
        
        guiManager.openHomeGUI(player);
        setGuiCooldown(player);
    }

    private void teleportToHome(Player player, String homeName) {
        player.teleport(homeManager.getHome(player, homeName).toLocation());
        player.sendMessage(ColorUtil.translateColorCodes("&aTeleported to home &6" + homeName));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return new ArrayList<>();
        
        switch (command.getName().toLowerCase()) {
            case "home":
            case "delhome":
                return getHomeTabComplete(player, args);
            default:
                return new ArrayList<>();
        }
    }
}
