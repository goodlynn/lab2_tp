package com.example.photogallery.models;

public interface MediaIterator {
    boolean hasNext();
    boolean hasPrevious();
    MediaItem next();
    MediaItem previous();
    MediaItem first();
    MediaItem last();
    int getCurrentIndex();
    void reset();
}