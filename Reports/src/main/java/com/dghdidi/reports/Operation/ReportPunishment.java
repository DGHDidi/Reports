package com.dghdidi.reports.Operation;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dghdidi.reports.Operation.ReportCMD.playerPrefix;

public class ReportPunishment {
    private static final Map<ProxiedPlayer, Long> map = new HashMap<>();

    public static void addPunish(ProxiedPlayer player, long timeStamp) {
        map.put(player, timeStamp + System.currentTimeMillis());
        player.sendMessage(new TextComponent(playerPrefix + "§c您因为违规举报被工作人员处罚, 在 " + formatTime(timeStamp) + "§c后才能进行举报)"));
    }

    public static void removePunish(ProxiedPlayer player) {
        map.remove(player);
        player.sendMessage(new TextComponent(playerPrefix + "§a对您违规举报的处罚已被工作人员撤销, 很抱歉给您带来的不便!"));
    }

    public static boolean isUnderPunish(ProxiedPlayer player) {
        if (!map.containsKey(player)) return false;
        long time = map.get(player);
        if (time <= System.currentTimeMillis()) {
            map.remove(player);
            return false;
        }
        return true;
    }

    public static String getRemainTime(ProxiedPlayer player) {
        long end = map.get(player), del = end - System.currentTimeMillis();
        return formatTime(del);
    }

    public static long convertToMilliseconds(ProxiedPlayer sender, String input) {

        Pattern pattern = Pattern.compile("(\\d+)([a-zA-Z]+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit.toLowerCase()) {
                case "d":
                    return Duration.ofDays(value).toMillis();
                case "h":
                    return Duration.ofHours(value).toMillis();
                case "min":
                    return Duration.ofMinutes(value).toMillis();
                case "s":
                    return Duration.ofSeconds(value).toMillis();
                default: {
                    sender.sendMessage(new TextComponent("§c您输入的时间格式有误!"));
                    throw new IllegalArgumentException("无法识别的时间单位");
                }
            }
        } else {
            sender.sendMessage(new TextComponent("§c您输入的时间格式有误!"));
            throw new IllegalArgumentException("无效的时间字符串");
        }
    }

    public static String formatTime(long milliseconds) {
        Duration duration = Duration.ofMillis(milliseconds);
        long days = duration.toDays(), hours = duration.toHours() % 24, minutes = duration.toMinutes() % 60;
        String result = "";
        if (days != 0) result += days + " 天 ";
        if (hours != 0 || days != 0) result += hours + " 时 ";
        return result + minutes + " 分 ";
    }
}
