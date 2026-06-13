package com.zrlog.plugin.article.arranger.util;

import com.google.gson.JsonElement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ArrangerUtils {

    private ArrangerUtils() {
    }

    public static String toActionUri(String uri) {
        return uri.replace(".action", "").replace(".html", "");
    }

    public static boolean isTypePage(String uri) {
        return uri.contains("sort/");
    }

    public static String extractTypeAlias(String uri) {
        String value = uri;
        int index = value.indexOf("sort/");
        if (index >= 0) {
            value = value.substring(index + "sort/".length());
        }
        return value.replace("/", "").replace(".html", "");
    }

    public static Set<String> selectedStringSet(Object value) {
        Set<String> result = new HashSet<>();
        if (Objects.isNull(value)) {
            return result;
        }
        if (value instanceof JsonElement) {
            JsonElement element = (JsonElement) value;
            if (element.isJsonNull()) {
                return result;
            }
            if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(item -> {
                    if (!item.isJsonNull()) {
                        result.add(idString(item.getAsString()));
                    }
                });
                return result;
            }
            if (element.isJsonPrimitive()) {
                result.add(idString(element.getAsString()));
                return result;
            }
        }
        if (value instanceof Collection) {
            ((Collection<?>) value).stream().filter(Objects::nonNull).map(ArrangerUtils::idString).forEach(result::add);
            return result;
        }
        if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            Arrays.stream(array).filter(Objects::nonNull).map(ArrangerUtils::idString).forEach(result::add);
            return result;
        }
        result.add(idString(value));
        return result;
    }

    public static String idString(Object value) {
        if (value instanceof Number) {
            return String.valueOf(((Number) value).intValue());
        }
        String raw = String.valueOf(value);
        try {
            double number = Double.parseDouble(raw);
            if (number == Math.rint(number)) {
                return String.valueOf((int) number);
            }
        } catch (NumberFormatException ignored) {
            return raw;
        }
        return raw;
    }

    public static String stringValue(Object value, String fallback) {
        if (Objects.isNull(value)) {
            return fallback;
        }
        String str = String.valueOf(value);
        return str.trim().isEmpty() ? fallback : str;
    }
}
