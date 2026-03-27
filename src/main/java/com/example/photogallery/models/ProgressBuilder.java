package com.example.photogallery.models;

public interface ProgressBuilder {
    void setCurrent(int current);
    void setTotal(int total);
    void buildMessage();
    ProgressStatus getResult();
}