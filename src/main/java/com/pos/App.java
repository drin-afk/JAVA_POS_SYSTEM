package com.pos;
import com.pos.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
@Override

public void start(Stage stage) {
Label title = new Label("POS Database Connection Test");
Label status = new Label("Click the button to test JDBC.");
Button testButton = new Button("Test Database Connection");
testButton.setOnAction(event -> {
String sql = "SELECT COUNT(*) AS total FROM products";
try (Connection conn = DatabaseConnection.getConnection();
PreparedStatement stmt = conn.prepareStatement(sql);
ResultSet rs = stmt.executeQuery()) {
if (rs.next()) {
int total = rs.getInt("total");

status.setText("Connected! Products in database: "

+ total);
}
} catch (SQLException e) {
status.setText("Connection failed: " + e.getMessage());
}
});
VBox root = new VBox(12, title, status, testButton);
root.setPadding(new Insets(25));
Scene scene = new Scene(root, 460, 180);
stage.setTitle("POS System - Laboratory 4");
stage.setScene(scene);
stage.show();
}
public static void main(String[] args) {
launch(args);
}
}
