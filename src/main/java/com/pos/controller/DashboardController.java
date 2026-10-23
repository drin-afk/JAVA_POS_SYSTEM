package com.pos.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label userInfoLabel;

    // Exercise 1: shows the current date
    @FXML
    private Label dateLabel;

    // Header user chip
    @FXML
    private Label userNameLabel;

    @FXML
    private Label roleBadgeLabel;

    @FXML
    private Label initialsLabel;

    private int userId;
    private String fullName;
    private String role;

    // Runs automatically after dashboard.fxml is loaded.
    @FXML
    private void initialize() {
        dateLabel.setText("Date: " + LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
    }

    public void setLoggedInUser(int userId,
                                String fullName,
                                String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.role = role;

        // Exercise 4: personalized welcome message (if / else)
        if (isAdministrator()) {
            welcomeLabel.setText("Welcome, Administrator: " + fullName);
        } else if (role.equals("Cashier")) {
            welcomeLabel.setText("Welcome, Cashier: " + fullName);
        } else {
            welcomeLabel.setText("Welcome, " + role + ": " + fullName);
        }

        userInfoLabel.setText(
                "User ID: " + userId + " | Role: " + role
        );

        // Header user chip: name, role badge, and initials avatar
        userNameLabel.setText(fullName);
        roleBadgeLabel.setText(getDisplayRole().toUpperCase());
        initialsLabel.setText(getInitials(fullName));
    }

    private String getInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        String initials = parts[0].substring(0, 1);
        if (parts.length > 1) {
            initials += parts[parts.length - 1].substring(0, 1);
        }
        return initials.toUpperCase();
    }

    // In pastry_shop_db the administrator account has the role "Owner".
    private boolean isAdministrator() {
        return role.equals("Administrator") || role.equals("Owner");
    }

    // Exercise 5: used by LoginController for the window title.
    public String getDisplayRole() {
        return isAdministrator() ? "Administrator" : role;
    }

    @FXML
    private void openSales() {
        showModuleMessage(
                "POS / Sales",
                "The Sales module will be developed in Laboratory 9."
        );
    }

    @FXML
    private void openProducts() {
        showModuleMessage(
                "Product Management",
                "The Product Management module will be developed in Laboratory 8."
        );
    }

    // Exercise 3: INVENTORY placeholder
    @FXML
    private void openInventory() {
        showModuleMessage(
                "Inventory",
                "Inventory features will be integrated with Product Management."
        );
    }

    // Laboratory 7: opens the real User Management window (Administrator only).
    @FXML
    private void openUsers() {
        if (!isAdministrator()) {
            showModuleMessage(
                    "Access Denied",
                    "Only an Administrator can manage user accounts."
            );
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/pos/view/user_management.fxml"
                    )
            );

            Scene scene = new Scene(loader.load(), 1000, 650);

            UserManagementController controller = loader.getController();
            controller.setLoggedInUserId(userId);

            Stage userStage = new Stage();
            userStage.setTitle("POS System - User Management");
            userStage.initOwner(welcomeLabel.getScene().getWindow());
            userStage.setScene(scene);
            userStage.centerOnScreen();
            userStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showModuleMessage(
                    "Navigation Error",
                    "Unable to open User Management."
            );
        }
    }

    @FXML
    private void openHistory() {
        showModuleMessage(
                "Transaction History",
                "Transaction History will be developed in Laboratory 11."
        );
    }

    @FXML
    private void openReports() {
        showModuleMessage(
                "Reports",
                "Report Generation will be developed in Laboratory 12."
        );
    }

    private void showModuleMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Do you want to logout?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirm.setTitle("Logout");
        confirm.setHeaderText("Confirm Logout");
        styleDialog(confirm);

        if (confirm.showAndWait().orElse(ButtonType.NO)
                == ButtonType.YES) {
            openLoginScreen();
        }
    }

    private void openLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/pos/view/login.fxml")
            );

            // No fixed size: the login screen uses its own 860 x 520 layout.
            Scene loginScene = new Scene(loader.load());
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();

            stage.setTitle("POS System - Login");
            stage.setScene(loginScene);
            stage.setResizable(false);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showModuleMessage(
                    "Navigation Error",
                    "Unable to return to the Login screen."
            );
        }
    }

    // Applies the shared pastry shop dialog style (pos.css).
    private void styleDialog(Alert alert) {
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/pos/view/pos.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("pos-dialog");
    }
}
