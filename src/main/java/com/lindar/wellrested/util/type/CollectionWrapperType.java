package com.lindar.wellrested.util.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class CollectionWrapperType<T, U> implements ParameterizedType {
    private final Class<T> wrapper;
    private final Class<U> inner;
    public CollectionWrapperType(Class<T> wrapper, Class<U> inner) {
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
