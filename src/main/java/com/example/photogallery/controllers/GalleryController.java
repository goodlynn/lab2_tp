package com.example.photogallery.controllers;

import com.example.photogallery.factories.*;
import com.example.photogallery.models.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GalleryController {

    @FXML private ImageView displayArea;
    @FXML private Label emotionLabel;
    @FXML private ComboBox<String> emotionSelector;
    @FXML private Label counterLabel;
    @FXML private TextField intervalInput;
    @FXML private StackPane rootPane;
    @FXML private ProgressBar progressBar;
    @FXML private VBox mainContainer;
    @FXML private Button playButton;
    @FXML private Button stopButton;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button addImageButton;

    private List<MediaItem> mediaItems = new ArrayList<>();
    private List<Path> imageFiles = new ArrayList<>();
    private int currentIndex = 0;
    private Timeline slideshowTimer;
    private double slideInterval = 2000;
    private boolean isPlaying = false;
    private Path imagesFolderPath;

    @FXML
    public void initialize() {
        // Устанавливаем путь к папке images
        imagesFolderPath = Paths.get("src/main/resources/images");

        // Создаем папку если её нет
        try {
            if (!Files.exists(imagesFolderPath)) {
                Files.createDirectories(imagesFolderPath);
                System.out.println("Создана папка: " + imagesFolderPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Не удалось создать папку images: " + e.getMessage());
        }

        setupUI();
        setupIcons();
        setupSlideshowTimer();

        // Загружаем изображения из папки images
        loadImagesFromFolder();
    }

    private void setupUI() {
        // Настройка ComboBox со смайликами
        emotionSelector.getItems().addAll("😊 Счастливый", "🎉 Восторженный", "😢 Грустный", "😐 Нейтральный");
        emotionSelector.setValue("😐 Нейтральный");
        emotionSelector.setStyle("-fx-font-size: 14px; -fx-font-family: 'Segoe UI';");

        // Настройка метки эмоций - в правом нижнем углу
        emotionLabel.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0.5, 2, 2); " +
                "-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 35; " +
                "-fx-padding: 10 22;");

        // Позиционируем эмодзи в правом нижнем углу
        StackPane.setAlignment(emotionLabel, Pos.BOTTOM_RIGHT);

        // Настройка прогресс-бара
        progressBar.setStyle("-fx-accent: linear-gradient(to right, #ff6b6b, #ff8e8e);");

        // Установка значения интервала по умолчанию
        intervalInput.setText("2000");
        intervalInput.setPrefWidth(100);
    }

    private void setupIcons() {
        try {
            // Иконка для кнопки добавления изображения
            java.net.URL folderUrl = getClass().getResource("/icons/folder.png");
            if (folderUrl != null) {
                ImageView folderIcon = new ImageView(new Image(folderUrl.toExternalForm()));
                folderIcon.setFitHeight(24);
                folderIcon.setFitWidth(24);
                addImageButton.setGraphic(folderIcon);
                addImageButton.setText(" Добавить изображение");
            } else {
                addImageButton.setText("📁 Добавить изображение");
            }

            // Иконки для кнопок навигации
            java.net.URL prevUrl = getClass().getResource("/icons/back.png");
            if (prevUrl != null) {
                ImageView prevIcon = new ImageView(new Image(prevUrl.toExternalForm()));
                prevIcon.setFitHeight(28);
                prevIcon.setFitWidth(28);
                prevButton.setGraphic(prevIcon);
                prevButton.setText("");
            } else {
                prevButton.setText("◀");
            }

            java.net.URL playUrl = getClass().getResource("/icons/play.png");
            if (playUrl != null) {
                ImageView playIcon = new ImageView(new Image(playUrl.toExternalForm()));
                playIcon.setFitHeight(28);
                playIcon.setFitWidth(28);
                playButton.setGraphic(playIcon);
                playButton.setText("");
            } else {
                playButton.setText("▶");
            }

            java.net.URL stopUrl = getClass().getResource("/icons/stop.png");
            if (stopUrl != null) {
                ImageView stopIcon = new ImageView(new Image(stopUrl.toExternalForm()));
                stopIcon.setFitHeight(28);
                stopIcon.setFitWidth(28);
                stopButton.setGraphic(stopIcon);
                stopButton.setText("");
            } else {
                stopButton.setText("⏹");
            }

            java.net.URL nextUrl = getClass().getResource("/icons/forward.png");
            if (nextUrl != null) {
                ImageView nextIcon = new ImageView(new Image(nextUrl.toExternalForm()));
                nextIcon.setFitHeight(28);
                nextIcon.setFitWidth(28);
                nextButton.setGraphic(nextIcon);
                nextButton.setText("");
            } else {
                nextButton.setText("▶");
            }
        } catch (Exception e) {
            addImageButton.setText("📁 Добавить изображение");
            prevButton.setText("◀");
            playButton.setText("▶");
            stopButton.setText("⏹");
            nextButton.setText("▶");
        }

        // Стилизация кнопок навигации
        String navButtonStyle = "-fx-background-color: linear-gradient(to bottom, #4a5e7a, #2a3a5a); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 24px; " +
                "-fx-background-radius: 40; -fx-cursor: hand; -fx-min-width: 80; -fx-min-height: 80;";

        prevButton.setStyle(navButtonStyle);
        playButton.setStyle(navButtonStyle);
        stopButton.setStyle(navButtonStyle);
        nextButton.setStyle(navButtonStyle);

        // Стилизация кнопки добавления изображения
        addImageButton.setStyle("-fx-background-color: linear-gradient(to bottom, #4a5e7a, #2a3a5a); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; " +
                "-fx-background-radius: 35; -fx-padding: 12 25; -fx-cursor: hand;");

        addButtonHoverEffects();
    }

    private void addButtonHoverEffects() {
        prevButton.setOnMouseEntered(e -> prevButton.setStyle(prevButton.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        prevButton.setOnMouseExited(e -> prevButton.setStyle(prevButton.getStyle().replace("-fx-scale-x: 1.05; -fx-scale-y: 1.05;", "")));

        playButton.setOnMouseEntered(e -> playButton.setStyle(playButton.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        playButton.setOnMouseExited(e -> playButton.setStyle(playButton.getStyle().replace("-fx-scale-x: 1.05; -fx-scale-y: 1.05;", "")));

        stopButton.setOnMouseEntered(e -> stopButton.setStyle(stopButton.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        stopButton.setOnMouseExited(e -> stopButton.setStyle(stopButton.getStyle().replace("-fx-scale-x: 1.05; -fx-scale-y: 1.05;", "")));

        nextButton.setOnMouseEntered(e -> nextButton.setStyle(nextButton.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        nextButton.setOnMouseExited(e -> nextButton.setStyle(nextButton.getStyle().replace("-fx-scale-x: 1.05; -fx-scale-y: 1.05;", "")));

        addImageButton.setOnMouseEntered(e -> addImageButton.setStyle("-fx-background-color: linear-gradient(to bottom, #5a6e8a, #3a4a6a); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; " +
                "-fx-background-radius: 35; -fx-padding: 12 25; -fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        addImageButton.setOnMouseExited(e -> addImageButton.setStyle("-fx-background-color: linear-gradient(to bottom, #4a5e7a, #2a3a5a); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; " +
                "-fx-background-radius: 35; -fx-padding: 12 25; -fx-cursor: hand;"));
    }

    private void loadImagesFromFolder() {
        // Очищаем списки
        mediaItems.clear();
        imageFiles.clear();

        List<Path> loadedFiles = new ArrayList<>();

        // Загружаем все изображения из папки images
        if (Files.exists(imagesFolderPath)) {
            try (var stream = Files.list(imagesFolderPath)) {
                loadedFiles = stream
                        .filter(Files::isRegularFile)
                        .filter(this::isImageFile)
                        .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                        .toList();

                System.out.println("Загружено изображений: " + loadedFiles.size());
            } catch (IOException e) {
                showAlert("Ошибка при загрузке изображений: " + e.getMessage());
            }
        }

        // Добавляем загруженные файлы в изменяемый список
        imageFiles.addAll(loadedFiles);

        // Создаем медиа-элементы
        NeutralReactionFactory factory = new NeutralReactionFactory();
        for (int i = 0; i < imageFiles.size(); i++) {
            mediaItems.add(factory.createMediaItem(imageFiles.get(i), i + 1));
        }

        currentIndex = mediaItems.isEmpty() ? 0 : 0;

        if (!mediaItems.isEmpty()) {
            displayMediaItem(mediaItems.get(0));
        } else {
            displayArea.setImage(null);
            emotionLabel.setText("");
            counterLabel.setText("Нет изображений");
            progressBar.setProgress(0);
        }
        updateProgressDisplay();
    }

    private boolean isImageFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") ||
                name.endsWith(".bmp") || name.endsWith(".webp");
    }

    private void addImageToFolder(Path imagePath) {
        try {
            // Проверяем существование папки
            if (!Files.exists(imagesFolderPath)) {
                Files.createDirectories(imagesFolderPath);
            }

            // Копируем изображение в папку images
            Path targetPath = imagesFolderPath.resolve(imagePath.getFileName());

            // Если файл с таким именем уже существует, добавляем номер
            if (Files.exists(targetPath)) {
                String fileName = imagePath.getFileName().toString();
                int dotIndex = fileName.lastIndexOf('.');
                String nameWithoutExt = fileName.substring(0, dotIndex);
                String ext = fileName.substring(dotIndex);
                int counter = 1;
                while (Files.exists(imagesFolderPath.resolve(nameWithoutExt + "_" + counter + ext))) {
                    counter++;
                }
                targetPath = imagesFolderPath.resolve(nameWithoutExt + "_" + counter + ext);
            }

            Files.copy(imagePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Изображение скопировано в: " + targetPath);

            // Перезагружаем изображения
            loadImagesFromFolder();

            showAlert("Изображение добавлено в галерею!");
        } catch (IOException e) {
            System.err.println("Ошибка при добавлении: " + e.getMessage());
            showAlert("Ошибка при добавлении изображения: " + e.getMessage());
        }
    }

    private void displayMediaItem(MediaItem item) {
        if (item == null || item.getImage() == null) {
            displayArea.setImage(null);
            emotionLabel.setText("");
            return;
        }

        displayArea.setImage(item.getImage());
        String emotion = item.getEmotion();

        String displayEmotion = emotion;
        switch (emotion) {
            case "😊": displayEmotion = "😊 Счастливый"; break;
            case "🎉": displayEmotion = "🎉 Восторженный"; break;
            case "😢": displayEmotion = "😢 Грустный"; break;
            case "😐": displayEmotion = "😐 Нейтральный"; break;
        }

        emotionLabel.setText(emotion);
        emotionSelector.setValue(displayEmotion);

        if (!mediaItems.isEmpty()) {
            double progress = (double) (currentIndex + 1) / mediaItems.size();
            progressBar.setProgress(progress);
        }
    }

    private ReactionFactory getFactoryByEmoji(String emojiText) {
        if (emojiText.contains("😊")) return new HappyReactionFactory();
        if (emojiText.contains("🎉")) return new ExcitedReactionFactory();
        if (emojiText.contains("😢")) return new SadReactionFactory();
        return new NeutralReactionFactory();
    }

    @FXML
    private void onEmotionChanged() {
        if (mediaItems.isEmpty()) return;

        String selected = emotionSelector.getValue();
        ReactionFactory factory = getFactoryByEmoji(selected);

        MediaItem updatedItem = factory.createMediaItem(
                imageFiles.get(currentIndex),
                currentIndex + 1
        );
        mediaItems.set(currentIndex, updatedItem);
        displayMediaItem(updatedItem);
    }

    private void updateProgressDisplay() {
        if (mediaItems.isEmpty()) {
            counterLabel.setText("Нет изображений");
            progressBar.setProgress(0);
            return;
        }

        int current = currentIndex + 1;
        int total = mediaItems.size();
        int remaining = total - current;

        String remainingText;
        if (remaining == 0) {
            remainingText = "последний";
        } else if (remaining == 1) {
            remainingText = "остался 1";
        } else {
            remainingText = "осталось " + remaining;
        }

        counterLabel.setText(String.format("%d из %d - %s", current, total, remainingText));
    }

    private void setupSlideshowTimer() {
        slideshowTimer = new Timeline();
        slideshowTimer.setCycleCount(Timeline.INDEFINITE);
        updateTimerFrame();
    }

    private void updateTimerFrame() {
        slideshowTimer.getKeyFrames().clear();
        KeyFrame frame = new KeyFrame(
                Duration.millis(slideInterval),
                this::onTimerTick
        );
        slideshowTimer.getKeyFrames().add(frame);
    }

    private void onTimerTick(ActionEvent event) {
        Platform.runLater(() -> {
            if (!mediaItems.isEmpty() && isPlaying) {
                currentIndex++;
                if (currentIndex >= mediaItems.size()) {
                    currentIndex = 0;
                }
                displayMediaItem(mediaItems.get(currentIndex));
                updateProgressDisplay();
            }
        });
    }

    @FXML
    private void onNextClick() {
        if (mediaItems.isEmpty()) return;

        if (isPlaying) {
            onStopSlideshow();
        }

        currentIndex++;
        if (currentIndex >= mediaItems.size()) {
            currentIndex = 0;
        }
        displayMediaItem(mediaItems.get(currentIndex));
        updateProgressDisplay();
    }

    @FXML
    private void onPreviousClick() {
        if (mediaItems.isEmpty()) return;

        if (isPlaying) {
            onStopSlideshow();
        }

        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = mediaItems.size() - 1;
        }
        displayMediaItem(mediaItems.get(currentIndex));
        updateProgressDisplay();
    }

    @FXML
    private void onStartSlideshow() {
        if (!mediaItems.isEmpty() && !isPlaying) {
            isPlaying = true;
            slideshowTimer.play();
            updatePlayButtonStyle();
        }
    }

    @FXML
    private void onStopSlideshow() {
        if (isPlaying) {
            isPlaying = false;
            slideshowTimer.stop();
            updatePlayButtonStyle();
        }
    }

    private void updatePlayButtonStyle() {
        if (isPlaying) {
            playButton.setStyle("-fx-background-color: linear-gradient(to bottom, #ff6b6b, #ff5252); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 24px; " +
                    "-fx-background-radius: 40; -fx-cursor: hand; -fx-min-width: 80; -fx-min-height: 80;");
        } else {
            playButton.setStyle("-fx-background-color: linear-gradient(to bottom, #4a5e7a, #2a3a5a); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 24px; " +
                    "-fx-background-radius: 40; -fx-cursor: hand; -fx-min-width: 80; -fx-min-height: 80;");
        }
    }

    @FXML
    private void onApplyInterval() {
        try {
            double value = Double.parseDouble(intervalInput.getText());
            if (value > 0 && value <= 10000) {
                slideInterval = value;
                updateTimerFrame();
                if (isPlaying) {
                    slideshowTimer.stop();
                    slideshowTimer.play();
                }
            } else {
                intervalInput.setText(String.valueOf((int)slideInterval));
                showAlert("Введите значение от 1 до 10000 мс");
            }
        } catch (NumberFormatException e) {
            intervalInput.setText(String.valueOf((int)slideInterval));
            showAlert("Введите корректное число");
        }
    }

    @FXML
    private void onAddImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Изображения", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.webp"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (selectedFile != null) {
            System.out.println("Выбран файл: " + selectedFile.getAbsolutePath());
            addImageToFolder(selectedFile.toPath());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}