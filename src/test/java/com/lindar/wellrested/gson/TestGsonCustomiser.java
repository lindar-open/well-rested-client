package com.lindar.wellrested.gson;

import com.google.gson.GsonBuilder;
import com.lindar.wellrested.GsonCustomiser;
import com.lindar.wellrested.util.BasicExclusionStrategy;

import java.util.ArrayList;
import java.util.List;

public class TestGsonCustomiser implements GsonCustomiser {

    public void customise(GsonBuilder builder){
        List<String> excludeFields = new ArrayList<String>();
        excludeFields.add("title");

        BasicExclusionStrategy exclude = new BasicExclusionStrategy(excludeFields);

        builder.setExclusionStrategies(exclude);

        return;
    }

}
