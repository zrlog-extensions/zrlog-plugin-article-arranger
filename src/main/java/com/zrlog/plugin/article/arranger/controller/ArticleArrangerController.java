package com.zrlog.plugin.article.arranger.controller;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.article.arranger.vo.ArrangeOutlineVO;
import com.zrlog.plugin.article.arranger.vo.ArticleInfo;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.common.model.PublicInfo;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.type.ActionType;
import com.zrlog.plugin.type.RunType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

public class ArticleArrangerController {


    private final IOSession session;
    private final MsgPacket requestPacket;
    private final HttpRequestInfo requestInfo;

    public ArticleArrangerController(IOSession session, MsgPacket requestPacket, HttpRequestInfo requestInfo) {
        this.session = session;
        this.requestPacket = requestPacket;
        this.requestInfo = requestInfo;
    }

    public void update() {
        if(Objects.isNull(requestInfo.getUserId()) || requestInfo.getUserId() <= 0){
            session.responseHtml("/templates/403.ftl",new HashMap<>(), requestPacket.getMethodStr(), requestPacket.getMsgId());
            return;
        }
        session.sendMsg(new MsgPacket(requestInfo.simpleParam(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(), ActionType.SET_WEBSITE.name()), msgPacket -> {
            Map<String, Object> map = new HashMap<>();
            map.put("success", true);
            session.sendMsg(new MsgPacket(map, ContentType.JSON, MsgPacketStatus.RESPONSE_SUCCESS, requestPacket.getMsgId(), requestPacket.getMethodStr()));
            //更新缓存
            session.sendJsonMsg(new HashMap<>(), ActionType.REFRESH_CACHE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST);
        });
    }

    public void widget() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "styleGlobal,groups");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            Map map = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
            Map<String, Object> data = new HashMap<>();
            String realUri = requestInfo.getUri().replace(".action","").replace(".html","");
            PublicInfo publicInfo = session.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
            List<ArticleInfo> articleInfos;
            HttpClient httpClient = HttpClient.newBuilder().build();
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(publicInfo.getApiHomeUrl() + "/api/article?size=50000")).build();
                HttpResponse<byte[]> send;
                send = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
                Map<String, Object> info = new Gson().fromJson(new String(send.body()), Map.class);
                List<Map<String,Object>> rows = (List<Map<String,Object>>) ((Map<String,Object>) info.get("data")).get("rows");
                articleInfos= rows.stream().map(e -> {
                    String renderPluginName = (String) e.get("arrange_plugin");
                    if(Objects.isNull(renderPluginName)){
                        return null;
                    }
                    if(!Objects.equals(renderPluginName,session.getPlugin().getShortName())){
                        return null;
                    }

                    ArticleInfo articleInfo = new ArticleInfo();
                    articleInfo.setTitle((String) e.get("title"));
                    articleInfo.setUrl((String) e.get("url"));
                    articleInfo.setAlias((String) e.get("alias"));
                    articleInfo.setTypeAlias((String)e.get("typeAlias"));
                    return articleInfo;
                }).filter(Objects::nonNull).collect(Collectors.toList());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            String logId = null;
            if(requestInfo.getUri().startsWith("/sort")){
                articleInfos = articleInfos.stream().filter(e -> realUri.contains(e.getTypeAlias())).collect(Collectors.toList());
                if(!articleInfos.isEmpty()){
                    logId = articleInfos.get(0).getAlias();
                }

            } else {
                logId = realUri.replace("/","");
            }
            if(Objects.nonNull(logId)) {
                HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(publicInfo.getApiHomeUrl() + "/api/article/detail?id=" + logId)).build();
                HttpResponse<byte[]> send;
                try {
                    send = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
                    Map<String, Object> info = new Gson().fromJson(new String(send.body()), Map.class);
                    Map<String, Object> log = ((Map<String, Object>) info.get("data"));
                    if(Objects.nonNull(log)) {
                        articleInfos = articleInfos.stream().filter(e -> {
                            return Objects.equals(e.getTypeAlias(), log.get("typeAlias"));
                        }).collect(Collectors.toList());
                        data.put("title", log.get("title"));
                        data.put("content", log.get("content"));
                    }else {
                        data.put("title", "");
                        data.put("content", "");
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }else {
                data.put("title", "");
                data.put("content","");
            }
            if (Objects.isNull(map.get("groups"))) {
                List<ArrangeOutlineVO> items = new ArrayList<>();
                boolean staticHtml = requestInfo.getUri().endsWith(".html");
                articleInfos.forEach(e -> {
                    ArrangeOutlineVO vo = new ArrangeOutlineVO();
                    vo.setUrl(RunConstants.runType == RunType.DEV ? e.getAlias() + (staticHtml ? ".html" :"") : e.getUrl());
                    vo.setTitle(e.getTitle());
                    vo.setActive(e.getAlias().replace(".html","").equals(realUri.replace("/","")));
                    items.add(vo);
                });
                data.put("items", items);
            }
            data.put("styleGlobal",Objects.requireNonNullElse(map.get("styleGlobal"),""));
            session.responseHtml("/templates/widget.ftl",data, requestPacket.getMethodStr(), requestPacket.getMsgId());
        });
    }

    public void index() {
        if(Objects.isNull(requestInfo.getUserId()) || requestInfo.getUserId() <= 0){
            session.responseHtml("/templates/403.ftl",new HashMap<>(), requestPacket.getMethodStr(), requestPacket.getMsgId());
            return;
        }
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "styleGlobal,groups");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            Map map = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
            Map<String, Object> data = new HashMap<>();
            data.put("theme", Objects.equals(requestInfo.getHeader().get("Dark-Mode"), "true") ? "dark" : "light");
            if (Objects.isNull(map.get("groups"))) {
                data.put("groups", new ArrayList<>());
            }
            data.put("styleGlobal",Objects.requireNonNullElse(map.get("styleGlobal"),""));
            session.responseHtml("/templates/index.ftl",data, requestPacket.getMethodStr(), requestPacket.getMsgId());
        });
    }
}