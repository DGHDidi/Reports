package com.dghdidi.reports.DataBase;

import net.md_5.bungee.api.plugin.Plugin;

import java.sql.*;
import java.util.logging.Level;

import static com.dghdidi.reports.Reports.connection;

public class ConnectDataBase {
    public static void connectSQL(String host, String Database, int port, String username, String password, Plugin plugin) throws SQLException {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
            String url = "jdbc:mysql://" + host + ":" + port + "/" + Database + "?useSSL=false";
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().log(Level.INFO, "§a成功连接至数据库!");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "§c连接数据库失败, 请检查配置文件!");
            return;
        }
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getTables(null, null, "report_info", null);
        if (!resultSet.next()) {
            executeCommand("CREATE TABLE report_info (" +
                    "    `index` INT AUTO_INCREMENT PRIMARY KEY," +
                    "    player_name VARCHAR(64)," +
                    "    reported_name VARCHAR(64)," +
                    "    staff_name VARCHAR(64)," +
                    "    server_name VARCHAR(64)," +
                    "    reason VARCHAR(128)" +
                    ")");
        }
    }

    public static int executeCommand(String sql) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ResultSet executeQuery(String sql) {
        Statement statement;
        try {
            statement = connection.createStatement();
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
