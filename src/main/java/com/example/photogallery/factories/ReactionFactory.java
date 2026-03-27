package com.example.photogallery.factories;

import com.example.photogallery.models.MediaItem;
import javafx.scene.image.Image;
import java.nio.file.Path;

public interface ReactionFactory {
    Image createMainImage(Path imagePath);
    String createReactionEmoji();

    default MediaItem createMediaItem(Path imagePath, int position) {
        return new MediaItem(
                createMainImage(imagePath),
                imagePath.toString(),
                createReactionEmoji(),
                position
        );
    }
}