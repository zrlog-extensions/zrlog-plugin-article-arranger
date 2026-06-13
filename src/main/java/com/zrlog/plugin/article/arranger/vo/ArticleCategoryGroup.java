package com.zrlog.plugin.article.arranger.vo;

import java.util.ArrayList;
import java.util.List;

public class ArticleCategoryGroup {

    private Integer id;
    private String alias;
    private String name;
    private String typeName;
    private String typeUrl;
    private String arrange_plugin;
    private boolean selected;
    private List<ArticleCategoryItem> items = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeUrl() {
        return typeUrl;
    }

    public void setTypeUrl(String typeUrl) {
        this.typeUrl = typeUrl;
    }

    public String getArrange_plugin() {
        return arrange_plugin;
    }

    public void setArrange_plugin(String arrange_plugin) {
        this.arrange_plugin = arrange_plugin;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public List<ArticleCategoryItem> getItems() {
        return items;
    }

    public void setItems(List<ArticleCategoryItem> items) {
        this.items = items;
    }
}
