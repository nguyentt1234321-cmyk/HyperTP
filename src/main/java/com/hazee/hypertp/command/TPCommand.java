package com.hazee.hypertp.command;

import com.hazee.hypertp.HyperTP;
import com.hazee.hypertp.manager.CooldownManager;
import com.hazee.hypertp.manager.LanguageManager;
import com.hazee.hypertp.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TPCommand extends BaseCommand {
    private final Map<UUID, Location> previousLocations = new HashMap<>();

    public TPCommand(HyperTP plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.translateColorCodes("&cThis command can only be used by players."));
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "back":
                handleBack(player);
                break;
        }

        return true;
    }

    private void handleBack(Player player) {
        if (!isFeatureEnabled("back")) {
            sendFeatureDisabled(player, "back");
            return;
        }

        CooldownManager cooldownManager = plugin.getCooldownManager();
        LanguageManager languageManager = plugin.getLanguageManager();

        if (!checkBackCooldown(player)) {
            return;
        }

        Location previousLoc = previousLocations.get(player.getUniqueId());
        if (previousLoc == null) {
            player.sendMessage(languageManager.getMessage(player, "back-no-location"));
            return;
        }

        storePreviousLocation(player);

        player.teleport(previousLoc);
        player.sendMessage(ColorUtil.translateColorCodes("&aTeleported to previous location."));

        setBackCooldown(player);
    }

    public void storePreviousLocation(Player player) {
        previousLocations.put(player.getUniqueId(), player.getLocation());
    }

    public void storePreviousLocation(Player player, Location location) {
        previousLocations.put(player.getUniqueId(), location);
    }

    public Location getPreviousLocation(Player player) {
        return previousLocations.get(player.getUniqueId());
    }

    public boolean hasPreviousLocation(Player player) {
        return previousLocations.containsKey(player.getUniqueId());
    }

    public void clearPreviousLocation(Player player) {
        previousLocations.remove(player.getUniqueId());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
