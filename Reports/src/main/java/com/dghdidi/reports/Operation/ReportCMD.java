package com.dghdidi.reports.Operation;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

import static com.dghdidi.reports.DataBase.ConnectDataBase.executeQuery;
import static com.dghdidi.reports.Operation.ReportPunishment.getRemainTime;
import static com.dghdidi.reports.Operation.ReportPunishment.isUnderPunish;
import static com.dghdidi.reports.Operation.ReportsStorage.*;
import static com.dghdidi.reports.Reports.*;

public class ReportCMD extends Command implements TabExecutor {

    private final Plugin plugin;
    public static String playerPrefix;

    public ReportCMD(Plugin plugin) {
        super("report", "reports.player");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(new TextComponent("§c用法: /report <ID> <原因>"));
            return;
        }
        String reportedName = args[0], reason = args[1], playerName = sender.getName();
        ProxiedPlayer reportedPlayer = plugin.getProxy().getPlayer(reportedName);
        if (isUnderPunish((ProxiedPlayer) sender)) {
            sender.sendMessage(new TextComponent(playerPrefix + "§c§l举报失败 §7(您因为违规举报被工作人员处罚, 在 " + getRemainTime((ProxiedPlayer) sender) + "§7后才能进行举报"));
            return;
        }
        if (Objects.equals(reportedName, playerName)) {
            sender.sendMessage(new TextComponent(playerPrefix + "§c您不能举报你自己!"));
            return;
        }
        if (reportedPlayer == null) {
            sender.sendMessage(new TextComponent(playerPrefix + "§c该名玩家不存在或离线!"));
            return;
        }
        if (reportedPlayer.hasPermission("reports.bypass")) {
            sender.sendMessage(new TextComponent(playerPrefix + "§c你不能举报这名玩家!"));
            return;
        }
        if (containsPair(playerName, reportedName)) {
            sender.sendMessage(new TextComponent(playerPrefix + "§c您已举报过玩家 " + getDisplayName(reportedName) + "§c, 请勿重复举报!"));
            return;
        }
        String serverName = reportedPlayer.getServer().getInfo().getName();
        try {
            String sql = "INSERT INTO report_info (player_name, reported_name, staff_name, server_name, reason) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, playerName);
            statement.setString(2, reportedName);
            statement.setString(3, "null");
            statement.setString(4, serverName);
            statement.setString(5, reason);
            statement.executeUpdate();
            statement.close();
            ResultSet resultSet = executeQuery("SELECT MAX(`index`) AS last_index FROM report_info");
            if (resultSet.next()) {
                int nowIndex = resultSet.getInt("last_index");
                add(nowIndex, playerName, reportedName);
                for (ProxiedPlayer staff : getPlayers("reports.staff")) {
                    if (!showReport(staff, nowIndex, false))
                        staff.sendMessage(new TextComponent("§c该举报不存在!"));
                }

            }
        } catch (SQLException e) {
            sender.sendMessage(new TextComponent("§c数据库错误, 请联系管理员查看服务器后台."));
            throw new RuntimeException(e);
        }
        sender.sendMessage(new TextComponent(playerPrefix + "§a举报成功, 请耐心等待处理!"));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return args.length == 1 ? getStrings(args) : new ArrayList<>();
    }
}
