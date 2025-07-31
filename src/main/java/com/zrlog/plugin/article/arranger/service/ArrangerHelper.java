package com.zrlog.plugin.article.arranger.service;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.article.arranger.vo.ArrangeOutlineVO;
import com.zrlog.plugin.article.arranger.vo.ArticleInfo;
import com.zrlog.plugin.common.model.PublicInfo;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.type.ActionType;
import com.zrlog.plugin.type.RunType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class ArrangerHelper {

    public static Map<String, Object> getWidgetData(IOSession session, String uri, boolean staticHtml, List<String> groups) {
        Map<String, Object> data = new HashMap<>();

        PublicInfo publicInfo = session.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
        List<ArticleInfo> articleInfos;
        try (HttpClient httpClient = HttpClient.newBuilder().build()) {
            HttpRequest httpRequest = HttpRequest.newBuilder().timeout(Duration.ofSeconds(30)).uri(URI.create(publicInfo.getApiHomeUrl() + "/api/article?size=50000")).build();
            HttpResponse<byte[]> send;
            send = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            Map<String, Object> info = new Gson().fromJson(new String(send.body()), Map.class);
            List<Map<String, Object>> rows = (List<Map<String, Object>>) ((Map<String, Object>) info.get("data")).get("rows");
            articleInfos = rows.stream().map(e -> {
                String renderPluginName = (String) e.get("arrange_plugin");
                if (Objects.isNull(renderPluginName)) {
                    return null;
                }
                if (!Objects.equals(renderPluginName, session.getPlugin().getShortName())) {
                    return null;
                }

                ArticleInfo articleInfo = new ArticleInfo();
                articleInfo.setTitle((String) e.get("title"));
                articleInfo.setUrl((String) e.get("url"));
                articleInfo.setAlias((String) e.get("alias"));
                articleInfo.setTypeAlias((String) e.get("typeAlias"));
                return articleInfo;
            }).filter(Objects::nonNull).collect(Collectors.toList()).reversed();

            Collections.reverse(articleInfos);
            String logId = null;
            if (uri.contains("sort/")) {
                articleInfos = articleInfos.stream().filter(e -> uri.contains(e.getTypeAlias())).collect(Collectors.toList());
                if (!articleInfos.isEmpty()) {
                    logId = articleInfos.get(0).getAlias();
                }
            } else {
                logId = uri.replace("/", "");
            }
            if (Objects.nonNull(logId)) {
                try {
                    HttpRequest detailHttpRequest = HttpRequest.newBuilder().uri(URI.create(publicInfo.getApiHomeUrl() + "/api/article/detail?id=" + logId)).build();
                    HttpResponse<byte[]> detailResponse = httpClient.send(detailHttpRequest, HttpResponse.BodyHandlers.ofByteArray());
                    Map<String, Object> detailInfo = new Gson().fromJson(new String(detailResponse.body()), Map.class);
                    Map<String, Object> log = ((Map<String, Object>) detailInfo.get("data"));
                    if (Objects.nonNull(log)) {
                        articleInfos = articleInfos.stream().filter(e -> {
                            return Objects.equals(e.getTypeAlias(), log.get("typeAlias"));
                        }).collect(Collectors.toList());
                        data.put("title", log.get("title"));
                        data.put("content", log.get("content"));
                    } else {
                        data.put("title", "");
                        data.put("content", "");
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                data.put("title", "");
                data.put("content", "");
            }
            if (Objects.isNull(groups) || groups.isEmpty()) {
                List<ArrangeOutlineVO> items = new ArrayList<>();
                List<ArticleInfo> finalArticleInfos = articleInfos;
                articleInfos.forEach(e -> {
                    ArrangeOutlineVO vo = new ArrangeOutlineVO();
                    vo.setUrl(RunConstants.runType == RunType.DEV ? e.getAlias() + (staticHtml ? ".html" : "") : e.getUrl());
                    vo.setTitle((finalArticleInfos.indexOf(e) + 1) + ". " + e.getTitle());
                    //默认选中首条
                    if (uri.contains("sort/") && finalArticleInfos.indexOf(e) == 0) {
                        vo.setActive(true);
                    } else {
                        vo.setActive(e.getAlias().equals(uri.replace("/", "")));
                    }
                    items.add(vo);
                });
                data.put("items", items);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return data;
    }
}
