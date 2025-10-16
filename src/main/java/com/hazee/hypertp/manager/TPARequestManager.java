package com.hazee.hypertp.manager;

import com.hazee.hypertp.HyperTP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;

public class TPARequestManager {
    private final HyperTP plugin;
    private final Map<UUID, TPARequest> pendingRequests = new HashMap<>();

    public TPARequestManager(HyperTP plugin) {
        this.plugin = plugin;
    }

    public boolean sendRequest(Player from, Player to, boolean isTpaHere) {
        UUID toUUID = to.getUniqueId();
        
        if (pendingRequests.containsKey(toUUID)) {
            TPARequest existing = pendingRequests.get(toUUID);
            if (existing.getFrom().equals(from.getUniqueId())) {
                pendingRequests.remove(toUUID);
            }
        }

        TPARequest request = new TPARequest(from.getUniqueId(), toUUID, isTpaHere);
        pendingRequests.put(toUUID, request);

        int timeoutSeconds = plugin.getConfigLoader().getTpaRequestTimeout();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.containsKey(toUUID) && pendingRequests.get(toUUID).equals(request)) {
                pendingRequests.remove(toUUID);
                Player fromPlayer = Bukkit.getPlayer(request.getFrom());
                if (fromPlayer != null && fromPlayer.isOnline()) {
                    fromPlayer.sendMessage(plugin.getLanguageManager().getMessage(fromPlayer, "tpa-request-timeout"));
                }
            }
        }, 20L * timeoutSeconds);

        return true;
    }

    public boolean hasPendingRequest(UUID playerUUID) {
        return pendingRequests.containsKey(playerUUID);
    }

    public TPARequest getPendingRequest(UUID playerUUID) {
        return pendingRequests.get(playerUUID);
    }

    public void acceptRequest(UUID playerUUID) {
        TPARequest request = pendingRequests.remove(playerUUID);
        if (request == null) return;

        Player from = Bukkit.getPlayer(request.getFrom());
        Player to = Bukkit.getPlayer(request.getTo());

        if (from != null && from.isOnline() && to != null && to.isOnline()) {
            if (request.isTpaHere()) {
                from.teleport(to.getLocation());
                from.sendMessage(plugin.getLanguageManager().getMessage(from, "tpa-accepted-by")
                    .replace("{player}", to.getName()));
            } else {
                to.teleport(from.getLocation());
                from.sendMessage(plugin.getLanguageManager().getMessage(from, "tpa-accepted-by")
                    .replace("{player}", to.getName()));
            }
        }
    }

    public void denyRequest(UUID playerUUID) {
        TPARequest request = pendingRequests.remove(playerUUID);
        if (request == null) return;

        Player from = Bukkit.getPlayer(request.getFrom());
        Player to = Bukkit.getPlayer(request.getTo());

        if (from != null && from.isOnline() && to != null && to.isOnline()) {
            from.sendMessage(plugin.getLanguageManager().getMessage(from, "tpa-denied-by")
                .replace("{player}", to.getName()));
        }
    }

    public void cleanupPlayer(UUID playerUUID) {
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().getFrom().equals(playerUUID));
        pendingRequests.remove(playerUUID);
    }

    public int getPendingRequestCount() {
        return pendingRequests.size();
    }

    private static class TPARequest {
        private final UUID from;
        private final UUID to;
        private final boolean isTpaHere;
        private final long timestamp;

        public TPARequest(UUID from, UUID to, boolean isTpaHere) {
            this.from = from;
            this.to = to;
            this.isTpaHere = isTpaHere;
            this.timestamp = System.currentTimeMillis();
        }

        public UUID getFrom() { return from; }
        public UUID getTo() { return to; }
        public boolean isTpaHere() { return isTpaHere; }
        public long getTimestamp() { return timestamp; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TPARequest that = (TPARequest) obj;
            return from.equals(that.from) && to.equals(that.to) && isTpaHere == that.isTpaHere;
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to, isTpaHere);
        }
    }
}
