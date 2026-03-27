package com.example.photogallery.models;

import javafx.scene.image.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MediaItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient Image mainImage;
    private String imagePath;
    private String emotion;
    private List<String> hashtags;
    private List<String> stickers;
    private int order;

    public MediaItem(Image image, String path, String emotion, int order) {
        this.mainImage = image;
        this.imagePath = path;
        this.emotion = emotion;
        this.order = order;
        this.hashtags = new ArrayList<>();
        this.stickers = new ArrayList<>();
    }

    public Image getImage() {
        if (mainImage == null && imagePath != null) {
            mainImage = new Image(new java.io.File(imagePath).toURI().toString());
        }
        return mainImage;
    }

    public String getImagePath() { return imagePath; }
    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }
    public List<String> getHashtags() { return hashtags; }
    public void addHashtag(String hashtag) { if (!hashtags.contains(hashtag)) hashtags.add(hashtag); }
    public void removeHashtag(String hashtag) { hashtags.remove(hashtag); }
    public List<String> getStickers() { return stickers; }
    public void addSticker(String sticker) { stickers.add(sticker); }
    public void removeSticker(String sticker) { stickers.remove(sticker); }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public String getHashtagsText() { return String.join(" ", hashtags); }
}