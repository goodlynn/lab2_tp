package com.example.photogallery.models;

import java.io.Serializable;
import java.util.List;

public class SlideShowData implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<SlideData> slides;
    private double slideInterval;
    private String name;

    public SlideShowData(List<SlideData> slides, double slideInterval, String name) {
        this.slides = slides;
        this.slideInterval = slideInterval;
        this.name = name;
    }

    public List<SlideData> getSlides() { return slides; }
    public double getSlideInterval() { return slideInterval; }
    public String getName() { return name; }
}