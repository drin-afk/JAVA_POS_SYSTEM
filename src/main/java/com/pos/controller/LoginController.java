package com.pos.controller;
import com.pos.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
public class LoginController {
@FXML
private TextField usernameField;
@FXML
private PasswordField passwordField;
@FXML
private Button loginButton;
@FXML
private Button clearButton;
@FXML
private Label messageLabel;
@FXML






private void handleLogin() {
    String username = usernameField.getText().trim();
    String password = passwordField.getText();

    // Validate empty fields
    if (username.isEmpty() && password.isEmpty()) {
        messageLabel.setText("Please enter username and password.");
        return;
    }

    if (username.isEmpty()) {
        messageLabel.setText("Please enter your username.");
        usernameField.requestFocus();
        return;
    }

    if (password.isEmpty()) {
        messageLabel.setText("Please enter your password.");
        passwordField.requestFocus();
        return;
    }

    String sql = "SELECT user_id, full_name, role "
               + "FROM users "
               + "WHERE username = ? AND password_hash = ? "
               + "AND status = 'Active'";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, username);
        ps.setString(2, password);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String fullName = rs.getString("full_name");
                String role = rs.getString("role");

                messageLabel.setText("");
                showDashboard(userId, fullName, role);

            } else {
                messageLabel.setText(
                    "Invalid username/password or inactive account."
                );
                passwordField.clear();
                passwordField.requestFocus();
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
        messageLabel.setText(
            "Database error: " + e.getMessage()
        );
    }
}




@FXML
private void handleClear() {
    usernameField.clear();
    passwordField.clear();
    messageLabel.setText("");
}

private void showDashboard(int userId, String fullName, String role) {
    
Stage stage = (Stage) loginButton.getScene().getWindow();
Label title = new Label("POS SYSTEM");
title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
Label welcome = new Label("Welcome, " + fullName + "!");
Label info = new Label(
"User ID: " + userId + " | Role: " + role
);

Label access = new Label();
if (role.equals("Owner") || role.equals("Administrator")) {
    access.setText("Administrator access");
} else if (role.equals("Cashier")) {
    access.setText("Cashier access");
} else {
    access.setText(role + " access");
}


Button logoutButton = new Button("Logout");
VBox dashboard = new VBox(15, title, welcome, info, logoutButton);
dashboard.setAlignment(Pos.CENTER);
dashboard.setPadding(new Insets(30));
Scene dashboardScene = new Scene(dashboard, 600, 400);
Scene loginScene = loginButton.getScene();
// Return to the same login scene when Logout is clicked.
logoutButton.setOnAction(event -> {
usernameField.clear();
passwordField.clear();
messageLabel.setText("");
stage.setTitle("POS System - Login");
stage.setScene(loginScene);
stage.setResizable(false);
usernameField.requestFocus();
});
stage.setTitle("POS System - Dashboard");
stage.setScene(dashboardScene);
stage.setResizable(true);
}
}
