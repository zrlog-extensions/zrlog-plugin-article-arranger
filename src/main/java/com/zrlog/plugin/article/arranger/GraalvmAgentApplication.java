package com.zrlog.plugin.article.arranger;

import com.google.gson.Gson;
import com.zrlog.plugin.article.arranger.controller.ArticleArrangerController;
import com.zrlog.plugin.article.arranger.vo.ArrangeOutlineVO;
import com.zrlog.plugin.article.arranger.vo.ArticleInfo;
import com.zrlog.plugin.common.PluginNativeImageUtils;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.message.Plugin;
import com.zrlog.plugin.render.FreeMarkerRenderHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GraalvmAgentApplication {


    public static void main(String[] args) throws IOException {
        new Gson().toJson(new HttpRequestInfo());
        new Gson().toJson(new Plugin());
        String basePath = System.getProperty("user.dir").replace("\\target","").replace("/target", "");
        File file = new File(basePath + "/src/main/resources");
        PluginNativeImageUtils.doLoopResourceLoad(file.listFiles(), file.getPath()  + "/", "/");
        PluginNativeImageUtils.exposeController(Collections.singletonList(ArticleArrangerController.class));
        PluginNativeImageUtils.usedGsonObject();
        freemarkerInit();
        new Gson().toJson(new ArrangeOutlineVO());
        new Gson().toJson(new ArticleInfo());
        Application.main(args);

    }

    private static void freemarkerInit(){
        Plugin plugin = new Plugin();
        plugin.setName("test");
        plugin.setDesc("test");
        plugin.setVersion("test");
        Map<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("styleGlobal", "");
        objectObjectHashMap.put("theme", "dark");
        objectObjectHashMap.put("mainColor", "");
        objectObjectHashMap.put("groups", new ArrayList<>());

        List<ArrangeOutlineVO> articleInfoList = new ArrayList<>();
        ArrangeOutlineVO articleInfo = new ArrangeOutlineVO();
        articleInfo.setActive(false);
        articleInfo.setUrl("/");
        articleInfo.setTitle("test");
        articleInfoList.add(articleInfo);
        objectObjectHashMap.put("items",articleInfoList);
        new FreeMarkerRenderHandler().render("/templates/index.ftl", plugin, objectObjectHashMap);
        new FreeMarkerRenderHandler().render("/templates/widget.ftl", plugin, objectObjectHashMap);
    }
}