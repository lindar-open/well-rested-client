package com.lindar.wellrested.vo;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1987956456765489787L;

    final private boolean success;
    final private T data;
    final private String msg;

    public Result(boolean success, T data, String msg) {
        this.success = success;
        this.data = data;
        this.msg = msg;
    }

    public Result(boolean success, String msg) {
        this.success = success;
        this.data = null;
        this.msg = msg;
    }

    public Result(boolean success, T data) {
        this.success = success;
        this.data = data;
        this.msg = StringUtils.EMPTY;
    }

    public boolean isSuccess() {
        return success;
    }
    
    public boolean isSuccessAndNotNull() {
        return success && data != null;
    }

    public Result<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isSuccess()) {
            return this;
        } else {
            return predicate.test(data) ? this : ResultFactory.getFailResult(msg);
        }
    }

    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isSuccess()) {
            return ResultFactory.getFailResult(msg);
        } else {
            return ResultFactory.getSuccessResult(mapper.apply(data));
        }
    }

    public <U> Result<U> flatMap(Function<? super T, Result<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isSuccess()) {
            return ResultFactory.getFailResult(msg);
        } else {
            return Objects.requireNonNull(mapper.apply(data));
        }
    }

    public void ifSuccess(Consumer<? super T> consumer) {
        if (data != null) {
            consumer.accept(data);
        }
    }

    public T orElse(T other) {
        return data != null ? data : other;
    }
}
