package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    public static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Comic Downloader");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();

        stage = primaryStage;
    }


    public static void main(String[] args) {
        launch(args);
    }
}