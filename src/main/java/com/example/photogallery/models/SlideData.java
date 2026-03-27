package com.example.photogallery.models;

import java.io.Serializable;
import java.util.List;

public class SlideData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String imagePath;
    private String emotion;
    private List<String> hashtags;
    private List<String> stickers;
    private int order;

    public SlideData(String imagePath, String emotion, List<String> hashtags,
                     List<String> stickers, int order) {
        this.imagePath = imagePath;
        this.emotion = emotion;
        this.hashtags = hashtags;
        this.stickers = stickers;
        this.order = order;
    }

    public String getImagePath() { return imagePath; }
    public String getEmotion() { return emotion; }
    public List<String> getHashtags() { return hashtags; }
    public List<String> getStickers() { return stickers; }
    public int getOrder() { return order; }
}