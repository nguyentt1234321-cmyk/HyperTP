package com.hazee.hypertp.command;

import com.hazee.hypertp.HyperTP;
import com.hazee.hypertp.config.ConfigLoader;
import com.hazee.hypertp.gui.GUIManager;
import com.hazee.hypertp.manager.CooldownManager;
import com.hazee.hypertp.manager.LanguageManager;
import com.hazee.hypertp.manager.TPARequestManager;
import com.hazee.hypertp.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TeleportCommands extends BaseCommand {
    private final TPARequestManager tpaManager;
    private final LanguageManager languageManager;
    private final CooldownManager cooldownManager;
    private final GUIManager guiManager;
    private final Random random;

    public TeleportCommands(HyperTP plugin) {
        super(plugin);
        this.tpaManager = plugin.getTpaRequestManager();
        this.languageManager = plugin.getLanguageManager();
        this.cooldownManager = plugin.getCooldownManager();
        this.guiManager = new GUIManager(plugin);
        this.random = new Random();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.translateColorCodes("&cThis command can only be used by players."));
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "tpa":
                handleTpa(player, args);
                break;
            case "tpahere":
                handleTpaHere(player, args);
                break;
            case "tpaccept":
                handleTpAccept(player);
                break;
            case "tpdeny":
                handleTpDeny(player);
                break;
            case "rtp":
                handleRtp(player, args);
                break;
        }

        return true;
    }

    private void handleTpa(Player player, String[] args) {
        if (!isFeatureEnabled("tpa")) {
            sendFeatureDisabled(player, "tpa");
            return;
        }

        if (args.length == 0) {
            if (!isFeatureEnabled("gui")) {
                sendUsage(player, "/tpa <player>");
                return;
            }
            
            if (!checkGuiCooldown(player)) {
                return;
            }
            guiManager.openTPAGUI(player);
            setGuiCooldown(player);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(languageManager.getMessage(player, "player-not-found"));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(languageManager.getMessage(player, "self-teleport"));
            return;
        }

        if (tpaManager.sendRequest(player, target, false)) {
            player.sendMessage(languageManager.getMessage(player, "tpa-request-sent")
                .replace("{player}", target.getName()));
            target.sendMessage(languageManager.getMessage(target, "tpa-request-received")
                .replace("{player}", player.getName()));
        }
    }

    private void handleTpaHere(Player player, String[] args) {
        if (!isFeatureEnabled("tpa")) {
            sendFeatureDisabled(player, "tpa");
            return;
        }

        if (args.length == 0) {
            sendUsage(player, "/tpahere <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(languageManager.getMessage(player, "player-not-found"));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(languageManager.getMessage(player, "self-teleport"));
            return;
        }

        if (tpaManager.sendRequest(player, target, true)) {
            player.sendMessage(languageManager.getMessage(player, "tpa-request-sent")
                .replace("{player}", target.getName()));
            target.sendMessage(languageManager.getMessage(target, "tpa-request-received")
                .replace("{player}", player.getName()));
        }
    }

    private void handleTpAccept(Player player) {
        if (!isFeatureEnabled("tpa")) {
            sendFeatureDisabled(player, "tpa");
            return;
        }

        if (!hasPendingTpaRequest(player)) {
            return;
        }

        tpaManager.acceptRequest(player.getUniqueId());
        player.sendMessage(languageManager.getMessage(player, "tpa-accepted"));
    }

    private void handleTpDeny(Player player) {
        if (!isFeatureEnabled("tpa")) {
            sendFeatureDisabled(player, "tpa");
            return;
        }

        if (!hasPendingTpaRequest(player)) {
            return;
        }

        tpaManager.denyRequest(player.getUniqueId());
        player.sendMessage(languageManager.getMessage(player, "tpa-denied"));
    }

    private void handleRtp(Player player, String[] args) {
        if (!isFeatureEnabled("rtp")) {
            sendFeatureDisabled(player, "rtp");
            return;
        }

        if (!checkRtpCooldown(player)) {
            return;
        }

        World world = player.getWorld();
        String worldName = world.getName();
        
        if (!isRtpEnabledForPlayerWorld(player)) {
            return;
        }

        player.sendMessage(languageManager.getMessage(player, "rtp-search"));
        
        Location randomLoc = findRandomLocation(world);
        if (randomLoc != null) {
            player.teleport(randomLoc);
            player.sendMessage(languageManager.getMessage(player, "rtp-success"));
            setRtpCooldown(player);
        } else {
            player.sendMessage(languageManager.getMessage(player, "rtp-failed"));
        }
    }

    private Location findRandomLocation(World world) {
        ConfigLoader.RTPRange range = plugin.getConfigLoader().getRtpRange(world.getName());
        int maxAttempts = plugin.getConfigLoader().getRtpMaxAttempts();
        boolean requireSafe = plugin.getConfigLoader().isRtpRequireSafeLocation();
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Location testLoc = generateRandomLocation(world, range);
            
            if (requireSafe) {
                Location safeLoc = findSafeLocation(testLoc);
                if (safeLoc != null) {
                    return safeLoc;
                }
            } else {
                return testLoc;
            }
        }
        
        return null;
    }

    private Location generateRandomLocation(World world, ConfigLoader.RTPRange range) {
        int minDist = range.getMinDistance();
        int maxDist = range.getMaxDistance();
        int centerX = range.getCenterX();
        int centerZ = range.getCenterZ();
        
        int distance = random.nextInt(maxDist - minDist) + minDist;
        double angle = random.nextDouble() * 2 * Math.PI;
        
        int x = centerX + (int) (distance * Math.cos(angle));
        int z = centerZ + (int) (distance * Math.sin(angle));
        
        return new Location(world, x, 0, z);
    }

    private Location findSafeLocation(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int z = location.getBlockZ();
        
        int highestY = world.getHighestBlockYAt(x, z);
        if (highestY <= world.getMinHeight()) {
            return null;
        }
        
        Location candidate = new Location(world, x + 0.5, highestY + 1.0, z + 0.5);
        if (isSafeLocation(candidate)) {
            return candidate;
        }
        
        return null;
    }

    private boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        Block feet = world.getBlockAt(x, y, z);
        if (!feet.isPassable()) {
            return false;
        }
        
        Block head = world.getBlockAt(x, y + 1, z);
        if (!head.isPassable()) {
            return false;
        }
        
        Block below = world.getBlockAt(x, y - 1, z);
        if (below.isPassable() || isDangerousBlock(below.getType())) {
            return false;
        }
        
        return true;
    }

    private boolean isDangerousBlock(Material material) {
        return material == Material.LAVA ||
               material == Material.FIRE ||
               material == Material.CACTUS ||
               material == Material.MAGMA_BLOCK ||
               material.toString().contains("LAVA") ||
               material.toString().contains("FIRE");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return new ArrayList<>();
        
        switch (command.getName().toLowerCase()) {
            case "tpa":
            case "tpahere":
                return getPlayerTabComplete(args);
            default:
                return new ArrayList<>();
        }
    }
}
