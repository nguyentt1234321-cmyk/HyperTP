package com.hazee.hypertp.gui;

import com.hazee.hypertp.HyperTP;
import org.bukkit.entity.Player;

public class GUIManager {
    private final HyperTP plugin;
    private final CustomGUI customGUI;

    public GUIManager(HyperTP plugin) {
        this.plugin = plugin;
        this.customGUI = new CustomGUI(plugin);
    }

    public void openHomeGUI(Player player) {
        customGUI.openGUI(player, CustomGUI.GUIType.HOME);
    }

    public void openHomeGUI(Player player, int page) {
        customGUI.openGUI(player, CustomGUI.GUIType.HOME, page);
    }

    public void openTPAGUI(Player player) {
        customGUI.openGUI(player, CustomGUI.GUIType.TPA);
    }

    public void openRTPGUI(Player player) {
        customGUI.openGUI(player, CustomGUI.GUIType.RTP);
    }

    public void openHomeManagementGUI(Player player) {
        customGUI.openGUI(player, CustomGUI.GUIType.HOME_MANAGEMENT);
    }

    public void openHomeManagementGUI(Player player, int page) {
        customGUI.openGUI(player, CustomGUI.GUIType.HOME_MANAGEMENT, page);
    }

    public void closeGUI(Player player) {
        customGUI.closeGUI(player);
    }

    public CustomGUI getCustomGUI() {
        return customGUI;
    }
}