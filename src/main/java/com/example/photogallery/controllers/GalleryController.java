package com.example.photogallery.controllers;

import com.example.photogallery.factories.*;
import com.example.photogallery.models.*;
import com.example.photogallery.utils.ProgressDirector;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class GalleryController {

    @FXML private ImageView displayArea;
    @FXML private StackPane imageContainer;
    @FXML private TextField hashtagInput;
    @FXML private Button addHashtagButton;
    @FXML private Button playButton;
    @FXML private Button stopButton;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button firstButton;
    @FXML private Button lastButton;
    @FXML private Button addImageButton;
    @FXML private Button reverseOrderButton;
    @FXML private Button musicButton;
    @FXML private TextField intervalInput;
    @FXML private Label counterLabel;
    @FXML private ComboBox<String> emotionSelector;
    @FXML private Slider volumeSlider;
    @FXML private Label volumeLabel;
    @FXML private Button applyIntervalButton;

    // ИСПОЛЬЗУЕМ ПАТТЕРНЫ
    private MediaCollection mediaCollection;
    private MediaIterator iterator;
    private ProgressDirector progressDirector;
    private ProgressBuilder progressBuilder;

    private boolean reverseOrder = false;
    private Timeline slideshowTimer;
    private double slideInterval = 2000;
    private boolean isPlaying = false;
    private Path imagesFolderPath;
    private Path dataFilePath;
    private MediaPlayer mediaPlayer;
    private boolean isMusicPlaying = false;

    // Оверлеи для фото
    private Label emotionOverlay;
    private FlowPane hashtagsOverlay;

    @FXML
    public void initialize() {
        imagesFolderPath = Paths.get("src/main/resources/images");
        dataFilePath = Paths.get("src/main/resources/slideshow.dat");

        try {
            if (!Files.exists(imagesFolderPath)) {
                Files.createDirectories(imagesFolderPath);
            }
        } catch (IOException e) {
            System.err.println("Не удалось создать папку images");
        }

        // ИНИЦИАЛИЗАЦИЯ ПАТТЕРНОВ
        progressDirector = new ProgressDirector();
        progressBuilder = new SimpleProgressBuilder();
        mediaCollection = new MediaCollection();

        setupUI();
        setupIcons();
        setupSlideshowTimer();
        setupMusic();

        Platform.runLater(() -> {
            createPhotoOverlays();
            loadImagesFromFolder();
            loadSlideShowData();
        });
    }

    private void createPhotoOverlays() {
        if (displayArea == null || imageContainer == null) {
            return;
        }


        // Реакция
        int reactionX = 710;      // смещение по горизонтали
        int reactionY = -390;     // смещение по вертикали

        // Хэштеги
        int hashtagsX = -710;     // смещение по горизонтали
        int hashtagsY = 190;     // смещение по вертикали



        emotionOverlay = new Label();
        emotionOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-background-radius: 20; -fx-padding: 4 10; -fx-font-size: 28px; -fx-text-fill: white;");
        emotionOverlay.setAlignment(Pos.CENTER);
        emotionOverlay.setTranslateX(reactionX);
        emotionOverlay.setTranslateY(reactionY);


        hashtagsOverlay = new FlowPane();
        hashtagsOverlay.setHgap(6);
        hashtagsOverlay.setVgap(4);
        hashtagsOverlay.setAlignment(Pos.CENTER_RIGHT);
        hashtagsOverlay.setTranslateX(hashtagsX);
        hashtagsOverlay.setTranslateY(hashtagsY);


        StackPane photoStack = new StackPane();
        photoStack.getChildren().addAll(displayArea, emotionOverlay, hashtagsOverlay);


        StackPane.setAlignment(emotionOverlay, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(hashtagsOverlay, Pos.BOTTOM_RIGHT);

        imageContainer.getChildren().clear();
        imageContainer.getChildren().add(photoStack);
        imageContainer.setStyle("-fx-background-color: transparent; -fx-background-radius: 20; -fx-padding: 12;");
    }

    private void setupUI() {
        emotionSelector.getItems().addAll("😊", "🎉", "😢", "😐");
        emotionSelector.setValue("😐");
        emotionSelector.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 18px; -fx-background-radius: 20;");

        intervalInput.setText("2000");
        intervalInput.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 10;");
        hashtagInput.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 6 12;");

        volumeSlider.setValue(50);
        volumeLabel.setText("50%");
        volumeLabel.setStyle("-fx-text-fill: #e0e0e0;");
        volumeSlider.valueProperty().addListener((obs, old, val) -> {
            volumeLabel.setText(val.intValue() + "%");
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(val.doubleValue() / 100);
            }
        });

        String actionButtonStyle = "-fx-background-color: linear-gradient(to bottom, #4a5e7a, #2a3a5a); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-background-radius: 25; -fx-padding: 8 15; -fx-cursor: hand;";

        addImageButton.setStyle(actionButtonStyle);
        reverseOrderButton.setStyle(actionButtonStyle);
        applyIntervalButton.setStyle(actionButtonStyle);
        musicButton.setStyle(actionButtonStyle);
        musicButton.setText("🎵");
        addHashtagButton.setStyle(actionButtonStyle);
        addHashtagButton.setText("➕");

        String navButtonStyle = "-fx-background-color: linear-gradient(to bottom, #4a5e7a, #2a3a5a); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; " +
                "-fx-background-radius: 30; -fx-cursor: hand; -fx-min-width: 55; -fx-min-height: 55;";

        firstButton.setStyle(navButtonStyle);
        prevButton.setStyle(navButtonStyle);
        playButton.setStyle(navButtonStyle);
        stopButton.setStyle(navButtonStyle);
        nextButton.setStyle(navButtonStyle);
        lastButton.setStyle(navButtonStyle);

        counterLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px; -fx-font-weight: bold;");
    }

    private void setupIcons() {
        try {
            java.net.URL folderUrl = getClass().getResource("/icons/folder.png");
            if (folderUrl != null) {
                ImageView folderIcon = new ImageView(new Image(folderUrl.toExternalForm()));
                folderIcon.setFitHeight(18);
                folderIcon.setFitWidth(18);
                addImageButton.setGraphic(folderIcon);
                addImageButton.setText(" Добавить изображение");
            }

            java.net.URL reverseUrl = getClass().getResource("/icons/reverse.png");
            if (reverseUrl != null) {
                ImageView reverseIcon = new ImageView(new Image(reverseUrl.toExternalForm()));
                reverseIcon.setFitHeight(18);
                reverseIcon.setFitWidth(18);
                reverseOrderButton.setGraphic(reverseIcon);
                reverseOrderButton.setText(" Обратный порядок");
            }

            java.net.URL backUrl = getClass().getResource("/icons/back.png");
            if (backUrl != null) {
                ImageView backIcon = new ImageView(new Image(backUrl.toExternalForm()));
                backIcon.setFitHeight(20);
                backIcon.setFitWidth(20);
                prevButton.setGraphic(backIcon);
                prevButton.setText("");
            }

            java.net.URL playUrl = getClass().getResource("/icons/play.png");
            if (playUrl != null) {
                ImageView playIcon = new ImageView(new Image(playUrl.toExternalForm()));
                playIcon.setFitHeight(20);
                playIcon.setFitWidth(20);
                playButton.setGraphic(playIcon);
                playButton.setText("");
            }

            java.net.URL stopUrl = getClass().getResource("/icons/stop.png");
            if (stopUrl != null) {
                ImageView stopIcon = new ImageView(new Image(stopUrl.toExternalForm()));
                stopIcon.setFitHeight(20);
                stopIcon.setFitWidth(20);
                stopButton.setGraphic(stopIcon);
                stopButton.setText("");
            }

            java.net.URL forwardUrl = getClass().getResource("/icons/forward.png");
            if (forwardUrl != null) {
                ImageView forwardIcon = new ImageView(new Image(forwardUrl.toExternalForm()));
                forwardIcon.setFitHeight(20);
                forwardIcon.setFitWidth(20);
                nextButton.setGraphic(forwardIcon);
                nextButton.setText("");
            }

            java.net.URL firstUrl = getClass().getResource("/icons/first.png");
            if (firstUrl != null) {
                ImageView firstIcon = new ImageView(new Image(firstUrl.toExternalForm()));
                firstIcon.setFitHeight(20);
                firstIcon.setFitWidth(20);
                firstButton.setGraphic(firstIcon);
                firstButton.setText("");
            }

            java.net.URL lastUrl = getClass().getResource("/icons/last.png");
            if (lastUrl != null) {
                ImageView lastIcon = new ImageView(new Image(lastUrl.toExternalForm()));
                lastIcon.setFitHeight(20);
                lastIcon.setFitWidth(20);
                lastButton.setGraphic(lastIcon);
                lastButton.setText("");
            }

            java.net.URL volumeUrl = getClass().getResource("/icons/volume.png");
            if (volumeUrl != null) {
                ImageView volumeIcon = new ImageView(new Image(volumeUrl.toExternalForm()));
                volumeIcon.setFitHeight(18);
                volumeIcon.setFitWidth(18);
                musicButton.setGraphic(volumeIcon);
                musicButton.setText("");
            }

            java.net.URL addUrl = getClass().getResource("/icons/add.png");
            if (addUrl != null) {
                ImageView addIcon = new ImageView(new Image(addUrl.toExternalForm()));
                addIcon.setFitHeight(18);
                addIcon.setFitWidth(18);
                addHashtagButton.setGraphic(addIcon);
                addHashtagButton.setText("");
            }

        } catch (Exception e) {
            System.out.println("Иконки не загружены");
        }
    }

    private void setupMusic() {
        try {
            Path musicPath = Paths.get("src/main/resources/music/background.mp3");
            if (Files.exists(musicPath)) {
                Media media = new Media(musicPath.toUri().toString());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setVolume(0.5);
            }
        } catch (Exception e) {
            System.out.println("Музыка не найдена");
        }
    }

    private void loadImagesFromFolder() {
        mediaCollection.loadFromDirectory(imagesFolderPath);
        iterator = mediaCollection.createIterator();
        updateDisplay();
    }

    private boolean isImageFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") ||
                name.endsWith(".bmp") || name.endsWith(".webp");
    }

    private void updateDisplay() {
        if (mediaCollection.isEmpty()) {
            displayArea.setImage(null);
            counterLabel.setText("0 из 0");
            if (hashtagsOverlay != null) hashtagsOverlay.getChildren().clear();
            if (emotionOverlay != null) emotionOverlay.setText("");
            return;
        }

        MediaItem current = mediaCollection.getItem(iterator.getCurrentIndex());
        if (current != null && current.getImage() != null) {
            displayArea.setImage(current.getImage());

            // ИСПОЛЬЗУЕМ СТРОИТЕЛЬ ДЛЯ ПРОГРЕССА
            int currentNum = iterator.getCurrentIndex() + 1;
            int total = mediaCollection.size();
            ProgressStatus status = progressDirector.build(progressBuilder, currentNum, total);
            counterLabel.setText(status.toString());

            updateHashtagsOnPhoto(current);
            if (emotionOverlay != null) emotionOverlay.setText(current.getEmotion());
            emotionSelector.setValue(current.getEmotion());
        }
    }

    private void updateHashtagsOnPhoto(MediaItem item) {
        if (hashtagsOverlay == null) return;

        hashtagsOverlay.getChildren().clear();

        if (item.getHashtags().isEmpty()) {
            return;
        }

        for (String tag : item.getHashtags()) {
            HBox tagBox = new HBox(3);
            tagBox.setAlignment(Pos.CENTER);
            tagBox.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-background-radius: 12; -fx-padding: 2 8;");

            Label tagLabel = new Label("#" + tag);
            tagLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");

            Button deleteBtn = new Button("✕");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff8888; -fx-font-size: 9px; -fx-cursor: hand; -fx-padding: 0;");
            deleteBtn.setVisible(false);

            tagBox.setOnMouseEntered(e -> deleteBtn.setVisible(true));
            tagBox.setOnMouseExited(e -> deleteBtn.setVisible(false));
            deleteBtn.setOnAction(e -> {
                item.removeHashtag(tag);
                updateHashtagsOnPhoto(item);
                saveSlideShowData();
            });

            tagBox.getChildren().addAll(tagLabel, deleteBtn);
            hashtagsOverlay.getChildren().add(tagBox);
        }
    }

    @FXML
    private void onAddHashtag() {
        String hashtag = hashtagInput.getText().trim();
        if (!hashtag.isEmpty() && !mediaCollection.isEmpty()) {
            MediaItem current = mediaCollection.getItem(iterator.getCurrentIndex());
            current.addHashtag(hashtag);
            hashtagInput.clear();
            updateHashtagsOnPhoto(current);
            saveSlideShowData();
        }
    }

    @FXML
    private void onEmotionChanged() {
        if (!mediaCollection.isEmpty()) {
            String emotion = emotionSelector.getValue();
            MediaItem current = mediaCollection.getItem(iterator.getCurrentIndex());
            current.setEmotion(emotion);
            if (emotionOverlay != null) emotionOverlay.setText(emotion);
            saveSlideShowData();
        }
    }

    @FXML
    private void onAddImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Изображения", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.webp")
        );

        File selectedFile = fileChooser.showOpenDialog(imageContainer.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Path targetPath = imagesFolderPath.resolve(selectedFile.getName());
                if (Files.exists(targetPath)) {
                    String name = selectedFile.getName();
                    int dotIndex = name.lastIndexOf('.');
                    String base = name.substring(0, dotIndex);
                    String ext = name.substring(dotIndex);
                    int counter = 1;
                    while (Files.exists(imagesFolderPath.resolve(base + "_" + counter + ext))) {
                        counter++;
                    }
                    targetPath = imagesFolderPath.resolve(base + "_" + counter + ext);
                }

                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                Image image = new Image(targetPath.toUri().toString());

                NeutralReactionFactory factory = new NeutralReactionFactory();
                MediaItem newItem = factory.createMediaItem(targetPath, mediaCollection.size());
                mediaCollection.addItem(newItem);
                iterator = mediaCollection.createIterator();
                updateDisplay();
                saveSlideShowData();
            } catch (IOException e) {
                showAlert("Ошибка при добавлении изображения");
            }
        }
    }

    @FXML
    private void onReverseOrder() {
        if (!mediaCollection.isEmpty()) {
            reverseOrder = !reverseOrder;
            updateDisplay();
        }
    }


    @FXML
    private void onFirstSlide() {
        if (!mediaCollection.isEmpty()) {
            if (isPlaying) onStopSlideshow();
            iterator.first();
            updateDisplay();
        }
    }

    @FXML
    private void onLastSlide() {
        if (!mediaCollection.isEmpty()) {
            if (isPlaying) onStopSlideshow();
            iterator.last();
            updateDisplay();
        }
    }

    @FXML
    private void onNextClick() {
        if (!mediaCollection.isEmpty()) {
            if (isPlaying) onStopSlideshow();
            if (reverseOrder) {
                if (iterator.hasPrevious()) {
                    iterator.previous();
                } else {
                    iterator.last();
                }
            } else {
                if (iterator.hasNext()) {
                    iterator.next();
                } else {
                    iterator.first();
                }
            }
            updateDisplay();
        }
    }

    @FXML
    private void onPreviousClick() {
        if (!mediaCollection.isEmpty()) {
            if (isPlaying) onStopSlideshow();
            if (reverseOrder) {
                if (iterator.hasNext()) {
                    iterator.next();
                } else {
                    iterator.first();
                }
            } else {
                if (iterator.hasPrevious()) {
                    iterator.previous();
                } else {
                    iterator.last();
                }
            }
            updateDisplay();
        }
    }

    @FXML
    private void onStartSlideshow() {
        if (!mediaCollection.isEmpty() && !isPlaying) {
            isPlaying = true;
            slideshowTimer.play();
            playButton.setStyle("-fx-background-color: linear-gradient(to bottom, #ff6b6b, #ff5252); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; " +
                    "-fx-background-radius: 30; -fx-cursor: hand; -fx-min-width: 55; -fx-min-height: 55;");
        }
    }

    @FXML
    private void onStopSlideshow() {
        if (isPlaying) {
            isPlaying = false;
            slideshowTimer.stop();
            playButton.setStyle("-fx-background-color: linear-gradient(to bottom, #4a5e7a, #2a3a5a); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; " +
                    "-fx-background-radius: 30; -fx-cursor: hand; -fx-min-width: 55; -fx-min-height: 55;");
        }
    }

    @FXML
    private void onToggleMusic() {
        if (mediaPlayer != null) {
            if (isMusicPlaying) {
                mediaPlayer.pause();
                isMusicPlaying = false;
            } else {
                mediaPlayer.play();
                isMusicPlaying = true;
            }
        } else {
            showAlert("Музыкальный файл не найден");
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
                saveSlideShowData();
            } else {
                intervalInput.setText(String.valueOf((int) slideInterval));
                showAlert("Введите значение от 1 до 10000 мс");
            }
        } catch (NumberFormatException e) {
            intervalInput.setText(String.valueOf((int) slideInterval));
            showAlert("Введите корректное число");
        }
    }

    private void setupSlideshowTimer() {
        slideshowTimer = new Timeline();
        slideshowTimer.setCycleCount(Timeline.INDEFINITE);
        updateTimerFrame();
    }

    private void updateTimerFrame() {
        slideshowTimer.getKeyFrames().clear();
        KeyFrame frame = new KeyFrame(Duration.millis(slideInterval), this::onTimerTick);
        slideshowTimer.getKeyFrames().add(frame);
    }

    private void onTimerTick(ActionEvent event) {
        Platform.runLater(() -> {
            if (!mediaCollection.isEmpty() && isPlaying) {
                if (reverseOrder) {
                    if (iterator.hasPrevious()) {
                        iterator.previous();
                    } else {
                        iterator.last();
                    }
                } else {
                    if (iterator.hasNext()) {
                        iterator.next();
                    } else {
                        iterator.first();
                    }
                }
                updateDisplay();
            }
        });
    }

    private void saveSlideShowData() {
        try {
            List<SlideData> slidesData = new ArrayList<>();
            for (int i = 0; i < mediaCollection.size(); i++) {
                MediaItem item = mediaCollection.getItem(i);
                slidesData.add(new SlideData(
                        item.getImagePath(),
                        item.getEmotion(),
                        item.getHashtags(),
                        new ArrayList<>(),
                        i
                ));
            }

            SlideShowData data = new SlideShowData(slidesData, slideInterval, "My Slideshow");

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFilePath.toFile()))) {
                oos.writeObject(data);
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения");
        }
    }

    private void loadSlideShowData() {
        if (Files.exists(dataFilePath)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFilePath.toFile()))) {
                SlideShowData data = (SlideShowData) ois.readObject();

                mediaCollection = new MediaCollection();
                for (SlideData slideData : data.getSlides()) {
                    File imageFile = new File(slideData.getImagePath());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        MediaItem item = new MediaItem(image, slideData.getImagePath(), slideData.getEmotion(), slideData.getOrder());

                        for (String tag : slideData.getHashtags()) {
                            item.addHashtag(tag);
                        }
                        mediaCollection.addItem(item);
                    }
                }

                slideInterval = data.getSlideInterval();
                intervalInput.setText(String.valueOf((int) slideInterval));
                updateTimerFrame();

                iterator = mediaCollection.createIterator();
                updateDisplay();
            } catch (Exception e) {
                System.err.println("Ошибка загрузки");
            }
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