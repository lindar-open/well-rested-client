package com.lindar.wellrested;

import com.google.gson.GsonBuilder;
import com.lindar.wellrested.util.BasicExclusionStrategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestGsonCustomiser implements GsonCustomiser {

    public void customise(GsonBuilder builder){
        List<String> excludeFields = new ArrayList<String>();
        excludeFields.add("title");

        BasicExclusionStrategy exclude = new BasicExclusionStrategy(excludeFields);

        builder.setExclusionStrategies(exclude);

        return;
    }

}
