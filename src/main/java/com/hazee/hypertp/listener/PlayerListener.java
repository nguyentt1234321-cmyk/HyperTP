package com.hazee.hypertp.listener;

import com.hazee.hypertp.HyperTP;
import com.hazee.hypertp.command.TPCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {
    private final HyperTP plugin;

    public PlayerListener(HyperTP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getTpaRequestManager().cleanupPlayer(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (plugin.getConfigLoader().isBackEnabled()) {
            TPCommand tpCommand = new TPCommand(plugin);
            tpCommand.storePreviousLocation(player, event.getFrom());
        }
    }
}
