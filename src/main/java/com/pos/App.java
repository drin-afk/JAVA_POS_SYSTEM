package com.pos;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
@Override
public void start(Stage stage) {
Label message = new Label("Aldrin Joseph D. Dinglasan");
Button testButton = new Button("Start POS Project");
testButton.setOnAction(event ->
message.setText("Welcome to the POS Laboratory!"));
VBox root = new VBox(15, message, testButton);
root.setPadding(new Insets(25));
root.setAlignment(Pos.CENTER);
Scene scene = new Scene(root, 450, 220);
stage.setTitle("BSCS 3B");
stage.setScene(scene);
stage.show();
}
public static void main(String[] args) {
launch(args);
}
}