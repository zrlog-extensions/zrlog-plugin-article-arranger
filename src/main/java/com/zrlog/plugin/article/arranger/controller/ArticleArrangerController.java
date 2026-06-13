package com.zrlog.plugin.article.arranger.controller;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.article.arranger.service.ArrangerHelper;
import com.zrlog.plugin.article.arranger.util.BeanUtils;
import com.zrlog.plugin.article.arranger.vo.ArrangerConfig;
import com.zrlog.plugin.article.arranger.vo.ArrangerInfoResponse;
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
        session.sendMsg(new MsgPacket(requestInfo.simpleParam(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(), ActionType.SET_WEBSITE.name()), msgPacket -> {
            session.sendMsg(new MsgPacket(StandardResponse.success(Boolean.TRUE), ContentType.JSON, MsgPacketStatus.RESPONSE_SUCCESS, requestPacket.getMsgId(), requestPacket.getMethodStr()));
            //更新缓存
            session.sendJsonMsg(new HashMap<>(), ActionType.REFRESH_CACHE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST);
        });
    }


    public void widget() {
        session.sendJsonMsg(WebsiteConfigRequest.arrangerKeys(), ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            ArrangerConfig config = normalizeConfig(new Gson().fromJson(msgPacket.getDataStr(), ArrangerConfig.class));
            try {
                String realUri = requestInfo.getUri().replace(".action", "").replace(".html", "");
                WidgetDataEntry data = ArrangerHelper.getWidgetData(session, realUri, config);
                data.setStyleGlobal(config.getStyleGlobal());
                data.setMainColor(config.getMainColor());
                String render = new FreeMarkerRenderHandler().render("/widget", session.getPlugin(), BeanUtils.convert(data, HashMap.class));
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
        data.put("data", new Gson().toJson(pageData()));
        session.responseHtml("/templates/index", data, requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    private StandardResponse<ArrangerInfoResponse> pageData() {
        ArrangerConfig config = normalizeConfig(session.getResponseSync(ContentType.JSON, WebsiteConfigRequest.arrangerKeys(), ActionType.GET_WEBSITE, ArrangerConfig.class));
        config.setGroups(ArrangerHelper.getArticleCategoryGroups(session, config));

        ArrangerInfoResponse data = new ArrangerInfoResponse();
        data.setDark(requestInfo.isDarkMode());
        data.setColorPrimary(requestInfo.getAdminColorPrimary());
        data.setPlugin(session.getPlugin());
        data.setConfig(config);

        return StandardResponse.success(data);
    }

    private ArrangerConfig normalizeConfig(ArrangerConfig config) {
        if (config == null) {
            config = new ArrangerConfig();
        }
        config.setStyleGlobal(Objects.requireNonNullElse(config.getStyleGlobal(), ""));
        config.setMainColor(Objects.requireNonNullElse(config.getMainColor(), "#007BFF"));
        return config;
    }

    private boolean isDarkMode() {
        return requestInfo.isDarkMode();
    }

}
