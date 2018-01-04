package com.lindar.wellrested;

import com.google.gson.GsonBuilder;

@FunctionalInterface
public interface GsonCustomiser {
    void customise(GsonBuilder gsonBuilder);
}
