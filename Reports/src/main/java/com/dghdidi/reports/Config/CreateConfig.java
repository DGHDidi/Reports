package com.dghdidi.reports.Config;

import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;


public class CreateConfig {
    public static void createDefaultConfig(Plugin plugin) {
        File pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            if (pluginFolder.mkdir()) {
                plugin.getLogger().log(Level.INFO, "§a成功创建插件文件夹!");
            } else {
                plugin.getLogger().log(Level.WARNING, "§c无法创建插件文件夹!");
                return;
            }
        }
        File configFile = new File(pluginFolder, "config.yml");
        if (!configFile.exists()) {
            try {
                InputStream inputStream = plugin.getResourceAsStream("defaultConfig.yml");
                Files.copy(inputStream, configFile.toPath());
                plugin.getLogger().log(Level.INFO, "§e配置文件不存在, 已创建默认配置文件, 请自行配置参数!");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}