package com.example.studentregistrationsystem;

import datamodel.StudentGroupData;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("mainwindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Student Registration System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws Exception {
        try {
            StudentGroupData.getInstance().storeStudentGroups();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void init() throws Exception {
        try {
            StudentGroupData.getInstance().loadStudentGroups();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}