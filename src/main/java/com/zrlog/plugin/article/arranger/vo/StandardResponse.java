package com.zrlog.plugin.article.arranger.vo;

public class StandardResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> StandardResponse<T> success(T data) {
        StandardResponse<T> response = new StandardResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
