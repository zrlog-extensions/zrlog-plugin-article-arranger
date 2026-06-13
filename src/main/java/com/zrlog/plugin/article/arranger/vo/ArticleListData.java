package com.zrlog.plugin.article.arranger.vo;

import java.util.ArrayList;
import java.util.List;

public class ArticleListData {

    private List<ArticleInfo> rows = new ArrayList<>();

    public List<ArticleInfo> getRows() {
        return rows;
    }

    public void setRows(List<ArticleInfo> rows) {
        this.rows = rows;
    }
}
