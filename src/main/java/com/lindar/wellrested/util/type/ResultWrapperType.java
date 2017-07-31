package com.lindar.wellrested.util.type;

import com.lindar.wellrested.vo.Result;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ResultWrapperType implements ParameterizedType {
    private final Type inner;
    public ResultWrapperType(Type inner) {
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
