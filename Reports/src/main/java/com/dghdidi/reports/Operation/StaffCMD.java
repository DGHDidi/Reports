package com.dghdidi.reports.Operation;

import com.dghdidi.reports.Config.LoadConfig;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.dghdidi.reports.DataBase.ConnectDataBase.executeCommand;
import static com.dghdidi.reports.DataBase.ConnectDataBase.executeQuery;
import static com.dghdidi.reports.DataBase.GetInformation.getReportInfo;
import static com.dghdidi.reports.Operation.ReportCMD.playerPrefix;
import static com.dghdidi.reports.Operation.ReportPunishment.*;
import static com.dghdidi.reports.Operation.ReportsStorage.*;
import static com.dghdidi.reports.Operation.StaffBroadCast.staffBC;
import static com.dghdidi.reports.Reports.getDisplayName;
import static com.dghdidi.reports.Reports.getStrings;

public class StaffCMD extends Command implements TabExecutor {

    private final Plugin plugin;
    public static int delay;

    public StaffCMD(Plugin plugin) {
        super("reports", "reports.staff");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender senderPlayer, String[] args) {
        if (!(senderPlayer instanceof ProxiedPlayer)) {
            senderPlayer.sendMessage(new TextComponent(playerPrefix + "§c只有玩家才能使用此命令!"));
            return;
        }
        ProxiedPlayer sender = (ProxiedPlayer) senderPlayer;
        if (args.length == 0) showAllReports(sender);
        else if (Objects.equals(args[0].toLowerCase(), "reload")) reloadPlugin(sender);
        else if (Objects.equals(args[0].toLowerCase(), "accept")) acceptReport(sender, args);
        else if (Objects.equals(args[0].toLowerCase(), "complete")) completeReport(sender, args);
        else if (Objects.equals(args[0].toLowerCase(), "delete")) deleteReport(sender, args);
        else if (Objects.equals(args[0].toLowerCase(), "tpto")) teleportTo(sender, args);
        else if (Objects.equals(args[0].toLowerCase(), "punish")) Punish(sender, args);
        else if (Objects.equals(args[0].toLowerCase(), "cancelpunish")) cancelPunish(sender, args);
        else if (Objects.equals(args[0].toLowerCase(), "stats")) getStats(sender, args);
        else if (Objects.equals(args[0].toLowerCase(), "help")) getHelp(sender);
        else if (Objects.equals(args[0].toLowerCase(), "clear")) clearReports(sender, args);
        else sender.sendMessage(new TextComponent("§c未知命令. 请输入 /reports help 查看可用命令!"));
    }

