package com.zrlog.plugin.article.arranger.vo;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class ArrangerConfig {

    private String styleGlobal;
    private String mainColor;
    private JsonElement type;
    private JsonElement item;
    private List<ArticleCategoryGroup> groups = new ArrayList<>();

    public String getStyleGlobal() {
        return styleGlobal;
    }

    public void setStyleGlobal(String styleGlobal) {
        this.styleGlobal = styleGlobal;
    }

    public String getMainColor() {
        return mainColor;
    }

    public void setMainColor(String mainColor) {
        this.mainColor = mainColor;
    }

    public JsonElement getType() {
        return type;
    }

    public void setType(JsonElement type) {
        this.type = type;
    }

    public JsonElement getItem() {
        return item;
    }

    public void setItem(JsonElement item) {
        this.item = item;
    }

    public List<ArticleCategoryGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<ArticleCategoryGroup> groups) {
        this.groups = groups;
    }
}
