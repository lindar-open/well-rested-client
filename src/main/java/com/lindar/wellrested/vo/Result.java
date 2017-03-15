package com.lindar.wellrested.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

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

    public T getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }

    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isSuccess()) {
            return ResultFactory.getFailResult(msg);
        } else {
            return ResultFactory.getSuccessResult(mapper.apply(data));
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

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("\"Result{\"");
        sb.append("success=").append(success);
        sb.append(", msg=").append(msg);

        sb.append(", data=");

        if (data == null) {

            sb.append("null");

        } else if (data instanceof List) {

            List castList = (List) data;
            if (castList.isEmpty()) {

                sb.append("empty list");

            } else {
                Object firstItem = castList.get(0);

                sb.append("List of ").append(firstItem.getClass());
            }

        } else {
            sb.append(data.toString());
        }

        sb.append("}");

        return sb.toString();

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.success ? 1 : 0);
        hash = 89 * hash + Objects.hashCode(this.data);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Result<?> other = (Result<?>) obj;
        if (this.success != other.success) {
            return false;
        }
        return Objects.deepEquals(this.data, other.data);
    }
}
