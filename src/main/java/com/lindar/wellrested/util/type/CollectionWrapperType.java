package com.lindar.wellrested.util.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class CollectionWrapperType implements ParameterizedType {
    private final Type wrapper;
    private final Type inner;
    public CollectionWrapperType(Type wrapper, Type inner) {
        this.wrapper = wrapper;
        this.inner = inner;
    }
    public Type[] getActualTypeArguments() {
        return new Type[]{inner};
    }
    public Type getRawType() {
        return wrapper;
    }
    public Type getOwnerType() {
        return null;
    }
}
