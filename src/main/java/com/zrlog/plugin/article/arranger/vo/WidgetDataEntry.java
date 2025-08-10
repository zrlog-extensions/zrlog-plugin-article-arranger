package com.zrlog.plugin.article.arranger.vo;

import java.util.List;

public class WidgetDataEntry {

    private String title;
    private String content;
    private List<ArrangeOutlineVO> items;
    private String styleGlobal;
    private String mainColor;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<ArrangeOutlineVO> getItems() {
        return items;
    }

    public void setItems(List<ArrangeOutlineVO> items) {
        this.items = items;
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
}
