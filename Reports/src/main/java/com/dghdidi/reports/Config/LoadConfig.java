package com.dghdidi.reports.Config;

import com.dghdidi.reports.DataBase.ConnectDataBase;
import com.dghdidi.reports.Operation.ReportCMD;
import com.dghdidi.reports.Operation.StaffBroadCast;
import com.dghdidi.reports.Operation.StaffCMD;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

public class LoadConfig {
    public static boolean loadConfig(Plugin plugin) throws IOException {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.getLogger().log(Level.WARNING, "§e配置文件不存在, 已创建默认配置文件, 请自行配置参数!");
            CreateConfig.createDefaultConfig(plugin);
            return false;
        }
        Configuration config = YamlConfiguration.getProvider(YamlConfiguration.class).load(configFile);
        String host = config.getString("MySQL.host");
        String Database = config.getString("MySQL.database");
        int port = config.getInt("MySQL.port");
        String username = config.getString("MySQL.username");
        String password = config.getString("MySQL.password");

        StaffCMD.delay = config.getInt("Reports.teleportDelay(ms)");
        ReportCMD.playerPrefix = config.getString("Reports.playerPrefix");
        StaffBroadCast.staffPrefix = config.getString("Reports.staffPrefix");

        try {
            ConnectDataBase.connectSQL(host, Database, port, username, password, plugin);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "§c连接数据库失败, 请检查配置文件!");
            return false;
        }
        return true;
    }
}
