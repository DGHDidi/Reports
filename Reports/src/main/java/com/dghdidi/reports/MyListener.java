package com.dghdidi.reports;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import static com.dghdidi.reports.Operation.ReportCMD.playerPrefix;
import static com.dghdidi.reports.Operation.ReportsStorage.getNum;

public class MyListener implements Listener {
    @EventHandler
    public void onStaffJoin(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (!player.hasPermission("reports.staff") || getNum() == 0)
            return;
        player.sendMessage(new TextComponent(playerPrefix + "§a当前有 §e" + getNum() + " §a个举报等待处理, 输入 §b/reports §a查看详情."));
    }
}
