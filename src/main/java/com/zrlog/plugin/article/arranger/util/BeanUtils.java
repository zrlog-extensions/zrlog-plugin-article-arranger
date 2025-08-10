package com.zrlog.plugin.article.arranger.util;

import com.google.gson.Gson;

public class BeanUtils {

    public static <T> T convert(Object obj, Class<T> tClass) {
        String jsonStr = new Gson().toJson(obj);
        return new Gson().fromJson(jsonStr, tClass);
    }
}
