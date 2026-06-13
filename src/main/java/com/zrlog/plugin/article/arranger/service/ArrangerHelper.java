package com.zrlog.plugin.article.arranger.service;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.article.arranger.util.BeanUtils;
import com.zrlog.plugin.article.arranger.vo.ArrangeOutlineVO;
import com.zrlog.plugin.article.arranger.vo.ArticleInfo;
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
        Map info = HttpClientUtils.sendGetRequest(apiHomeUrl + "/api/article?size=50000", Map.class, session, Duration.ofSeconds(30));
        Object data = info.get("data");
        if (!(data instanceof Map)) {
            return new ArrayList<>();
        }
        Object rawRows = ((Map<String, Object>) data).get("rows");
        if (!(rawRows instanceof Collection)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> rows = ((Collection<?>) rawRows).stream()
                .filter(row -> row instanceof Map)
                .map(row -> (Map<String, Object>) row)
                .collect(Collectors.toList());
        List<ArticleInfo> articleInfos = rows.stream().map(e -> BeanUtils.convert(e, ArticleInfo.class)).collect(Collectors.toList());
        Collections.reverse(articleInfos);
        return articleInfos;
    }

    private static Map<String, Object> getPublicCache(String apiHomeUrl, IOSession session) throws IOException, InterruptedException {
        Map info = HttpClientUtils.sendGetRequest(apiHomeUrl + "/api/cache", Map.class, session, Duration.ofSeconds(30));
        Object data = info.get("data");
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        return new HashMap<>();
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

    private static Integer intValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (Objects.nonNull(value)) {
            try {
                return (int) Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static boolean isSelectedArticle(ArticleInfo articleInfo, IOSession session, Set<String> selectedArticleIds) {
        return Objects.equals(articleInfo.getArrange_plugin(), session.getPlugin().getShortName())
                || selectedArticleIds.contains(String.valueOf(articleInfo.getId()));
    }

    private static boolean isSelectedType(Map<String, Object> type, IOSession session, Set<String> selectedTypeIds) {
        return Objects.equals(type.get("arrange_plugin"), session.getPlugin().getShortName())
                || selectedTypeIds.contains(String.valueOf(intValue(type.get("id"))));
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

    public static List<Map<String, Object>> getAdminGroups(IOSession session, Map<String, Object> config) {
        PublicInfo publicInfo = session.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
        try {
            Set<String> selectedTypeIds = selectedStringSet(config.get("type"));
            Set<String> selectedArticleIds = selectedStringSet(config.get("item"));
            List<ArticleInfo> articleInfos = getArticles(publicInfo.getApiHomeUrl(), session);
            Map<String, List<ArticleInfo>> articlesByTypeAlias = articleInfos.stream()
                    .filter(e -> Objects.nonNull(e.getTypeAlias()))
                    .collect(Collectors.groupingBy(ArticleInfo::getTypeAlias, LinkedHashMap::new, Collectors.toList()));
            List<Map<String, Object>> groups = new ArrayList<>();
            Object rawTypes = loadTypes(publicInfo.getApiHomeUrl(), session);
            if (rawTypes instanceof Collection) {
                for (Object rawType : (Collection<?>) rawTypes) {
                    if (!(rawType instanceof Map)) {
                        continue;
                    }
                    Map<String, Object> type = (Map<String, Object>) rawType;
                    String alias = stringValue(type.get("alias"), "");
                    Map<String, Object> group = new LinkedHashMap<>();
                    group.put("id", intValue(type.get("id")));
                    group.put("alias", alias);
                    group.put("name", stringValue(type.get("typeName"), alias));
                    group.put("typeName", stringValue(type.get("typeName"), alias));
                    group.put("typeUrl", stringValue(type.get("url"), ""));
                    group.put("arrange_plugin", type.get("arrange_plugin"));
                    group.put("selected", isSelectedType(type, session, selectedTypeIds));
                    group.put("items", toAdminItems(articlesByTypeAlias.getOrDefault(alias, new ArrayList<>()), session, selectedArticleIds));
                    groups.add(group);
                }
            }
            if (groups.isEmpty()) {
                for (Map.Entry<String, List<ArticleInfo>> entry : articlesByTypeAlias.entrySet()) {
                    ArticleInfo first = entry.getValue().get(0);
                    Map<String, Object> group = new LinkedHashMap<>();
                    group.put("id", first.getTypeId());
                    group.put("alias", entry.getKey());
                    group.put("name", stringValue(first.getTypeName(), entry.getKey()));
                    group.put("typeName", stringValue(first.getTypeName(), entry.getKey()));
                    group.put("typeUrl", stringValue(first.getTypeUrl(), ""));
                    group.put("selected", selectedTypeIds.contains(String.valueOf(first.getTypeId())));
                    group.put("items", toAdminItems(entry.getValue(), session, selectedArticleIds));
                    groups.add(group);
                }
            }
            return groups;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object loadTypes(String apiHomeUrl, IOSession session) {
        try {
            return getPublicCache(apiHomeUrl, session).get("types");
        } catch (IOException | InterruptedException e) {
            return new ArrayList<>();
        }
    }

    private static List<Map<String, Object>> toAdminItems(List<ArticleInfo> articles, IOSession session, Set<String> selectedArticleIds) {
        return articles.stream().map(article -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", article.getId());
            item.put("title", article.getTitle());
            item.put("alias", article.getAlias());
            item.put("url", article.getUrl());
            item.put("typeAlias", article.getTypeAlias());
            item.put("arrange_plugin", article.getArrange_plugin());
            item.put("selected", isSelectedArticle(article, session, selectedArticleIds));
            return item;
        }).collect(Collectors.toList());
    }

    public static WidgetDataEntry getWidgetData(IOSession session, String uri, Map<String, Object> config) {
        WidgetDataEntry data = new WidgetDataEntry();
        PublicInfo publicInfo = session.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
        try {
            Set<String> selectedTypeIds = selectedStringSet(config.get("type"));
            Set<String> selectedArticleIds = selectedStringSet(config.get("item"));
            List<ArticleInfo> articleInfos = getArticles(publicInfo.getApiHomeUrl(), session);
            String typeAlias = isTypePage(uri) ? extractTypeAlias(uri) : "";
            boolean wholeTypeSelected = isSelectedType(articleInfos, typeAlias, selectedTypeIds);
            if (isTypePage(uri)) {
                final String selectedTypeAlias = typeAlias;
                final boolean selectedWholeType = wholeTypeSelected;
                List<ArticleInfo> selectedArticles = articleInfos.stream()
                        .filter(article -> Objects.equals(article.getTypeAlias(), selectedTypeAlias))
                        .filter(article -> selectedWholeType || isSelectedArticle(article, session, selectedArticleIds))
                        .collect(Collectors.toList());
                articleInfos = selectedArticles;
            }
            String logId = getLogId(uri, articleInfos);
            if (Objects.nonNull(logId)) {
                Map detailInfo = HttpClientUtils.sendGetRequest(publicInfo.getApiHomeUrl() + "/api/article/detail?id=" + logId, Map.class, session, Duration.ofSeconds(30));
                Map<String, Object> log = ((Map<String, Object>) detailInfo.get("data"));
                if (Objects.nonNull(log)) {
                    articleInfos = articleInfos.stream().filter(e -> {
                        return Objects.equals(e.getTypeAlias(), log.get("typeAlias"));
                    }).collect(Collectors.toList());
                    typeAlias = String.valueOf(log.get("typeAlias"));
                    data.setTitle((String) log.get("title"));
                    data.setContent((String) log.get("content"));
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
