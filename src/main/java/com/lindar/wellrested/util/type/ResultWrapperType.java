package com.lindar.wellrested.util.type;

import com.lindar.wellrested.vo.Result;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ResultWrapperType<T, U> implements ParameterizedType {
    private final CollectionWrapperType<T, U> inner;
    public ResultWrapperType(CollectionWrapperType<T, U> inner) {
        this.inner = inner;
    }
    public Type[] getActualTypeArguments() {
        return new Type[]{inner};
    }
    public Type getRawType() {
        return Result.class;
    }
    public Type getOwnerType() {
        return null;
    }
}
