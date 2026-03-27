package com.example.photogallery.models;

import com.example.photogallery.factories.NeutralReactionFactory;
import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MediaCollection {
    private List<MediaItem> items;
    private Path sourceDirectory;

    public MediaCollection() {
        this.items = new ArrayList<>();
    }

    public void loadFromDirectory(Path directory) {
        this.sourceDirectory = directory;
        items.clear();

        if (!Files.exists(directory)) return;

        try (var stream = Files.list(directory)) {
            List<Path> imageFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(this::isImageFile)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .toList();

            NeutralReactionFactory factory = new NeutralReactionFactory();
            for (int i = 0; i < imageFiles.size(); i++) {
                Image image = new Image(imageFiles.get(i).toUri().toString());
                items.add(new MediaItem(image, imageFiles.get(i).toString(), "😐", i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addItem(MediaItem item) {
        items.add(item);
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    public MediaItem getItem(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return null;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void reverseOrder() {
        java.util.Collections.reverse(items);
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setOrder(i);
        }
    }

    public MediaIterator createIterator() {
        return new MediaCollectionIterator();
    }

    private boolean isImageFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") ||
                name.endsWith(".bmp") || name.endsWith(".webp");
    }

    private class MediaCollectionIterator implements MediaIterator {
        private int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor < items.size() - 1;
        }

        @Override
        public boolean hasPrevious() {
            return cursor > 0;
        }

        @Override
        public MediaItem next() {
            if (hasNext()) {
                cursor++;
                return items.get(cursor);
            }
            return items.get(cursor);
        }

        @Override
        public MediaItem previous() {
            if (hasPrevious()) {
                cursor--;
                return items.get(cursor);
            }
            return items.get(cursor);
        }

        @Override
        public MediaItem first() {
            cursor = 0;
            return items.get(cursor);
        }

        @Override
        public MediaItem last() {
            cursor = items.size() - 1;
            return items.get(cursor);
        }

        @Override
        public int getCurrentIndex() {
            return cursor;
        }

        @Override
        public void reset() {
            cursor = 0;
        }
    }
}