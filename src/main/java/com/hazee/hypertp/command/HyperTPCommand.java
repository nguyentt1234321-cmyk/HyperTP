package com.hazee.hypertp.command;

import com.hazee.hypertp.HyperTP;
import com.hazee.hypertp.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HyperTPCommand extends BaseCommand {
    
    public HyperTPCommand(HyperTP plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "help":
                showHelp(sender, label);
                break;
            case "version":
            case "ver":
                showVersion(sender);
                break;
            default:
                sender.sendMessage(ColorUtil.translateColorCodes("&cUnknown subcommand. Use &e/" + label + " help &cfor help."));
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("hypertp.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(
                sender instanceof Player ? (Player) sender : null, 
                "no-permission"
            ));
            return;
        }

        try {
            plugin.reloadPlugin();
            sender.sendMessage(ColorUtil.translateColorCodes("&aHyperTP configuration reloaded successfully!"));
        } catch (Exception e) {
            sender.sendMessage(ColorUtil.translateColorCodes("&cError reloading configuration: " + e.getMessage()));
            plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
        }
    }

    private void showHelp(CommandSender sender, String label) {
        Player player = sender instanceof Player ? (Player) sender : null;
        
        List<String> helpLines = new ArrayList<>();
        helpLines.add("&6&lHyperTP &7- &eAdvanced Teleportation Plugin");
        helpLines.add("&7Version: &f" + plugin.getDescription().getVersion());
        helpLines.add("");
        
        if (sender.hasPermission("hypertp.home") || sender.hasPermission("hypertp.*")) {
            addHelpLine(helpLines, player, label + " home [name]", "Teleport to your home");
            addHelpLine(helpLines, player, label + " sethome <name>", "Set a new home");
            addHelpLine(helpLines, player, label + " delhome <name>", "Delete a home");
            addHelpLine(helpLines, player, label + " homelist", "List all your homes (GUI)");
        }
        
        if (sender.hasPermission("hypertp.tpa") || sender.hasPermission("hypertp.*")) {
            addHelpLine(helpLines, player, label + " tpa <player>", "Request to teleport to player");
            addHelpLine(helpLines, player, label + " tpahere <player>", "Request player to teleport to you");
            addHelpLine(helpLines, player, label + " tpaccept", "Accept teleport request");
            addHelpLine(helpLines, player, label + " tpdeny", "Deny teleport request");
        }
        
        if (sender.hasPermission("hypertp.rtp") || sender.hasPermission("hypertp.*")) {
            addHelpLine(helpLines, player, label + " rtp", "Random teleport");
        }
        
        if (sender.hasPermission("hypertp.back") || sender.hasPermission("hypertp.*")) {
            addHelpLine(helpLines, player, label + " back", "Return to previous location");
        }
        
        if (sender.hasPermission("hypertp.admin") || sender.isOp()) {
            helpLines.add("");
            helpLines.add("&6&lAdmin Commands:");
            addHelpLine(helpLines, player, label + " reload", "Reload plugin configuration");
            addHelpLine(helpLines, player, label + " version", "Show plugin version");
        }
        
        helpLines.add("");
        helpLines.add("&7Use &e/" + label + " help &7for this message");
        
        for (String line : helpLines) {
            sender.sendMessage(ColorUtil.translateColorCodes(line));
        }
    }

    private void addHelpLine(List<String> helpLines, Player player, String command, String description) {
        String format = plugin.getLanguageManager().getMessage(player, "help-format");
        if (format == null || format.equals("help-format")) {
            format = "&e/{command} &7- {description}";
        }
        
        String line = format
            .replace("{command}", command)
            .replace("{description}", description);
        helpLines.add(line);
    }

    private void showVersion(CommandSender sender) {
        Player player = sender instanceof Player ? (Player) sender : null;
        
        List<String> versionInfo = Arrays.asList(
            "&6&lHyperTP &7Version Information",
            "&7Version: &f" + plugin.getDescription().getVersion(),
            "&7Author: &f" + plugin.getDescription().getAuthors(),
            "&7Website: &f" + plugin.getDescription().getWebsite(),
            "&7API Version: &f" + plugin.getDescription().getAPIVersion(),
            "",
            "&7Supported MC Versions: &f1.21 - 1.21.8",
            "&7Default Language: &f" + plugin.getConfigLoader().getDefaultLanguage()
        );
        
        for (String line : versionInfo) {
            sender.sendMessage(ColorUtil.translateColorCodes(line));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("help");
            completions.add("version");
            
            if (sender.hasPermission("hypertp.admin") || sender.isOp()) {
                completions.add("reload");
            }
        }
        
        return completions;
    }
}
