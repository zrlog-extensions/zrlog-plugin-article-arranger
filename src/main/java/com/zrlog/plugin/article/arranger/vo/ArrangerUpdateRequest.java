package com.zrlog.plugin.article.arranger.vo;

import java.util.Objects;

public class ArrangerUpdateRequest {

    private String styleGlobal;
    private String mainColor;
    private Object type;
    private Object item;

    public static ArrangerUpdateRequest of(String styleGlobal, String mainColor, Object type, Object item) {
        ArrangerUpdateRequest request = new ArrangerUpdateRequest();
        request.setStyleGlobal(Objects.toString(styleGlobal, ""));
        request.setMainColor(Objects.toString(mainColor, "#007BFF"));
        request.setType(type);
        request.setItem(item);
        return request;
    }

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

    public Object getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = type;
    }

    public Object getItem() {
        return item;
    }

    public void setItem(Object item) {
        this.item = item;
    }
}
