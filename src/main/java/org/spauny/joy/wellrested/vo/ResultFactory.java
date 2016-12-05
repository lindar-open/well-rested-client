package org.spauny.joy.wellrested.vo;


public class ResultFactory {

    public static <T> Result<T> getSuccessResult(T data) {

        return new Result<>(true, data);
    }

    public static <T> Result<T> getSuccessResult(T data, String msg) {

        return new Result<>(true, data, msg);
    }

    public static <T> Result<T> getSuccessResultMsg(String msg) {

        return new Result<>(true, msg);
    }

    public static <T> Result<T> getFailResult(String msg) {

        return new Result<>(false, msg);
    }
    
    public static <T> Result<T> getFailResult(T data, String msg) {

        return new Result<>(false, data, msg);
    }

    public static <T> Result<T> getNotFoundResult() {

        return new Result<>(false, "Not Found");
    }

    private ResultFactory() {
    }
}
