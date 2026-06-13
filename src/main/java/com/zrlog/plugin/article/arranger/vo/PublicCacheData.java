package com.zrlog.plugin.article.arranger.vo;

import java.util.ArrayList;
import java.util.List;

public class PublicCacheData {

    private List<ArticleTypeInfo> types = new ArrayList<>();

    public List<ArticleTypeInfo> getTypes() {
        return types;
    }

    public void setTypes(List<ArticleTypeInfo> types) {
        this.types = types;
    }
}
