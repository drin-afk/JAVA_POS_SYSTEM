package com.pos.test;
 
import com.pos.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
 
// Exercise 2: list the products with quantity less than 30
public class LowStockProductTest {
 
    public static void main(String[] args) {
        String sql = "SELECT product_code, product_name, quantity "
                + "FROM products WHERE quantity < ? ORDER BY quantity ASC";
 
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, 30);
 
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("LOW-STOCK PRODUCTS (quantity < 30)");
                System.out.println("------------------------------------------");
 
                while (rs.next()) {
                    System.out.printf("%s | %s | Stock: %d%n",
                            rs.getString("product_code"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
    }
}

