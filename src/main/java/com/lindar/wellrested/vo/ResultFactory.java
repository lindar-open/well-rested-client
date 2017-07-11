package com.lindar.wellrested.vo;


public class ResultFactory {

    public static <T, U> Result<T> successful(T newData, Result<U> anotherResult) {
        return Result.<T>builder()
                .success(true).visible(anotherResult.isVisible()).code(anotherResult.getCode())
                .data(newData).build();
    }

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

    public static <T, U> Result<T> failed(Result<U> anotherResult) {
        return Result.<T>builder().success(false)
                .msg(anotherResult.getMsg())
                .code(anotherResult.getCode())
                .visible(anotherResult.isVisible())
                .build();
    }

    public static <T, U> Result<T> copyWithoutData(Result<U> anotherResult) {
        return Result.<T>builder().success(anotherResult.isSuccess())
                .msg(anotherResult.getMsg())
                .code(anotherResult.getCode())
                .visible(anotherResult.isVisible())
                .build();
    }

    private ResultFactory() {
    }
}