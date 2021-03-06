package com.lindar.wellrested.vo;

public class ResultBuilder<T> {

    private Result.ResultBuilder<T> resultBuilder;

    private ResultBuilder(Result.ResultBuilder<T> resultBuilder) {
        this.resultBuilder = resultBuilder;
    }

    public static <T> ResultBuilder<T> of(Result<T> result) {
        Result.ResultBuilder<T> resultBuilder = Result.<T>builder().data(result.getData())
                .msg(result.getMsg()).code(result.getCode())
                .visible(result.isVisible()).success(result.isSuccess());
        return new ResultBuilder<>(resultBuilder);
    }

    public static <T> ResultBuilder<T> successful() {
        return new ResultBuilder<>(Result.<T>builder().success(true));
    }

    public static <T> ResultBuilder<T> failed() {
        return new ResultBuilder<>(Result.<T>builder().success(false));
    }

    public static <T> ResultBuilder<T> empty() {
        return new ResultBuilder<>(Result.<T>builder());
    }

    /** This is a shortcut method for ResultBuilder.successful().data(data).build() */
    public static <T> Result<T> successful(T data) {
        return Result.<T>builder().success(true).data(data).build();
    }

    /** This is a shortcut method for ResultBuilder.successful().data(data).msg(msg).build() */
    public static <T> Result<T> successful(T data, String msg) {
        return Result.<T>builder().success(true).data(data).msg(msg).build();
    }

    /** This is a shortcut method for ResultBuilder.successful().msg(msg).build() */
    public static <T> Result<T> successfulWithoutData(String msg) {
        return Result.<T>builder().success(true).msg(msg).build();
    }

    /** This is a shortcut method for ResultBuilder.failed().msg(msg).build() */
    public static <T> Result<T> failed(String msg) {
        return Result.<T>builder().success(false).msg(msg).build();
    }

    public static <T> Result<T> failedCastingResult() {
        return Result.<T>builder().castingErrorThrown(true).success(false).msg("Failed casting response object").build();
    }

    public ResultBuilder<T> success(boolean success) {
        resultBuilder.success(success);
        return this;
    }

    public ResultBuilder<T> data(T data) {
        resultBuilder.data(data);
        return this;
    }

    public ResultBuilder<T> visible(boolean visible) {
        resultBuilder.visible(visible);
        return this;
    }

    public ResultBuilder<T> visible() {
        resultBuilder.visible(true);
        return this;
    }

    public ResultBuilder<T> hidden() {
        resultBuilder.visible(false);
        return this;
    }

    public ResultBuilder<T> msg(String msg) {
        resultBuilder.msg(msg);
        return this;
    }

    public ResultBuilder<T> code(String code) {
        resultBuilder.code(code);
        return this;
    }

    public ResultBuilder<T> castingErrorThrown() {
        resultBuilder.castingErrorThrown(true);
        return this;
    }

    public ResultBuilder<T> castingErrorThrown(boolean castingErrorThrown) {
        resultBuilder.castingErrorThrown(castingErrorThrown);
        return this;
    }

    public Result<T> build() {
        return resultBuilder.build();
    }

    public <U> Result<U> buildAndOverrideData(U data) {
        Result<T> oldResult = resultBuilder.build();
        return Result.<U>builder()
                .code(oldResult.getCode()).msg(oldResult.getMsg()).data(data)
                .success(oldResult.isSuccess()).visible(oldResult.isVisible()).build();
    }

    public <U> Result<U> buildAndIgnoreData() {
        Result<T> oldResult = resultBuilder.build();
        return Result.<U>builder().success(oldResult.isSuccess())
                .msg(oldResult.getMsg()).code(oldResult.getCode())
                .visible(oldResult.isVisible()).build();
    }
}
