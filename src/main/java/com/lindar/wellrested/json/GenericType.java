package com.lindar.wellrested.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class GenericType<T> {
    protected final Type type;

    protected GenericType() {
        Type superClass = this.getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        } else {
            this.type = ((ParameterizedType)superClass).getActualTypeArguments()[0];
        }
    }

    /**
     * @return the Type which includes generic type information
     */
    public Type getType() {
        return this.type;
    }
}