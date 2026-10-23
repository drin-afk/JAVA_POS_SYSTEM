package com.pos.controller;

import com.pos.database.DatabaseConnection;
import com.pos.model.Product;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProductManagementController implements Initializable {

    @FXML private TextField txtProductCode;
    @FXML private TextField txtProductName;
    @FXML private TextField txtCategory;
    @FXML private TextField txtPrice;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private Label lblMessage;

    @FXML private TableView<Product> tblProducts;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colCode;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colQuantity;
    @FXML private TableColumn<Product, String> colStatus;

    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));
        cmbStatus.setValue("Active");

        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Exercise 5: Format the TableView price column so it always displays two decimal places
        colPrice.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", price));
                }
            }
        });

        tblProducts.setItems(productList);
        tblProducts.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldValue, selected) -> showSelectedProduct(selected));

        loadProducts("");
    }

    private void loadProducts(String keyword) {
        productList.clear();

        // Join products with categories table to obtain the human-readable category name
        String sql = "SELECT p.product_id, p.product_code, p.product_name, "
                + "COALESCE(c.category_name, 'Uncategorized') AS category_name, "
                + "p.price, p.quantity, p.status "
                + "FROM products p "
                + "LEFT JOIN categories c ON p.category_id = c.category_id ";

        boolean searching = keyword != null && !keyword.trim().isEmpty();
        if (searching) {
            sql += "WHERE p.product_code LIKE ? OR p.product_name LIKE ? OR c.category_name LIKE ? ";
        }
        sql += "ORDER BY p.product_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (searching) {
                String key = "%" + keyword.trim() + "%";
                ps.setString(1, key);
                ps.setString(2, key);
                ps.setString(3, key);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productList.add(new Product(
                            rs.getInt("product_id"),
                            rs.getString("product_code"),
                            rs.getString("product_name"),
                            rs.getString("category_name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"),
                            rs.getString("status")
                    ));
                }
            }

            lblMessage.setText(productList.size() + " product(s) displayed.");

        } catch (SQLException ex) {
            showError("Unable to load products", ex.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        ProductInput input = readAndValidateInput();
        if (input == null) return;

        if (productCodeExists(input.code, 0)) {
            showWarning("Duplicate Product Code", "Product code already exists.");
            return;
        }

        String sql = "INSERT INTO products (product_code, product_name, category_id, price, quantity, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int categoryId = resolveCategoryId(conn, input.category);

            ps.setString(1, input.code);
            ps.setString(2, input.name);
            ps.setInt(3, categoryId);
            ps.setDouble(4, input.price);
            ps.setInt(5, input.quantity);
            ps.setString(6, input.status);

            ps.executeUpdate();

            showInformation("Success", "Product \"" + input.name + "\" added successfully.");
            lblMessage.setText("Product added successfully.");
            clearForm();
            loadProducts("");

        } catch (SQLException ex) {
            showError("Unable to add product", ex.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Product selected = tblProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Select a product from the table first.");
            return;
        }

        ProductInput input = readAndValidateInput();
        if (input == null) return;

        if (productCodeExists(input.code, selected.getProductId())) {
            showWarning("Duplicate Product Code", "Another product already uses this code.");
            return;
        }

        String sql = "UPDATE products SET product_code=?, product_name=?, category_id=?, "
                + "price=?, quantity=?, status=? WHERE product_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int categoryId = resolveCategoryId(conn, input.category);

            ps.setString(1, input.code);
            ps.setString(2, input.name);
            ps.setInt(3, categoryId);
            ps.setDouble(4, input.price);
            ps.setInt(5, input.quantity);
            ps.setString(6, input.status);
            ps.setInt(7, selected.getProductId());

            ps.executeUpdate();

            showInformation("Success", "Product \"" + input.name + "\" updated successfully.");
            lblMessage.setText("Product updated successfully.");
            clearForm();
            loadProducts("");

        } catch (SQLException ex) {
            showError("Unable to update product", ex.getMessage());
        }
    }

    // Challenge: Add a confirmation dialog before deactivating a product
    @FXML
    private void handleDeactivate() {
        Product selected = tblProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Select a product to deactivate.");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to deactivate \"" + selected.getProductName()
                        + "\" (Code: " + selected.getProductCode() + ")?",
                ButtonType.YES, ButtonType.NO
        );
        confirm.setTitle("Confirm Deactivation");
        confirm.setHeaderText("Deactivate Product");
        styleDialog(confirm);

        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        String sql = "UPDATE products SET status='Inactive' WHERE product_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selected.getProductId());
            ps.executeUpdate();

            showInformation("Success", "Product \"" + selected.getProductName() + "\" deactivated.");
            lblMessage.setText("Product deactivated.");
            clearForm();
            loadProducts("");

        } catch (SQLException ex) {
            showError("Unable to deactivate product", ex.getMessage());
        }
    }

    // Exercise 1: Search button displays all records when search field is blank
    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText();
        loadProducts(keyword == null ? "" : keyword.trim());
    }

    // Exercise 1: Clear Search button to clear field and show all records
    @FXML
    private void handleClearSearch() {
        txtSearch.clear();
        loadProducts("");
    }

    @FXML
    private void handleRefresh() {
        txtSearch.clear();
        loadProducts("");
    }

    // Exercise 4: Low Stock search button that displays products with quantity <= 5
    @FXML
    private void handleLowStock() {
        productList.clear();

        String sql = "SELECT p.product_id, p.product_code, p.product_name, "
                + "COALESCE(c.category_name, 'Uncategorized') AS category_name, "
                + "p.price, p.quantity, p.status "
                + "FROM products p "
                + "LEFT JOIN categories c ON p.category_id = c.category_id "
                + "WHERE p.quantity <= 5 "
                + "ORDER BY p.quantity ASC, p.product_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productList.add(new Product(
                        rs.getInt("product_id"),
                        rs.getString("product_code"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("status")
                ));
            }

            lblMessage.setText(productList.size() + " low stock product(s) (Qty \u2264 5).");

        } catch (SQLException ex) {
            showError("Unable to load low stock products", ex.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void showSelectedProduct(Product product) {
        if (product == null) return;
        txtProductCode.setText(product.getProductCode());
        txtProductName.setText(product.getProductName());
        txtCategory.setText(product.getCategory());
        txtPrice.setText(String.format("%.2f", product.getPrice()));
        txtQuantity.setText(String.valueOf(product.getQuantity()));
        cmbStatus.setValue(product.getStatus());
    }

    private ProductInput readAndValidateInput() {
        // Exercise 2: Automatically convert product codes to uppercase before saving
        String code = txtProductCode.getText().trim().toUpperCase();
        String name = txtProductName.getText().trim();
        String category = txtCategory.getText().trim();
        String status = cmbStatus.getValue();

        if (code.isEmpty() || name.isEmpty() || category.isEmpty()
                || txtPrice.getText().trim().isEmpty()
                || txtQuantity.getText().trim().isEmpty()) {
            showWarning("Incomplete Data", "Complete all required product fields.");
            return null;
        }

        double price;
        int quantity;

        try {
            price = Double.parseDouble(txtPrice.getText().trim());
            quantity = Integer.parseInt(txtQuantity.getText().trim());
        } catch (NumberFormatException ex) {
            showWarning("Invalid Number", "Price must be numeric and quantity must be a whole number.");
            return null;
        }

        // Exercise 3: Add validation rule that product price must be greater than zero
        if (price <= 0) {
            showWarning("Invalid Value", "Price must be greater than zero.");
            return null;
        }

        if (quantity < 0) {
            showWarning("Invalid Value", "Price and quantity cannot be negative.");
            return null;
        }

        return new ProductInput(code, name, category, price, quantity, status);
    }

    private int resolveCategoryId(Connection conn, String categoryName) throws SQLException {
        String selectSql = "SELECT category_id FROM categories WHERE LOWER(category_name) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, categoryName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }
        }

        // Insert new category if it does not exist yet
        String insertSql = "INSERT INTO categories (category_name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categoryName.trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Failed to resolve category ID for: " + categoryName);
    }

    private boolean productCodeExists(String code, int excludedId) {
        String sql = "SELECT product_id FROM products WHERE product_code=? AND product_id<>?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, excludedId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            showError("Unable to validate product code", ex.getMessage());
            return true;
        }
    }

    private void clearForm() {
        txtProductCode.clear();
        txtProductName.clear();
        txtCategory.clear();
        txtPrice.clear();
        txtQuantity.clear();
        cmbStatus.setValue("Active");
        tblProducts.getSelectionModel().clearSelection();
        lblMessage.setText("");
    }

    private void showInformation(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        styleDialog(a);
        a.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        styleDialog(a);
        a.showAndWait();
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        styleDialog(a);
        a.showAndWait();
    }

    private void styleDialog(Alert alert) {
        try {
            URL cssUrl = getClass().getResource("/com/pos/view/pos.css");
            if (cssUrl != null) {
                alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                alert.getDialogPane().getStyleClass().add("pos-dialog");
            }
        } catch (Exception ignored) {
        }
    }

    private static class ProductInput {
        String code, name, category, status;
        double price;
        int quantity;

        ProductInput(String code, String name, String category,
                     double price, int quantity, String status) {
            this.code = code;
            this.name = name;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.status = status;
        }
    }
}
