package com.zrlog.plugin.article.arranger.vo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Object> toRenderModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("title", title);
        model.put("content", content);
        model.put("items", items);
        model.put("styleGlobal", styleGlobal);
        model.put("mainColor", mainColor);
        return model;
    }
}
