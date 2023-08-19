package com.dghdidi.reports.Operation;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import static com.dghdidi.reports.Reports.getPlayers;


public class StaffBroadCast {
    public static String staffPrefix;
    public static void staffBC(String msg) {
        for (ProxiedPlayer staff : getPlayers("reports.staff")) {
            staff.sendMessage(new TextComponent(staffPrefix + msg));
        }
    }

}