    private void showAllReports(ProxiedPlayer sender) {
        if (getNum() == 0) {
            sender.sendMessage(new TextComponent(playerPrefix + "§c当前没有未处理的举报!"));
            return;
        }
        try {
            showReports(sender);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void reloadPlugin(ProxiedPlayer sender) {
        if (!sender.hasPermission("reports.reload")) {
            sender.sendMessage(new TextComponent("§c你没有权限使用此命令!"));
            return;
        }
        try {
            if (LoadConfig.loadConfig(plugin)) sender.sendMessage(new TextComponent("§a成功重载Reports!"));
            else sender.sendMessage(new TextComponent("§c重载插件出错, 请检查配置文件!"));
        } catch (IOException e) {
            sender.sendMessage(new TextComponent("§c重载插件出错, 请检查配置文件!"));
        }
    }

    private void acceptReport(ProxiedPlayer sender, String[] args) {
        if (!sender.hasPermission("reports.staff.accept")) {
            sender.sendMessage(new TextComponent("§c你没有权限使用此命令!"));
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(new TextComponent("§c用法: /reports accept <编号>"));
            return;
        }
        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponent("§c数字格式不符! 例: /reports accept 233"));
            return;
        }
        try {
            List<String> result = getReportInfo(index);
            if (result == null || contains(index)) {
                sender.sendMessage(new TextComponent("§c该举报不存在!"));
                return;
            }
            String staffName = result.get(2), playerName = result.get(0);
            if (!Objects.equals(staffName, "null")) {
                sender.sendMessage(new TextComponent("§c该举报已被受理!"));
                return;
            }
            executeCommand("UPDATE report_info SET `staff_name` = '" + sender.getName() + "' WHERE `index` = " + index);
            staffBC("§7" + getDisplayName(sender) + " §e受理了举报 §7#" + index);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void completeReport(ProxiedPlayer sender, String[] args) {
        if (!sender.hasPermission("reports.staff.complete")) {
            sender.sendMessage(new TextComponent("§c你没有权限使用此命令!"));
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(new TextComponent("§c用法: /reports complete <编号>"));
            return;
        }
        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponent("§c数字格式不符! 例: /reports complete 233"));
            return;
        }
        try {
            List<String> result = getReportInfo(index);
            if (result == null || contains(index)) {
                sender.sendMessage(new TextComponent("§c该举报不存在!"));
                return;
            }
            String staffName = result.get(2), playerName = result.get(0), reportedName = result.get(1);
            if (!Objects.equals(staffName, sender.getName())) {
                sender.sendMessage(new TextComponent("§c请处理自己受理的举报!"));
                return;
            }
            del(index, playerName, reportedName);
            staffBC("§7" + getDisplayName(sender) + " §a处理了举报 §7#" + args[1]);
            ProxiedPlayer player = plugin.getProxy().getPlayer(playerName);
            if (player != null)
                player.sendMessage(new TextComponent(playerPrefix + "§a§l您的举报已被工作人员完成处理, 感谢您对游戏环境的维护!"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteReport(ProxiedPlayer sender, String[] args) {
        if (!sender.hasPermission("reports.staff.delete")) {
            sender.sendMessage(new TextComponent("§c你没有权限使用此命令!"));
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(new TextComponent("§c用法: /reports delete <编号>"));
            return;
        }
        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponent("§c数字格式不符! 例: /reports delete 233"));
            return;
        }
        List<String> result = null;
        try {
            result = getReportInfo(index);
            if (result == null || contains(index)) {
                sender.sendMessage(new TextComponent("§c该举报不存在!"));
                return;
            }
            String playerName = result.get(0), reportedName = result.get(1);
            del(index, playerName, reportedName);
            staffBC("§7" + getDisplayName(sender) + " §c删除了举报 §7#" + args[1]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void teleportTo(ProxiedPlayer sender, String[] args) {
        if (!sender.hasPermission("reports.staff.tpto")) {
            sender.sendMessage(new TextComponent("§c你没有权限使用此命令!"));
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(new TextComponent("§c用法: /reports tpto <ID>"));
            return;
        }
        String targetID = args[1];
        ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(targetID);
        if (targetPlayer == null) {
            sender.sendMessage(new TextComponent("§c该名玩家不在线或不存在!"));
            return;
        }
        Server server = targetPlayer.getServer();
        if (server.getInfo().equals(sender.getServer().getInfo())) {
            sender.chat("/tp " + targetID);
            return;
        }
        String serverName = server.getInfo().getName();
        staffBC(getDisplayName(sender) + " §a正在前往服务器 §7" + serverName + " §a处理对于玩家 §7" + getDisplayName(targetID) + " §a的举报");
        sender.sendMessage(new TextComponent("§7正在连接到服务器 " + serverName + "..."));
        sender.connect(server.getInfo());
        plugin.getProxy().getScheduler().schedule(plugin, () -> sender.chat("/tp " + targetID), delay, TimeUnit.MILLISECONDS);
    }

    private void Punish(ProxiedPlayer sender, String[] args) {
        if (!sender.hasPermission("reports.staff.punish")) {
            sender.sendMessage(new TextComponent("§c你没有权限使用此命令!"));
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(new TextComponent("§c用法: /reports punish <ID> <时长>"));
            return;
        }
        String targetID = args[1], timeLen = args[2];
        ProxiedPlayer targetPlayer = plugin.getProxy().getPlayer(targetID);
        if (targetPlayer == null) {
            sender.sendMessage(new TextComponent("§c该名玩家不在线或不存在!"));
            return;
        }
        staffBC(getDisplayName(sender) + "§c 因为违规举报惩罚了 " + getDisplayName(targetPlayer));
        addPunish(targetPlayer, convertToMilliseconds(sender, timeLen));
    }

    private void cancelPunish(ProxiedPlayer sender, String[] args) {
        if (!sender.hasPermission("reports.staff.punish")) {
            sender.sendMessage(new TextComponent("§c你没有权限使用此命令!"));
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(new TextComponent("§c用法: /reports CancelPunish <ID>"));
            return;
        }
        String targetID = args[1];
        ProxiedPlayer targetPlayer = plugin.getProxy().getPlayer(targetID);
        if (targetPlayer == null) {
            sender.sendMessage(new TextComponent("§c该名玩家不在线或不存在!"));
            return;
        }
        if (!isUnderPunish(targetPlayer)) {
            sender.sendMessage(new TextComponent("§c该名玩家没有被处罚!"));
            return;
        }
        staffBC(getDisplayName(sender) + "§a 取消了对 " + getDisplayName(targetPlayer) + " §a的违规举报处罚");
        removePunish(targetPlayer);
    }

    private void getStats(ProxiedPlayer sender, String[] args) {
        if (!sender.hasPermission("reports.admin.stats")) {
            sender.sendMessage(new TextComponent("§c你没有权限使用此命令!"));
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(new TextComponent("§c用法: /reports stats <ID>"));
            return;
        }
        String targetID = args[1];
        ResultSet resultSet = executeQuery("SELECT COUNT(*) AS count FROM report_info WHERE staff_name LIKE '" + targetID + "'");
        int count = 0;
        try {
            if (resultSet.next()) count = resultSet.getInt("count");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        sender.sendMessage(new TextComponent(playerPrefix + "§a工作人员 §b" + targetID + " §a至现在已处理 §e" + count + " §a个举报."));
    }

    private void clearReports(ProxiedPlayer sender, String[] args) {
        if (!sender.hasPermission("reports.admin.clear")) {
            sender.sendMessage(new TextComponent("§c你没有权限使用此命令!"));
            return;
        }
        if (args.length == 2 && Objects.equals(args[1], "confirm")) {
            int rowsAffected = executeCommand("DELETE FROM report_info");
            staffBC(getDisplayName(sender) + " §c§l清除了所有举报记录");
            sender.sendMessage(new TextComponent(playerPrefix + "§a§l清除成功 §a共清除了 §e" + rowsAffected + " §a条记录."));
        } else
            sender.sendMessage(new TextComponent(playerPrefix + "§c您确定要清除所有举报吗? 确认请输入 §b/reports clear confirm"));
    }

    private void getHelp(ProxiedPlayer sender) {
        sender.sendMessage(new TextComponent("§8--------------§e§lReports§8--------------"));
        sender.sendMessage(new TextComponent("§a/report <ID> <原因> §7举报某位玩家 §8(reports.player)"));
        sender.sendMessage(new TextComponent("§a/reports §7显示当前未处理的举报 §8(reports.staff)"));
        sender.sendMessage(new TextComponent("§a/reports accept <编号> §7受理某举报 §8(reports.staff.accept)"));
        sender.sendMessage(new TextComponent("§a/reports complete <编号> §7处理某举报 §8(reports.staff.complete)"));
        sender.sendMessage(new TextComponent("§a/reports delete <编号> §7删除某举报 §8(reports.staff.delete)"));
        sender.sendMessage(new TextComponent("§a/reports tpto <ID> §7跨服传送到某玩家 §8(reports.staff.tpto)"));
        sender.sendMessage(new TextComponent("§a/reports punish <ID> <时长> §7惩罚某玩家不得举报 §8(reports.staff.punish)"));
        sender.sendMessage(new TextComponent("§a/reports cancelpunish <ID> §7解除对某玩家的惩罚 §8(reports.staff.punish)"));
        sender.sendMessage(new TextComponent("§a/reports stats <ID> §7查看某工作人员的举报处理情况 §8(reports.admin.stats)"));
        sender.sendMessage(new TextComponent("§a/reports clear §7删除所有举报 §8(reports.admin.clear)"));
        sender.sendMessage(new TextComponent("§a/reports reload §7重载插件 §8(reports.reload)"));
        sender.sendMessage(new TextComponent("§8-----------------------------------"));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2 && (args[0].equalsIgnoreCase("tpto") || args[0].equalsIgnoreCase("punish") || args[0].equalsIgnoreCase("cancelpunish") || args[0].equalsIgnoreCase("stats")))
            return getStrings(new String[]{args[1]});
        else
            return new ArrayList<>();
    }
}
