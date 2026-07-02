package com.zrlog.plugin.article.arranger;

import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.article.arranger.controller.ArticleArrangerController;
import com.zrlog.plugin.article.arranger.vo.ApiResponse;
import com.zrlog.plugin.article.arranger.vo.ArrangeOutlineVO;
import com.zrlog.plugin.article.arranger.vo.ArrangerConfig;
import com.zrlog.plugin.article.arranger.vo.ArrangerInfoResponse;
import com.zrlog.plugin.article.arranger.vo.ArrangerUpdateRequest;
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
import com.zrlog.plugin.article.arranger.vo.StandardResponse;
import com.zrlog.plugin.article.arranger.vo.WebsiteConfigRequest;
import com.zrlog.plugin.article.arranger.vo.WidgetDataEntry;
import com.zrlog.plugin.common.PluginNativeImageUtils;
import com.zrlog.plugin.message.Plugin;
import com.zrlog.plugin.render.FreeMarkerRenderHandler;
import com.zrlog.plugin.render.SimpleTemplateRender;
import com.zrlog.plugin.type.RunType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GraalvmAgentApplication {


    public static void main(String[] args) throws IOException {
        RunConstants.runType = RunType.AGENT;
        String basePath = System.getProperty("user.dir").replace("\\target", "").replace("/target", "");
        File file = new File(basePath + "/src/main/resources");
        PluginNativeImageUtils.doLoopResourceLoad(file.listFiles(), file.getPath() + "/", "/");
        PluginNativeImageUtils.exposeController(Collections.singletonList(ArticleArrangerController.class));
        PluginNativeImageUtils.usedGsonObject();
        freemarkerInit();
        PluginNativeImageUtils.gsonNativeAgentByClazz(Arrays.asList(
                ApiResponse.class,
                ArrangeOutlineVO.class,
                ArrangerConfig.class,
                ArrangerInfoResponse.class,
                ArrangerUpdateRequest.class,
                ArticleCategoryGroup.class,
                ArticleCategoryItem.class,
                ArticleDetailInfo.class,
                ArticleDetailResponse.class,
                ArticleInfo.class,
                ArticleListData.class,
                ArticleListResponse.class,
                ArticleTypeInfo.class,
                Plugin.class,
                PublicCacheData.class,
                PublicCacheResponse.class,
                StandardResponse.class,
                WebsiteConfigRequest.class,
                WidgetDataEntry.class
        ));
        Application.main(args);

    }

    private static void freemarkerInit() {
        Plugin plugin = new Plugin();
        plugin.setName("test");
        plugin.setDesc("test");
        plugin.setVersion("test");
        WidgetDataEntry widgetData = new WidgetDataEntry();
        widgetData.setStyleGlobal("");
        widgetData.setMainColor("");
        List<ArrangeOutlineVO> articleInfoList = new ArrayList<>();
        ArrangeOutlineVO articleInfo = new ArrangeOutlineVO();
        articleInfo.setActive(false);
        articleInfo.setUrl("/");
        articleInfo.setTitle("test");
        articleInfoList.add(articleInfo);
        widgetData.setItems(articleInfoList);

        Map<String, Object> indexData = new HashMap<>();
        indexData.put("theme", "dark");
        indexData.put("data", "{}");
        new SimpleTemplateRender().render("/templates/index", plugin, indexData);
        new FreeMarkerRenderHandler().render("/widget", plugin, widgetData.toRenderModel());
    }
}
