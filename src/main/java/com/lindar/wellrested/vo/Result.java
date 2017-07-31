package com.lindar.wellrested.vo;

import lombok.Builder;
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
@Builder
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

    public boolean isSuccess() {
        return success;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isSuccessAndNotNull() {
        return success && data != null;
    }

    public Result<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isSuccessAndNotNull()) {
            return predicate.test(data) ? this : ResultFactory.failed(this);
        } else {
            return this;
        }
    }

    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isSuccessAndNotNull()) {
            return ResultFactory.successful(mapper.apply(data), this);
        } else {
            return ResultFactory.failed(this);
        }
    }

    public <U> Result<U> flatMap(Function<? super T, Result<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (isSuccessAndNotNull()) {
            return Objects.requireNonNull(mapper.apply(data));
        } else {
            return ResultFactory.failed(this);
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

}
