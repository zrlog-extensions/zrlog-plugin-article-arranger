package com.zrlog.plugin.article.arranger.service;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.article.arranger.util.ArrangerUtils;
import com.zrlog.plugin.article.arranger.vo.ArrangeOutlineVO;
import com.zrlog.plugin.article.arranger.vo.ArrangerConfig;
import com.zrlog.plugin.article.arranger.vo.ArrangerInfoResponse;
import com.zrlog.plugin.article.arranger.vo.ArticleCategoryGroup;
import com.zrlog.plugin.article.arranger.vo.ArticleCategoryItem;
import com.zrlog.plugin.article.arranger.vo.ArticleDetailInfo;
import com.zrlog.plugin.article.arranger.vo.ArticleDetailResponse;
import com.zrlog.plugin.article.arranger.vo.ArticleInfo;
import com.zrlog.plugin.article.arranger.vo.ArticleListData;
import com.zrlog.plugin.article.arranger.vo.ArticleListResponse;
import com.zrlog.plugin.article.arranger.vo.ArticleTypeInfo;
import com.zrlog.plugin.article.arranger.vo.PublicCacheData;
import com.zrlog.plugin.article.arranger.vo.PublicCacheResponse;
import com.zrlog.plugin.article.arranger.vo.StandardResponse;
import com.zrlog.plugin.article.arranger.vo.WebsiteConfigRequest;
import com.zrlog.plugin.article.arranger.vo.WidgetDataEntry;
import com.zrlog.plugin.client.HttpClientUtils;
import com.zrlog.plugin.common.model.PublicInfo;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.type.ActionType;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ArrangerService {

    private static final Gson GSON = new Gson();

    public ArrangerConfig parseConfig(String dataStr) {
        return normalizeConfig(GSON.fromJson(dataStr, ArrangerConfig.class));
    }

    public StandardResponse<ArrangerInfoResponse> getPageData(IOSession session, HttpRequestInfo requestInfo) {
        ArrangerConfig config = loadConfig(session);
        config.setGroups(getArticleCategoryGroups(session, config));

        ArrangerInfoResponse data = new ArrangerInfoResponse();
        data.setDark(requestInfo.isDarkMode());
        data.setColorPrimary(requestInfo.getAdminColorPrimary());
        data.setPlugin(session.getPlugin());
        data.setConfig(config);

        return StandardResponse.success(data);
    }

    public ArrangerConfig loadConfig(IOSession session) {
        return normalizeConfig(session.getResponseSync(ContentType.JSON, WebsiteConfigRequest.arrangerKeys(), ActionType.GET_WEBSITE, ArrangerConfig.class));
    }

    public WidgetDataEntry getWidgetData(IOSession session, String uri, ArrangerConfig config) {
        config = normalizeConfig(config);
        WidgetDataEntry data = new WidgetDataEntry();
        data.setStyleGlobal(config.getStyleGlobal());
        data.setMainColor(config.getMainColor());
        PublicInfo publicInfo = session.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
        try {
            Set<String> selectedTypeIds = ArrangerUtils.selectedStringSet(config.getType());
            Set<String> selectedArticleIds = ArrangerUtils.selectedStringSet(config.getItem());
            List<ArticleInfo> articleInfos = getArticles(publicInfo.getApiHomeUrl(), session);
            String typeAlias = ArrangerUtils.isTypePage(uri) ? ArrangerUtils.extractTypeAlias(uri) : "";
            boolean wholeTypeSelected = isSelectedType(articleInfos, typeAlias, selectedTypeIds);
            if (ArrangerUtils.isTypePage(uri)) {
                final String selectedTypeAlias = typeAlias;
                final boolean selectedWholeType = wholeTypeSelected;
                articleInfos = articleInfos.stream()
                        .filter(article -> Objects.equals(article.getTypeAlias(), selectedTypeAlias))
                        .filter(article -> selectedWholeType || isSelectedArticle(article, session, selectedArticleIds))
                        .collect(Collectors.toList());
            }
            String logId = getLogId(uri, articleInfos);
            if (Objects.nonNull(logId)) {
                ArticleDetailResponse detailInfo = HttpClientUtils.sendGetRequest(publicInfo.getApiHomeUrl() + "/api/article/detail?id=" + logId, ArticleDetailResponse.class, session, Duration.ofSeconds(30));
                ArticleDetailInfo log = detailInfo == null ? null : detailInfo.getData();
                if (Objects.nonNull(log)) {
                    articleInfos = articleInfos.stream().filter(e -> {
                        return Objects.equals(e.getTypeAlias(), log.getTypeAlias());
                    }).collect(Collectors.toList());
                    typeAlias = log.getTypeAlias();
                    data.setTitle(log.getTitle());
                    data.setContent(log.getContent());
                } else {
                    data.setTitle("");
                    data.setContent("");
                }
            } else {
                data.setTitle("");
                data.setContent("");
            }
            wholeTypeSelected = isSelectedType(articleInfos, typeAlias, selectedTypeIds);
            final String currentTypeAlias = typeAlias;
            final boolean currentWholeTypeSelected = wholeTypeSelected;
            articleInfos = articleInfos.stream()
                    .filter(article -> Objects.equals(article.getTypeAlias(), currentTypeAlias))
                    .filter(article -> currentWholeTypeSelected || isSelectedArticle(article, session, selectedArticleIds))
                    .collect(Collectors.toList());
            data.setItems(getItems(articleInfos, uri));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    public List<ArticleCategoryGroup> getArticleCategoryGroups(IOSession session, ArrangerConfig config) {
        PublicInfo publicInfo = session.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
        try {
            Set<String> selectedTypeIds = ArrangerUtils.selectedStringSet(config.getType());
            Set<String> selectedArticleIds = ArrangerUtils.selectedStringSet(config.getItem());
            List<ArticleInfo> articleInfos = getArticles(publicInfo.getApiHomeUrl(), session);
            Map<String, List<ArticleInfo>> articlesByTypeAlias = articleInfos.stream()
                    .filter(e -> Objects.nonNull(e.getTypeAlias()))
                    .collect(Collectors.groupingBy(ArticleInfo::getTypeAlias, LinkedHashMap::new, Collectors.toList()));
            List<ArticleCategoryGroup> groups = new ArrayList<>();
            List<ArticleTypeInfo> types = loadTypes(publicInfo.getApiHomeUrl(), session);
            if (types != null) {
                for (ArticleTypeInfo type : types) {
                    String alias = ArrangerUtils.stringValue(type.getAlias(), "");
                    ArticleCategoryGroup group = new ArticleCategoryGroup();
                    group.setId(type.getId());
                    group.setAlias(alias);
                    group.setName(ArrangerUtils.stringValue(type.getTypeName(), alias));
                    group.setTypeName(ArrangerUtils.stringValue(type.getTypeName(), alias));
                    group.setTypeUrl(ArrangerUtils.stringValue(type.getUrl(), ""));
                    group.setArrange_plugin(type.getArrange_plugin());
                    group.setSelected(isSelectedType(type, session, selectedTypeIds));
                    group.setItems(toArticleCategoryItems(articlesByTypeAlias.getOrDefault(alias, new ArrayList<>()), session, selectedArticleIds));
                    groups.add(group);
                }
            }
            if (groups.isEmpty()) {
                for (Map.Entry<String, List<ArticleInfo>> entry : articlesByTypeAlias.entrySet()) {
                    ArticleInfo first = entry.getValue().get(0);
                    ArticleCategoryGroup group = new ArticleCategoryGroup();
                    group.setId(first.getTypeId() == null ? null : first.getTypeId().intValue());
                    group.setAlias(entry.getKey());
                    group.setName(ArrangerUtils.stringValue(first.getTypeName(), entry.getKey()));
                    group.setTypeName(ArrangerUtils.stringValue(first.getTypeName(), entry.getKey()));
                    group.setTypeUrl(ArrangerUtils.stringValue(first.getTypeUrl(), ""));
                    group.setSelected(selectedTypeIds.contains(String.valueOf(first.getTypeId())));
                    group.setItems(toArticleCategoryItems(entry.getValue(), session, selectedArticleIds));
                    groups.add(group);
                }
            }
            return groups;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrangerConfig normalizeConfig(ArrangerConfig config) {
        if (config == null) {
            config = new ArrangerConfig();
        }
        config.setStyleGlobal(Objects.requireNonNullElse(config.getStyleGlobal(), ""));
        config.setMainColor(Objects.requireNonNullElse(config.getMainColor(), "#007BFF"));
        return config;
    }

    private List<ArticleInfo> getArticles(String apiHomeUrl, IOSession session) throws IOException, InterruptedException {
        ArticleListResponse response = HttpClientUtils.sendGetRequest(apiHomeUrl + "/api/article?size=50000", ArticleListResponse.class, session, Duration.ofSeconds(30));
        ArticleListData data = response == null ? null : response.getData();
        if (data == null || data.getRows() == null) {
            return new ArrayList<>();
        }
        List<ArticleInfo> articleInfos = new ArrayList<>(data.getRows());
        Collections.reverse(articleInfos);
        return articleInfos;
    }

    private PublicCacheData getPublicCache(String apiHomeUrl, IOSession session) throws IOException, InterruptedException {
        PublicCacheResponse response = HttpClientUtils.sendGetRequest(apiHomeUrl + "/api/cache", PublicCacheResponse.class, session, Duration.ofSeconds(30));
        return response == null || response.getData() == null ? new PublicCacheData() : response.getData();
    }

    private String getLogId(String uri, List<ArticleInfo> articleInfos) {
        if (ArrangerUtils.isTypePage(uri)) {
            String typeAlias = ArrangerUtils.extractTypeAlias(uri);
            articleInfos = articleInfos.stream()
                    .filter(e -> Objects.equals(typeAlias, e.getTypeAlias()))
                    .collect(Collectors.toList());
            if (!articleInfos.isEmpty()) {
                return articleInfos.get(0).getId() + "";
            }
            return null;
        }
        return uri.replace("/", "");
    }

    private boolean isSelectedArticle(ArticleInfo articleInfo, IOSession session, Set<String> selectedArticleIds) {
        return Objects.equals(articleInfo.getArrange_plugin(), session.getPlugin().getShortName())
                || selectedArticleIds.contains(String.valueOf(articleInfo.getId()));
    }

    private boolean isSelectedType(ArticleTypeInfo type, IOSession session, Set<String> selectedTypeIds) {
        return Objects.equals(type.getArrange_plugin(), session.getPlugin().getShortName())
                || selectedTypeIds.contains(String.valueOf(type.getId()));
    }

    private boolean isSelectedType(List<ArticleInfo> articleInfos, String typeAlias, Set<String> selectedTypeIds) {
        if (selectedTypeIds.isEmpty()) {
            return false;
        }
        return articleInfos.stream()
                .filter(article -> Objects.equals(typeAlias, article.getTypeAlias()))
                .map(ArticleInfo::getTypeId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .anyMatch(selectedTypeIds::contains);
    }

    private List<ArrangeOutlineVO> getItems(List<ArticleInfo> articleInfos, String uri) {
        List<ArrangeOutlineVO> items = new ArrayList<>();
        for (ArticleInfo e : articleInfos) {
            ArrangeOutlineVO vo = new ArrangeOutlineVO();
            String url = Objects.requireNonNullElse(e.getUrl(), "");
            vo.setUrl(url);
            vo.setTitle((articleInfos.indexOf(e) + 1) + ". " + Objects.requireNonNullElse(e.getTitle(), ""));
            if (ArrangerUtils.isTypePage(uri) && articleInfos.indexOf(e) == 0) {
                vo.setActive(true);
            } else {
                vo.setActive(url.replaceFirst("\\.html", "").equals(uri));
            }
            items.add(vo);
        }
        return items;
    }

    private List<ArticleTypeInfo> loadTypes(String apiHomeUrl, IOSession session) {
        try {
            PublicCacheData data = getPublicCache(apiHomeUrl, session);
            return data.getTypes() == null ? new ArrayList<>() : data.getTypes();
        } catch (IOException | InterruptedException e) {
            return new ArrayList<>();
        }
    }

    private List<ArticleCategoryItem> toArticleCategoryItems(List<ArticleInfo> articles, IOSession session, Set<String> selectedArticleIds) {
        return articles.stream().map(article -> {
            ArticleCategoryItem item = new ArticleCategoryItem();
            item.setId(article.getId());
            item.setTitle(article.getTitle());
            item.setAlias(article.getAlias());
            item.setUrl(article.getUrl());
            item.setTypeAlias(article.getTypeAlias());
            item.setArrange_plugin(article.getArrange_plugin());
            item.setSelected(isSelectedArticle(article, session, selectedArticleIds));
            return item;
        }).collect(Collectors.toList());
    }
}
