package com.zrlog.plugin.article.arranger.controller;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.article.arranger.service.ArrangerService;
import com.zrlog.plugin.article.arranger.util.ArrangerUtils;
import com.zrlog.plugin.article.arranger.vo.ArrangerConfig;
import com.zrlog.plugin.article.arranger.vo.ArrangerUpdateRequest;
import com.zrlog.plugin.article.arranger.vo.StandardResponse;
import com.zrlog.plugin.article.arranger.vo.WebsiteConfigRequest;
import com.zrlog.plugin.article.arranger.vo.WidgetDataEntry;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.render.FreeMarkerRenderHandler;
import com.zrlog.plugin.type.ActionType;
import com.zrlog.plugin.type.RunType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArticleArrangerController {

    private static final Logger LOGGER = LoggerUtil.getLogger(ArticleArrangerController.class);
    private final ArrangerService arrangerService = new ArrangerService();

    private final IOSession session;
    private final MsgPacket requestPacket;
    private final HttpRequestInfo requestInfo;

    public ArticleArrangerController(IOSession session, MsgPacket requestPacket, HttpRequestInfo requestInfo) {
        this.session = session;
        this.requestPacket = requestPacket;
        this.requestInfo = requestInfo;
    }

    public void update() {
        if (Objects.isNull(requestInfo.getUserId()) || requestInfo.getUserId() <= 0) {
            session.responseHtml("/templates/403", new HashMap<>(), requestPacket.getMethodStr(), requestPacket.getMsgId());
            return;
        }
        ArrangerUpdateRequest request = ArrangerUpdateRequest.of(paramValue("styleGlobal"), paramValue("mainColor"),
                paramObject("type"), paramObject("item"));
        session.sendMsg(new MsgPacket(request, ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(), ActionType.SET_WEBSITE.name()), msgPacket -> {
            session.sendMsg(new MsgPacket(StandardResponse.success(Boolean.TRUE), ContentType.JSON, MsgPacketStatus.RESPONSE_SUCCESS, requestPacket.getMsgId(), requestPacket.getMethodStr()));
            //更新缓存
            session.sendJsonMsg(new HashMap<>(), ActionType.REFRESH_CACHE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST);
        });
    }


    public void widget() {
        session.sendJsonMsg(WebsiteConfigRequest.arrangerKeys(), ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            ArrangerConfig config = arrangerService.parseConfig(msgPacket.getDataStr());
            try {
                String realUri = ArrangerUtils.toActionUri(requestInfo.getUri());
                WidgetDataEntry data = arrangerService.getWidgetData(session, realUri, config);
                String render = new FreeMarkerRenderHandler().render("/widget", session.getPlugin(), data.toRenderModel());
                session.responseHtmlStr(render, requestPacket.getMethodStr(), requestPacket.getMsgId());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Render widget error, uri=" + requestInfo.getUri(), e);
                String message = renderErrorMessage(e);
                session.sendMsg(ContentType.HTML, "Render widget error " + message, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
            }
        });
    }

    private String renderErrorMessage(Exception e) {
        if (isDevMode()) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            return "<pre style=\"white-space: pre-wrap; word-break: break-word;\">" + escapeHtml(writer.toString()) + "</pre>";
        }
        String message = e.getMessage();
        if (Objects.isNull(message) || message.isEmpty()) {
            message = e.getClass().getSimpleName();
        }
        return escapeHtml(message);
    }

    private boolean isDevMode() {
        return RunConstants.runType == RunType.DEV;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public void index() {
        Map<String, Object> data = new HashMap<>();
        data.put("theme", isDarkMode() ? "dark" : "light");
        data.put("data", new Gson().toJson(arrangerService.getPageData(session, requestInfo)));
        session.responseHtml("/templates/index", data, requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    private boolean isDarkMode() {
        return requestInfo.isDarkMode();
    }

    private Object paramObject(String key) {
        if (requestInfo.getParam() == null || requestInfo.getParam().get(key) == null || requestInfo.getParam().get(key).length == 0) {
            return null;
        }
        String[] values = requestInfo.getParam().get(key);
        return values.length == 1 ? values[0] : values;
    }

    private String paramValue(String key) {
        Object value = paramObject(key);
        return value == null ? null : String.valueOf(value);
    }

}
