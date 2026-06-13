package com.zrlog.plugin.article.arranger.vo;

public class ArticleInfo {
    private String title;
    private String alias;
    private String typeAlias;
    private String typeName;
    private Long typeId;
    private String url;
    private String typeUrl;
    private String arrange_plugin;
    public Integer id;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getTypeAlias() {
        return typeAlias;
    }

    public void setTypeAlias(String typeAlias) {
        this.typeAlias = typeAlias;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
