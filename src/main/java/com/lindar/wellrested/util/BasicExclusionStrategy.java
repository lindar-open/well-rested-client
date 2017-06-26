package com.lindar.wellrested.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.List;
import java.util.Set;

public class BasicExclusionStrategy implements ExclusionStrategy {

    private List<String> fieldNames;
    private Set<String> classNames;

    public BasicExclusionStrategy (List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public BasicExclusionStrategy (Set<String> classNames) {
        this.classNames = classNames;
    }

    public BasicExclusionStrategy (Set<String> classNames, List<String> fieldNames) {
        this.classNames = classNames;
        this.fieldNames = fieldNames;
    }

    public boolean shouldSkipClass(Class<?> arg0) {
        return classNames != null && classNames.contains(arg0.getSimpleName());
    }

    public boolean shouldSkipField(FieldAttributes f) {
        return fieldNames != null && fieldNames.contains(f.getName());
    }
}
