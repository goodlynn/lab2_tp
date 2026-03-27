package com.example.photogallery.factories;

import javafx.scene.image.Image;
import java.nio.file.Path;

public abstract class BaseReactionFactory implements ReactionFactory {

    @Override
    public Image createMainImage(Path imagePath) {
        try {
            return new Image(imagePath.toUri().toString(), true);
        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath);
            return null;
        }
    }
}