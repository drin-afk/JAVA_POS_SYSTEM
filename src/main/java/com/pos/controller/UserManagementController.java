package com.pos.controller;

import com.pos.database.DatabaseConnection;
import com.pos.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.mindrot.jbcrypt.BCrypt;

public class UserManagementController {

    // Exercise 3: minimum password length for new accounts.
    private static final int MIN_PASSWORD_LENGTH = 6;

    // Challenge: CSS pseudo-class used to style Inactive rows (see pos.css).
    private static final PseudoClass INACTIVE = PseudoClass.getPseudoClass("inactive");

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField fullNameField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label messageLabel;

    // Exercise 1: Search Users
    @FXML private TextField searchField;
    // Exercise 2: Active User Counter
    @FXML private Label activeCountLabel;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;

    private int loggedInUserId;
    private User selectedUser;

    // All users loaded from the database. The TableView shows a filtered
    // (Exercise 1) and sortable view of this list.
    private final ObservableList<User> masterUsers = FXCollections.observableArrayList();
    private FilteredList<User> filteredUsers;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // pastry_shop_db roles: Owner (administrator access), Cashier, Baker
        roleComboBox.getItems().addAll("Owner", "Cashier", "Baker");
        // Shows "Select a role" until a role is chosen.
        roleComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select a role" : item);
            }
        });
        statusComboBox.getItems().addAll("Active", "Inactive");
        statusComboBox.setValue("Active");

        // Exercise 1: FilteredList wraps the master list; the predicate is
        // updated every time the search text changes. SortedList keeps
        // column-header sorting working on the filtered result.
        filteredUsers = new FilteredList<>(masterUsers, user -> true);
        searchField.textProperty().addListener(
                (observable, oldText, newText) -> applySearchFilter(newText));

        SortedList<User> sortedUsers = new SortedList<>(filteredUsers);
        sortedUsers.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedUsers);

        // Exercise 2: recount whenever the displayed list changes
        // (reload from the database or a new search).
        filteredUsers.addListener((ListChangeListener<User>) change -> updateActiveCount());

        // Challenge: TableRow factory. Each row toggles the ":inactive"
        // pseudo-class depending on the user's status, so pos.css can
        // style Inactive accounts differently from Active ones.
        userTable.setRowFactory(table -> new TableRow<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                boolean inactive = !empty && user != null
                        && "Inactive".equalsIgnoreCase(user.getStatus());
                pseudoClassStateChanged(INACTIVE, inactive);
            }
        });

        userTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectedUser = newValue;
                        fillForm(newValue);
                    }
                }
        );

        loadUsers();
    }

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    private void loadUsers() {
        String sql = "SELECT user_id, username, full_name, role, status "
                + "FROM users ORDER BY user_id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ObservableList<User> users = FXCollections.observableArrayList();
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }

            // Replace the contents (not the list itself) so the
            // FilteredList / SortedList / search keep working.
            masterUsers.setAll(users);
            updateActiveCount();
            messageLabel.setText("");

        } catch (SQLException e) {
            messageLabel.setText("Unable to load users: " + e.getMessage());
        }
    }

    // Exercise 1: show only users whose username or full name contains
    // the search text (case-insensitive). Empty search shows everyone.
    private void applySearchFilter(String text) {
        String keyword = text == null ? "" : text.trim().toLowerCase();

        filteredUsers.setPredicate(user -> {
            if (keyword.isEmpty()) {
                return true;
            }
            return user.getUsername().toLowerCase().contains(keyword)
                    || user.getFullName().toLowerCase().contains(keyword);
        });
    }

    // Exercise 2: number of Active accounts currently shown in the TableView.
    private void updateActiveCount() {
        long active = filteredUsers.stream()
                .filter(user -> "Active".equalsIgnoreCase(user.getStatus()))
                .count();
        activeCountLabel.setText("Active accounts: " + active);
    }

    @FXML
    private void handleAdd() {
        if (!validateFields(true)) {
            return;
        }

        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String fullName = fullNameField.getText().trim();
        String role = roleComboBox.getValue();
        String status = statusComboBox.getValue();

        if (usernameExists(username, 0)) {
            messageLabel.setText("Username already exists.");
            return;
        }

        // pastry_shop_db stores a BCrypt hash in password_hash, never the plain password.
        String sql = "INSERT INTO users "
                + "(username, password_hash, full_name, role, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
            ps.setString(3, fullName);
            ps.setString(4, role);
            ps.setString(5, status);

            ps.executeUpdate();

            showInformation("Success", "User account added successfully.");
            loadUsers();
            clearForm();

        } catch (SQLException e) {
            messageLabel.setText("Unable to add user: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedUser == null) {
            messageLabel.setText("Select a user from the table first.");
            return;
        }

        if (!validateFields(false)) {
            return;
        }

        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String fullName = fullNameField.getText().trim();
        String role = roleComboBox.getValue();
        String status = statusComboBox.getValue();

        if (selectedUser.getUserId() == loggedInUserId
                && "Inactive".equals(status)) {
            messageLabel.setText("You cannot deactivate the account currently in use.");
            return;
        }

        if (usernameExists(username, selectedUser.getUserId())) {
            messageLabel.setText("Username is already used by another account.");
            return;
        }

        boolean changePassword = !password.isBlank();

        // Exercise 4: confirm before changing an existing account.
        if (!confirmUpdate(username, fullName, role, status, changePassword)) {
            messageLabel.setText("Update cancelled.");
            return;
        }

        String sql;

        if (changePassword) {
            sql = "UPDATE users SET username=?, password_hash=?, full_name=?, "
                    + "role=?, status=? WHERE user_id=?";
        } else {
            sql = "UPDATE users SET username=?, full_name=?, role=?, "
                    + "status=? WHERE user_id=?";
        }

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int i = 1;
            ps.setString(i++, username);

            if (changePassword) {
                ps.setString(i++, BCrypt.hashpw(password, BCrypt.gensalt()));
            }

            ps.setString(i++, fullName);
            ps.setString(i++, role);
            ps.setString(i++, status);
            ps.setInt(i, selectedUser.getUserId());

            ps.executeUpdate();

            // Exercise 5: reactivating an Inactive account through UPDATE.
            boolean reactivated = "Inactive".equalsIgnoreCase(selectedUser.getStatus())
                    && "Active".equals(status);

            showInformation("Success", reactivated
                    ? "User account reactivated. " + username + " can log in again."
                    : "User account updated successfully.");
            loadUsers();
            clearForm();

        } catch (SQLException e) {
            messageLabel.setText("Unable to update user: " + e.getMessage());
        }
    }

    // Exercise 4: Confirmation Alert listing what will be saved.
    private boolean confirmUpdate(String username, String fullName, String role,
                                  String status, boolean changePassword) {
        StringBuilder details = new StringBuilder();
        details.append("Username: ").append(username).append('\n')
                .append("Full Name: ").append(fullName).append('\n')
                .append("Role: ").append(role).append('\n')
                .append("Status: ").append(selectedUser.getStatus());

        if (!selectedUser.getStatus().equals(status)) {
            details.append("  \u2192  ").append(status);
        }

        details.append('\n')
                .append("Password: ").append(changePassword ? "will be changed" : "unchanged");

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                details.toString(),
                ButtonType.YES, ButtonType.NO
        );
        confirm.setTitle("Update User");
        confirm.setHeaderText("Save changes to " + selectedUser.getUsername() + "?");
        styleDialog(confirm);

        return confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    @FXML
    private void handleDeactivate() {
        if (selectedUser == null) {
            messageLabel.setText("Select a user from the table first.");
            return;
        }

        if (selectedUser.getUserId() == loggedInUserId) {
            messageLabel.setText("You cannot deactivate the account currently in use.");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Deactivate " + selectedUser.getUsername() + "?",
                ButtonType.YES, ButtonType.NO
        );
        confirm.setHeaderText("Confirm Account Deactivation");
        styleDialog(confirm);

        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        String sql = "UPDATE users SET status='Inactive' WHERE user_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, selectedUser.getUserId());
            ps.executeUpdate();

            showInformation("Success", "User account deactivated.");
            loadUsers();
            clearForm();

        } catch (SQLException e) {
            messageLabel.setText("Unable to deactivate user: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void fillForm(User user) {
        usernameField.setText(user.getUsername());
        passwordField.clear();
        fullNameField.setText(user.getFullName());
        roleComboBox.setValue(user.getRole());
        statusComboBox.setValue(user.getStatus());
        messageLabel.setText("Leave Password blank to keep the current password.");
    }

    private void clearForm() {
        selectedUser = null;
        userTable.getSelectionModel().clearSelection();
        usernameField.clear();
        passwordField.clear();
        fullNameField.clear();
        roleComboBox.setValue(null);
        statusComboBox.setValue("Active");
        messageLabel.setText("");
        usernameField.requestFocus();
    }

    private boolean validateFields(boolean adding) {
        if (usernameField.getText().trim().isEmpty()) {
            messageLabel.setText("Username is required.");
            return false;
        }

        String password = passwordField.getText();

        if (adding && password.isEmpty()) {
            messageLabel.setText("Password is required for a new user.");
            return false;
        }

        // Exercise 3: Stronger Password Validation. Required for new accounts;
        // also applied when an existing user's password is being changed.
        if (!password.isEmpty() && password.length() < MIN_PASSWORD_LENGTH) {
            messageLabel.setText("Password must be at least " + MIN_PASSWORD_LENGTH
                    + " characters long (currently " + password.length() + ").");
            passwordField.requestFocus();
            return false;
        }

        if (fullNameField.getText().trim().isEmpty()) {
            messageLabel.setText("Full name is required.");
            return false;
        }

        if (roleComboBox.getValue() == null) {
            messageLabel.setText("Select a role.");
            return false;
        }

        if (statusComboBox.getValue() == null) {
            messageLabel.setText("Select a status.");
            return false;
        }

        return true;
    }

    private boolean usernameExists(String username, int excludedUserId) {
        String sql = "SELECT user_id FROM users "
                + "WHERE username=? AND user_id<>?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setInt(2, excludedUserId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            messageLabel.setText("Unable to check username: " + e.getMessage());
            return true;
        }
    }

    private void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }

    // Applies the shared pastry shop dialog style.
    private void styleDialog(Alert alert) {
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/pos/view/pos.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("pos-dialog");
    }
}
