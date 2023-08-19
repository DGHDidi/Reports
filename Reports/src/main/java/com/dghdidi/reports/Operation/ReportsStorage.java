package com.dghdidi.reports.Operation;


import javafx.util.Pair;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.dghdidi.reports.DataBase.GetInformation.getReportInfo;
import static com.dghdidi.reports.Reports.getDisplayName;

public class ReportsStorage {
    private static final Set<Integer> set = new HashSet<>();
    private static final Set<Pair<String, String>> reports = new HashSet<>();

    public static void add(int index, String playerName, String reportedName) {
        set.add(index);
        reports.add(new Pair<>(playerName, reportedName));
    }

    public static void del(int index, String playerName, String reportedName) {
        if (!set.contains(index))
            return;
        set.remove(index);
        reports.remove(new Pair<>(playerName, reportedName));
    }

    public static int getNum() {
        return set.size();
    }

    public static boolean contains(int index) {
        return !set.contains(index);
    }

    public static boolean containsPair(String playerName, String reportedName) {
        return reports.contains(new Pair<>(playerName, reportedName));
    }

    public static boolean showReport(ProxiedPlayer player, int index, boolean isSelf) throws SQLException {
        List<String> reportInfo = getReportInfo(index);
        if (reportInfo == null || !set.contains(index))
            return false;
        String playerName = reportInfo.get(0), reportedName = reportInfo.get(1),
                staffName = reportInfo.get(2), serverName = reportInfo.get(3), reason = reportInfo.get(4);
        if (isSelf ^ Objects.equals(staffName, player.getName()))
            return true;
        BaseComponent[] teleportTo = createClickableText("§8[§f传送至该玩家§8]", "/reports tpto " + reportedName, "§a点击传送到该玩家的位置");
        BaseComponent[] acceptReport;
        if (Objects.equals(staffName, "null")) {
            acceptReport = createClickableText("§8[§a受理举报§8]", "/reports accept " + index, "§a点击受理此举报");
        } else if (Objects.equals(staffName, player.getName())) {
            acceptReport = createClickableText("§8[§a§l您已受理§8]", "/reports accept " + index, "§a§l您已受理此举报");
        } else {
            acceptReport = createClickableText("§8§m[已被" + staffName + "受理]", "", "§c§l此举报已被受理");
        }
        BaseComponent[] completeReport = createClickableText("§8[§e完成举报§8]", "/reports complete " + index, "§a点击完成处理此举报");
        BaseComponent[] deleteReport = createClickableText("§8[§c删除举报§8]", "/reports delete " + index, "§c点击删除此举报");
        BaseComponent commandLine = new TextComponent("");
        commandLine.addExtra(teleportTo[0]);
        commandLine.addExtra("  ");
        commandLine.addExtra(acceptReport[0]);
        commandLine.addExtra("  ");
        commandLine.addExtra(completeReport[0]);
        commandLine.addExtra("  ");
        if (player.hasPermission("reports.staff.delete"))
            commandLine.addExtra(deleteReport[0]);
        player.sendMessage(new TextComponent("§8------------------------------------------"));
        player.sendMessage(new TextComponent("§e被举报人: §7" + (isOnline(reportedName) ? getDisplayName(reportedName) + "§8 (§a在线§8)" : "§8" + reportedName + "§8 (§c离线§8)\n")));
        player.sendMessage(new TextComponent("§a举报人: §7" + (isOnline(playerName) ? getDisplayName(playerName) + "§8 (§a在线§8)" : "§8" + playerName + "§8 (§c离线§8)")));
        player.sendMessage(new TextComponent("§b编号: §7#" + index + "  §b原因: §7" + reason));
        player.sendMessage(new TextComponent(""));
        player.sendMessage(commandLine);
        player.sendMessage(new TextComponent("§8------------------------------------------"));
        return true;
    }

    public static void showReports(ProxiedPlayer player) throws SQLException {
        for (int index : set)
            showReport(player, index, false);
        for (int index : set)
            showReport(player, index, true);
    }

    public static boolean isOnline(String playerName) {
        return ProxyServer.getInstance().getPlayer(playerName) != null;
    }

    private static BaseComponent[] createClickableText(String text, String Command, String HoverText) {
        return new ComponentBuilder(text)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, Command))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(HoverText).create()))
                .create();
    }
}
