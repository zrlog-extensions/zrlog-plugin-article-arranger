package com.zrlog.plugin.article.arranger.vo;

import com.zrlog.plugin.message.Plugin;

public class ArrangerInfoResponse {

    private boolean dark;
    private String colorPrimary;
    private Plugin plugin;
    private ArrangerConfig config;

    public boolean isDark() {
        return dark;
    }

    public void setDark(boolean dark) {
        this.dark = dark;
    }

    public String getColorPrimary() {
        return colorPrimary;
    }

    public void setColorPrimary(String colorPrimary) {
        this.colorPrimary = colorPrimary;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public ArrangerConfig getConfig() {
        return config;
    }

    public void setConfig(ArrangerConfig config) {
        this.config = config;
    }
}
