package com.zrlog.plugin.article.arranger.service;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.article.arranger.util.BeanUtils;
import com.zrlog.plugin.article.arranger.vo.ArrangeOutlineVO;
import com.zrlog.plugin.article.arranger.vo.ArticleInfo;
import com.zrlog.plugin.article.arranger.vo.WidgetDataEntry;
import com.zrlog.plugin.common.model.PublicInfo;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.type.ActionType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class ArrangerHelper {

    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    private static List<ArticleInfo> getArticles(String apiHomeUrl, IOSession session) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder().timeout(Duration.ofSeconds(30)).uri(URI.create(apiHomeUrl + "/api/article?size=50000")).build();
        HttpResponse<byte[]> send = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        if (send.statusCode() != 200) {
            return new ArrayList<>();
        }
        Map<String, Object> info = new Gson().fromJson(new String(send.body()), Map.class);
        List<Map<String, Object>> rows = (List<Map<String, Object>>) ((Map<String, Object>) info.get("data")).get("rows");
        List<ArticleInfo> articleInfos = rows.stream().map(e -> {
            String renderPluginName = (String) e.get("arrange_plugin");
            if (Objects.isNull(renderPluginName)) {
                return null;
            }
            if (!Objects.equals(renderPluginName, session.getPlugin().getShortName())) {
                return null;
            }

            return BeanUtils.convert(e, ArticleInfo.class);
        }).filter(Objects::nonNull).collect(Collectors.toList());
        Collections.reverse(articleInfos);
        return articleInfos;
    }

    private static String getLogId(String uri, List<ArticleInfo> articleInfos) {
        if (isTypePage(uri)) {
            articleInfos = articleInfos.stream().filter(e -> uri.contains(e.getTypeAlias())).collect(Collectors.toList());
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

    private static List<ArrangeOutlineVO> getItems(List<String> groups, List<ArticleInfo> articleInfos, String uri) {
        List<ArrangeOutlineVO> items = new ArrayList<>();
        if (Objects.isNull(groups) || groups.isEmpty()) {
            for (ArticleInfo e : articleInfos) {
                ArrangeOutlineVO vo = new ArrangeOutlineVO();
                vo.setUrl(e.getUrl());
                vo.setTitle((articleInfos.indexOf(e) + 1) + ". " + e.getTitle());
                //默认选中首条
                if (isTypePage(uri) && articleInfos.indexOf(e) == 0) {
                    vo.setActive(true);
                } else {
                    vo.setActive(e.getUrl().equals(uri));
                }
                items.add(vo);
            }
        }
        return items;
    }

    public static WidgetDataEntry getWidgetData(IOSession session, String uri, List<String> groups) {
        WidgetDataEntry data = new WidgetDataEntry();
        PublicInfo publicInfo = session.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
        try {
            List<ArticleInfo> articleInfos = getArticles(publicInfo.getApiHomeUrl(), session);
            String logId = getLogId(uri, articleInfos);
            if (Objects.nonNull(logId)) {
                HttpRequest detailHttpRequest = HttpRequest.newBuilder().uri(URI.create(publicInfo.getApiHomeUrl() + "/api/article/detail?id=" + logId)).build();
                HttpResponse<byte[]> detailResponse = httpClient.send(detailHttpRequest, HttpResponse.BodyHandlers.ofByteArray());
                Map<String, Object> detailInfo = new Gson().fromJson(new String(detailResponse.body()), Map.class);
                Map<String, Object> log = ((Map<String, Object>) detailInfo.get("data"));
                if (Objects.nonNull(log)) {
                    articleInfos = articleInfos.stream().filter(e -> {
                        return Objects.equals(e.getTypeAlias(), log.get("typeAlias"));
                    }).collect(Collectors.toList());
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
            data.setItems(getItems(groups, articleInfos, uri));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return data;
    }
}
