package com.lindar.wellrested.vo;


public class ResultFactory {

    public static <T> Result<T> successful(T data) {
        return Result.<T>builder().success(true).data(data).build();
    }

    public static <T> Result<T> successful(T data, String msg) {
        return Result.<T>builder().success(true).data(data).msg(msg).build();
    }

    public static <T> Result<T> successful(T data, String msg, String code) {
        return Result.<T>builder().success(true).data(data).msg(msg).code(code).build();
    }

    public static <T> Result<T> successfulMsg(String msg) {
        return Result.<T>builder().success(true).msg(msg).build();
    }

    public static <T> Result<T> successful(String msg, String code) {
        return Result.<T>builder().success(true).msg(msg).code(code).build();
    }


    public static <T> Result<T> failed(String msg) {
        return Result.<T>builder().success(false).msg(msg).build();
    }

    public static <T> Result<T> failed(String msg, String code) {
        return Result.<T>builder().success(false).msg(msg).code(code).build();
    }

    public static <T> Result<T> failed(T data, String msg) {
        return Result.<T>builder().success(false).msg(msg).data(data).build();
    }

    public static <T> Result<T> failed(T data, String msg, String code) {
        return Result.<T>builder().success(false).data(data).msg(msg).code(code).build();
    }

    private ResultFactory() {
    }
}