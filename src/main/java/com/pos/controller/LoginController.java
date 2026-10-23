package com.pos.controller;

import com.pos.database.DatabaseConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    @FXML
    private Button clearButton;

    // Called by the CLEAR button (onAction="#handleClear" in login.fxml).
    @FXML
    private void handleClear() {
        usernameField.clear();
        passwordField.clear();
        messageLabel.setText("");
        usernameField.requestFocus();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // 1. Validate the form first.
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        // 2. Find the Active account by username. pastry_shop_db stores a BCrypt
        //    hash (password_hash), so the password is checked in Java.
        String sql = "SELECT user_id, full_name, role, password_hash "
                + "FROM users "
                + "WHERE username = ? AND status = 'Active'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && passwordMatches(password, rs.getString("password_hash"))) {
                    int userId = rs.getInt("user_id");
                    String fullName = rs.getString("full_name");
                    String role = rs.getString("role");

                    messageLabel.setText("");
                    openDashboard(userId, fullName, role);
                } else {
                    messageLabel.setText(
                            "Invalid username/password or inactive account."
                    );
                    passwordField.clear();
                    passwordField.requestFocus();
                }
            }
        } catch (SQLException e) {
            messageLabel.setText("Database error. Please contact the instructor.");
            e.printStackTrace();
        }
    }

    // BCrypt.checkpw throws IllegalArgumentException ("Invalid salt version")
    // when the stored value is not a real BCrypt hash (e.g. a plain-text
    // password typed directly into phpMyAdmin). Treat that as a failed login
    // instead of letting the exception escape the button handler.
    private boolean passwordMatches(String password, String storedHash) {
        if (storedHash == null || !storedHash.startsWith("$2")) {
            System.err.println("Stored password is not a BCrypt hash. "
                    + "Re-save the user's password from User Management.");
            return false;
        }
        try {
            return BCrypt.checkpw(password, storedHash);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid BCrypt hash in database: " + e.getMessage());
            return false;
        }
    }

    // Replaces the temporary showDashboard(...) method from Laboratory 5.
    private void openDashboard(int userId,
                               String fullName,
                               String role) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/pos/view/dashboard.fxml")
            );

            Scene dashboardScene = new Scene(loader.load(), 1000, 640);

            // getController() is called after load(), because the controller
            // is created while the FXML file is being loaded.
            DashboardController dashboardController = loader.getController();
            dashboardController.setLoggedInUser(
                    userId,
                    fullName,
                    role
            );

            Stage stage = (Stage) loginButton.getScene().getWindow();
            // Exercise 5: the role is part of the window title.
            stage.setTitle("POS System - Dashboard - "
                    + dashboardController.getDisplayRole());
            stage.setScene(dashboardScene);
            stage.setResizable(true);
            stage.centerOnScreen();

        } catch (IOException e) {
            messageLabel.setText("Unable to open dashboard.");
            e.printStackTrace();
        }
    }
}
