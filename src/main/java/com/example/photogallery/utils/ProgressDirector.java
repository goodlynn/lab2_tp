package com.example.photogallery.utils;

import com.example.photogallery.models.ProgressBuilder;
import com.example.photogallery.models.ProgressStatus;

public class ProgressDirector {
    public ProgressStatus build(ProgressBuilder builder, int current, int total) {
        builder.setCurrent(current);
        builder.setTotal(total);
        builder.buildMessage();
        return builder.getResult();
    }
}