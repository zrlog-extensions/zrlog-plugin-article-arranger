package com.zrlog.plugin.article.arranger;


import com.zrlog.plugin.client.NioClient;
import com.zrlog.plugin.render.FreeMarkerRenderHandler;
import com.zrlog.plugin.render.SimpleTemplateRender;
import com.zrlog.plugin.article.arranger.controller.ArticleArrangerController;
import com.zrlog.plugin.article.arranger.handle.ConnectHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Application {

    private static final ConnectHandler connectHandler = new ConnectHandler();

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        List<Class> classList = new ArrayList<>();
        classList.add(ArticleArrangerController.class);
        new NioClient(connectHandler, new FreeMarkerRenderHandler(), new ArticleArrangerClientActionHandler()).connectServer(args, classList, ArticleArrangerPluginAction.class);
    }
}

