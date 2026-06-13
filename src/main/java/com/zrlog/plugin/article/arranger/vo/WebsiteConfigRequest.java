package com.zrlog.plugin.article.arranger.vo;

public class WebsiteConfigRequest {

    private String key;

    public static WebsiteConfigRequest arrangerKeys() {
        WebsiteConfigRequest request = new WebsiteConfigRequest();
        request.setKey("styleGlobal,groups,mainColor,type,item");
        return request;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
