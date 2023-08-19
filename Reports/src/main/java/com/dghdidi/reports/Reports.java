package com.dghdidi.reports;

import com.dghdidi.reports.Config.CreateConfig;
import com.dghdidi.reports.Config.LoadConfig;
import com.dghdidi.reports.Operation.StaffCMD;
import com.dghdidi.reports.Operation.ReportCMD;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class Reports extends Plugin {

    public static Connection connection;
    private static LuckPerms luckPerms;

    @Override
    public void onEnable() {
        luckPerms = LuckPermsProvider.get();
        getLogger().log(Level.INFO, "§a插件已成功加载!");
        CreateConfig.createDefaultConfig(this);
        try {
            LoadConfig.loadConfig(this);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "§e加载配置文件失败");
        }
        PluginManager pluginManager = getProxy().getPluginManager();
        pluginManager.registerListener(this, new MyListener());
        pluginManager.registerCommand(this, new StaffCMD(this));
        pluginManager.registerCommand(this, new ReportCMD(this));
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "§a插件已卸载!");
    }

    public static List<ProxiedPlayer> getPlayers(String perm) {
        List<ProxiedPlayer> players = new ArrayList<>();
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (player.hasPermission(perm)) {
                players.add(player);
            }
        }
        return players;
    }

    public static String getDisplayName(ProxiedPlayer player) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null)
            return null;
        String prefix = user.getCachedData().getMetaData().getPrefix() == null ? "" : user.getCachedData().getMetaData().getPrefix();
        String suffix = user.getCachedData().getMetaData().getSuffix() == null ? "" : user.getCachedData().getMetaData().getSuffix();
        return transColor(prefix) + player.getName() + transColor(suffix);
    }

    public static String getDisplayName(String playerName) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null)
            return null;
        String prefix = user.getCachedData().getMetaData().getPrefix() == null ? "" : user.getCachedData().getMetaData().getPrefix();
        String suffix = user.getCachedData().getMetaData().getSuffix() == null ? "" : user.getCachedData().getMetaData().getSuffix();
        return transColor(prefix) + player.getName() + transColor(suffix);
    }

    public static String transColor(String arg) {
        return ChatColor.translateAlternateColorCodes('&', String.join(" ", arg));
    }

    public static Iterable<String> getStrings(String[] args) {
        if (args.length == 1) {
            List<String> playerIDs = new ArrayList<>();
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                playerIDs.add(player.getName());
            }
            List<String> matchedIDs = new ArrayList<>();
            String partialID = args[0].toLowerCase();
            for (String id : playerIDs) {
                if (id.toLowerCase().startsWith(partialID)) {
                    matchedIDs.add(id);
                }
            }
            return matchedIDs;
        }
        return null;
    }
}
