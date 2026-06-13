package com.zrlog.plugin.article.arranger.service;

import com.google.gson.JsonElement;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.article.arranger.vo.ArrangeOutlineVO;
import com.zrlog.plugin.article.arranger.vo.ArrangerConfig;
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
import com.zrlog.plugin.article.arranger.vo.WidgetDataEntry;
import com.zrlog.plugin.client.HttpClientUtils;
import com.zrlog.plugin.common.model.PublicInfo;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.type.ActionType;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class ArrangerHelper {

    private static List<ArticleInfo> getArticles(String apiHomeUrl, IOSession session) throws IOException, InterruptedException {
        ArticleListResponse response = HttpClientUtils.sendGetRequest(apiHomeUrl + "/api/article?size=50000", ArticleListResponse.class, session, Duration.ofSeconds(30));
        ArticleListData data = response == null ? null : response.getData();
        if (data == null || data.getRows() == null) {
            return new ArrayList<>();
        }
        List<ArticleInfo> articleInfos = new ArrayList<>(data.getRows());
        Collections.reverse(articleInfos);
        return articleInfos;
    }

    private static PublicCacheData getPublicCache(String apiHomeUrl, IOSession session) throws IOException, InterruptedException {
        PublicCacheResponse response = HttpClientUtils.sendGetRequest(apiHomeUrl + "/api/cache", PublicCacheResponse.class, session, Duration.ofSeconds(30));
        return response == null || response.getData() == null ? new PublicCacheData() : response.getData();
    }

    private static String getLogId(String uri, List<ArticleInfo> articleInfos) {
        if (isTypePage(uri)) {
            String typeAlias = extractTypeAlias(uri);
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

    private static boolean isTypePage(String uri) {
        return uri.contains("sort/");
    }

    private static Set<String> selectedStringSet(Object value) {
        Set<String> result = new HashSet<>();
        if (Objects.isNull(value)) {
            return result;
        }
        if (value instanceof JsonElement) {
            JsonElement element = (JsonElement) value;
            if (element.isJsonNull()) {
                return result;
            }
            if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(item -> {
                    if (!item.isJsonNull()) {
                        result.add(idString(item.getAsString()));
                    }
                });
                return result;
            }
            if (element.isJsonPrimitive()) {
                result.add(idString(element.getAsString()));
                return result;
            }
        }
        if (value instanceof Collection) {
            ((Collection<?>) value).stream().filter(Objects::nonNull).map(ArrangerHelper::idString).forEach(result::add);
            return result;
        }
        if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            Arrays.stream(array).filter(Objects::nonNull).map(ArrangerHelper::idString).forEach(result::add);
            return result;
        }
        result.add(idString(value));
        return result;
    }

    private static String idString(Object value) {
        if (value instanceof Number) {
            return String.valueOf(((Number) value).intValue());
        }
        String raw = String.valueOf(value);
        try {
            double number = Double.parseDouble(raw);
            if (number == Math.rint(number)) {
                return String.valueOf((int) number);
            }
        } catch (NumberFormatException ignored) {
            return raw;
        }
        return raw;
    }

    private static String stringValue(Object value, String fallback) {
        if (Objects.isNull(value)) {
            return fallback;
        }
        String str = String.valueOf(value);
        return str.trim().isEmpty() ? fallback : str;
    }

    private static boolean isSelectedArticle(ArticleInfo articleInfo, IOSession session, Set<String> selectedArticleIds) {
        return Objects.equals(articleInfo.getArrange_plugin(), session.getPlugin().getShortName())
                || selectedArticleIds.contains(String.valueOf(articleInfo.getId()));
    }

    private static boolean isSelectedType(ArticleTypeInfo type, IOSession session, Set<String> selectedTypeIds) {
        return Objects.equals(type.getArrange_plugin(), session.getPlugin().getShortName())
                || selectedTypeIds.contains(String.valueOf(type.getId()));
    }

    private static boolean isSelectedType(List<ArticleInfo> articleInfos, String typeAlias, Set<String> selectedTypeIds) {
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

    private static List<ArrangeOutlineVO> getItems(List<ArticleInfo> articleInfos, String uri) {
        List<ArrangeOutlineVO> items = new ArrayList<>();
        for (ArticleInfo e : articleInfos) {
            ArrangeOutlineVO vo = new ArrangeOutlineVO();
            String url = Objects.requireNonNullElse(e.getUrl(), "");
            vo.setUrl(url);
            vo.setTitle((articleInfos.indexOf(e) + 1) + ". " + Objects.requireNonNullElse(e.getTitle(), ""));
            if (isTypePage(uri) && articleInfos.indexOf(e) == 0) {
                vo.setActive(true);
            } else {
                vo.setActive(url.replaceFirst("\\.html", "").equals(uri));
            }
            items.add(vo);
        }
        return items;
    }

    public static List<ArticleCategoryGroup> getArticleCategoryGroups(IOSession session, ArrangerConfig config) {
        PublicInfo publicInfo = session.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
        try {
            Set<String> selectedTypeIds = selectedStringSet(config.getType());
            Set<String> selectedArticleIds = selectedStringSet(config.getItem());
            List<ArticleInfo> articleInfos = getArticles(publicInfo.getApiHomeUrl(), session);
            Map<String, List<ArticleInfo>> articlesByTypeAlias = articleInfos.stream()
                    .filter(e -> Objects.nonNull(e.getTypeAlias()))
                    .collect(Collectors.groupingBy(ArticleInfo::getTypeAlias, LinkedHashMap::new, Collectors.toList()));
            List<ArticleCategoryGroup> groups = new ArrayList<>();
            List<ArticleTypeInfo> types = loadTypes(publicInfo.getApiHomeUrl(), session);
            if (types != null) {
                for (ArticleTypeInfo type : types) {
                    String alias = stringValue(type.getAlias(), "");
                    ArticleCategoryGroup group = new ArticleCategoryGroup();
                    group.setId(type.getId());
                    group.setAlias(alias);
                    group.setName(stringValue(type.getTypeName(), alias));
                    group.setTypeName(stringValue(type.getTypeName(), alias));
                    group.setTypeUrl(stringValue(type.getUrl(), ""));
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
                    group.setName(stringValue(first.getTypeName(), entry.getKey()));
                    group.setTypeName(stringValue(first.getTypeName(), entry.getKey()));
                    group.setTypeUrl(stringValue(first.getTypeUrl(), ""));
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

    private static List<ArticleTypeInfo> loadTypes(String apiHomeUrl, IOSession session) {
        try {
            PublicCacheData data = getPublicCache(apiHomeUrl, session);
            return data.getTypes() == null ? new ArrayList<>() : data.getTypes();
        } catch (IOException | InterruptedException e) {
            return new ArrayList<>();
        }
    }

    private static List<ArticleCategoryItem> toArticleCategoryItems(List<ArticleInfo> articles, IOSession session, Set<String> selectedArticleIds) {
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

    public static WidgetDataEntry getWidgetData(IOSession session, String uri, ArrangerConfig config) {
        WidgetDataEntry data = new WidgetDataEntry();
        PublicInfo publicInfo = session.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
        try {
            Set<String> selectedTypeIds = selectedStringSet(config.getType());
            Set<String> selectedArticleIds = selectedStringSet(config.getItem());
            List<ArticleInfo> articleInfos = getArticles(publicInfo.getApiHomeUrl(), session);
            String typeAlias = isTypePage(uri) ? extractTypeAlias(uri) : "";
            boolean wholeTypeSelected = isSelectedType(articleInfos, typeAlias, selectedTypeIds);
            if (isTypePage(uri)) {
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

    private static String extractTypeAlias(String uri) {
        String value = uri;
        int index = value.indexOf("sort/");
        if (index >= 0) {
            value = value.substring(index + "sort/".length());
        }
        return value.replace("/", "").replace(".html", "");
    }
}
