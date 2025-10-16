package com.hazee.hypertp.util;

import org.bukkit.ChatColor;

import java.util.regex.Pattern;

public class ColorUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#[a-fA-F0-9]{6}");
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§[0-9a-fk-or]");
    
    public static String translateColorCodes(String message) {
        if (message == null) return "";
        
        message = HEX_PATTERN.matcher(message).replaceAll(result -> {
            String hex = result.group().substring(2);
            return net.md_5.bungee.api.ChatColor.of("#" + hex).toString();
        });
        
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String stripColorCodes(String message) {
        if (message == null) return "";
        
        message = HEX_PATTERN.matcher(message).replaceAll("");
        message = COLOR_CODE_PATTERN.matcher(message).replaceAll("");
        message = message.replace("&0", "")
                        .replace("&1", "")
                        .replace("&2", "")
                        .replace("&3", "")
                        .replace("&4", "")
                        .replace("&5", "")
                        .replace("&6", "")
                        .replace("&7", "")
                        .replace("&8", "")
                        .replace("&9", "")
                        .replace("&a", "")
                        .replace("&b", "")
                        .replace("&c", "")
                        .replace("&d", "")
                        .replace("&e", "")
                        .replace("&f", "")
                        .replace("&k", "")
                        .replace("&l", "")
                        .replace("&m", "")
                        .replace("&n", "")
                        .replace("&o", "")
                        .replace("&r", "");
        
        return message;
    }

    public static String withPrefix(String message) {
        return "&6[HyperTP]&r " + message;
    }

    public static String error(String message) {
        return "&c" + message;
    }

    public static String success(String message) {
        return "&a" + message;
    }

    public static String warning(String message) {
        return "&e" + message;
    }

    public static String info(String message) {
        return "&7" + message;
    }

    public static String number(int number) {
        return "&6" + number + "&r";
    }

    public static String time(int seconds) {
        return "&e" + seconds + "&r";
    }

    // ✅ Thêm hàm này để tương thích với LanguageManager
    public static String format(String message) {
        return translateColorCodes(message);
    }
}
