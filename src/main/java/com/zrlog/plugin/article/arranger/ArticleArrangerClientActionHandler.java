package com.zrlog.plugin.article.arranger;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.article.arranger.controller.ArticleArrangerController;
import com.zrlog.plugin.client.ClientActionHandler;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;

public class ArticleArrangerClientActionHandler extends ClientActionHandler {

    @Override
    public void getFile(IOSession session, MsgPacket msgPacket) {
        HttpRequestInfo httpRequestInfo = new Gson().fromJson(msgPacket.getDataStr(), HttpRequestInfo.class);
        if(httpRequestInfo.getUri().endsWith(".html")) {
            new ArticleArrangerController(session,msgPacket,httpRequestInfo).widget();
            return;
        }
        super.getFile(session, msgPacket);
    }

    @Override
    public void httpMethod(IOSession session, MsgPacket msgPacket) {
        HttpRequestInfo httpRequestInfo = new Gson().fromJson(msgPacket.getDataStr(), HttpRequestInfo.class);
        if (httpRequestInfo.getUri().equals("index.action")) {
            new ArticleArrangerController(session,msgPacket,httpRequestInfo).index();
        }else if (httpRequestInfo.getUri().equals("/update.action")) {
            new ArticleArrangerController(session,msgPacket,httpRequestInfo).update();
        } else {
            new ArticleArrangerController(session,msgPacket,httpRequestInfo).widget();
        }
    }

    @Override
    public void refreshCache(IOSession session, MsgPacket msgPacket) {

    }
}
