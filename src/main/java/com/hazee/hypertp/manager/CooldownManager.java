package com.hazee.hypertp.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager {
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public void setCooldown(UUID playerId, String type, int seconds) {
        Map<String, Long> playerCooldowns = cooldowns.getOrDefault(playerId, new HashMap<>());
        playerCooldowns.put(type, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds));
        cooldowns.put(playerId, playerCooldowns);
    }

    public boolean hasCooldown(UUID playerId, String type) {
        if (!cooldowns.containsKey(playerId)) return false;
        
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (!playerCooldowns.containsKey(type)) return false;
        
        return playerCooldowns.get(type) > System.currentTimeMillis();
    }

    public int getRemainingCooldown(UUID playerId, String type) {
        if (!hasCooldown(playerId, type)) return 0;
        
        long remaining = cooldowns.get(playerId).get(type) - System.currentTimeMillis();
        return (int) TimeUnit.MILLISECONDS.toSeconds(remaining) + 1;
    }

    public void clearCooldown(UUID playerId, String type) {
        if (cooldowns.containsKey(playerId)) {
            cooldowns.get(playerId).remove(type);
        }
    }

    public void clearAllCooldowns(UUID playerId) {
        cooldowns.remove(playerId);
    }
}