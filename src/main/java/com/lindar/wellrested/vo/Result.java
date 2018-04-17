package com.lindar.wellrested.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@ToString
@EqualsAndHashCode
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1987956456765489787L;

    final private boolean success;
    final private boolean visible; // the result message is visible for the end user

    @Getter
    final private T data;
    @Getter
    final private String msg;
    @Getter
    final private String code; // response code - usually error code

    final private boolean castingErrorThrown;

    @java.beans.ConstructorProperties({"success", "visible", "data", "msg", "code", "castingErrorThrown"})
    public Result(boolean success, boolean visible, T data, String msg, String code, boolean castingErrorThrown) {
        this.success = success;
        this.visible = visible;
        this.data = data;
        this.msg = msg;
        this.code = code;
        this.castingErrorThrown = castingErrorThrown;
    }

    public static <T> ResultBuilder<T> builder() {
        return new ResultBuilder<>();
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailed() {
        return !success;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isSuccessAndNotNull() {
        return success && data != null;
    }

    public boolean isFailedAndNull() {
        return !success && data == null;
    }

    public boolean castingErrorThrown() {
        return castingErrorThrown;
    }

    public Result<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isSuccessAndNotNull()) {
            return predicate.test(data) ? this : com.lindar.wellrested.vo.ResultBuilder.of(this).success(false).buildAndIgnoreData();
        } else {
            return this;
        }
    }

    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isSuccessAndNotNull()) {
            return com.lindar.wellrested.vo.ResultBuilder.of(this).buildAndOverrideData(mapper.apply(data));
        } else {
            return com.lindar.wellrested.vo.ResultBuilder.of(this).success(false).buildAndIgnoreData();
        }
    }

    public <U> Result<U> flatMap(Function<? super T, Result<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (isSuccessAndNotNull()) {
            return Objects.requireNonNull(mapper.apply(data));
        } else {
            return com.lindar.wellrested.vo.ResultBuilder.of(this).success(false).buildAndIgnoreData();
        }
    }

    public void ifSuccess(Runnable function) {
        if (isSuccess()) {
            function.run();
        }
    }

    public void ifSuccessAndNotNull(Consumer<? super T> consumer) {
        if (isSuccessAndNotNull()) {
            consumer.accept(data);
        }
    }

    public T orElse(T other) {
        return isSuccessAndNotNull() ? data : other;
    }

    public Result<T> orElseProcessResult(Function<Result<T>, Result<T>> resultProcessor) {
        return isSuccessAndNotNull() ? this : resultProcessor.apply(this);
    }
    
    /**
     * The Runnable function here has NOTHING to do with Threads! It's just a functional interface that allows you to
     * run a piece of code (function) when the result data is null and success is false - a function that receives and returns nothing (void)
     *
     * @param function
     * @param defaultVal
     * @return
     */
    public T orElseDoAndReturnDefault(Runnable function, T defaultVal) {
        if (isSuccessAndNotNull()) {
            return data;
        }
        function.run();
        return defaultVal;
    }
    
    /**
     * The consumer function receives the entire result object as parameter in case you want to log or manage the error message or code in any way.
     * @param consumer
     * @param defaultVal
     * @return
     */
    public T orElseDoAndReturnDefault(Consumer<Result<T>> consumer, T defaultVal) {
        if (isSuccessAndNotNull()) {
            return data;
        }
        consumer.accept(this);
        return defaultVal;
    }
    
    /**
     * The consumer function receives the entire result object as parameter and is executed regardless of the result status (successful or not)
     * @param consumer
     */
    public void execute(Consumer<Result<T>> consumer) {
        consumer.accept(this);
    }
    
    /**
     * The consumer function receives the entire result object as parameter and is executed regardless of the result status (successful or not).
     * Returns defaultVal only if the actual result is not successful and data is null.
     * @param consumer
     * @param defaultVal
     * @return
     */
    public T execute(Consumer<Result<T>> consumer, T defaultVal) {
        consumer.accept(this);
        if (isSuccessAndNotNull()) {
            return data;
        }
        return defaultVal;
    }


    static class ResultBuilder<T> {
        private boolean success;
        private boolean visible;
        private T data;
        private String msg;
        private String code;

        private boolean castingErrorThrown;

        ResultBuilder() {
        }

        ResultBuilder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        ResultBuilder<T> visible(boolean visible) {
            this.visible = visible;
            return this;
        }

        ResultBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        ResultBuilder<T> msg(String msg) {
            this.msg = msg;
            return this;
        }

        ResultBuilder<T> code(String code) {
            this.code = code;
            return this;
        }

        ResultBuilder<T> castingErrorThrown(boolean castingErrorThrown) {
            this.castingErrorThrown = castingErrorThrown;
            return this;
        }

        Result<T> build() {
            return new Result<T>(success, visible, data, msg, code, castingErrorThrown);
        }

        public String toString() {
            return "Result.ResultBuilder(success=" + this.success + ", visible=" + this.visible + ", data=" + this.data
                    + ", msg=" + this.msg + ", code=" + this.code + ", castingErrorThrown=" + this.castingErrorThrown + ")";
        }
    }
}
