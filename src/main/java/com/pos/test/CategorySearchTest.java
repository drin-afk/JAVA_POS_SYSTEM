package com.pos.test;
 
import com.pos.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
 
// Exercise 3: search the products by category
public class CategorySearchTest {
 
    public static void main(String[] args) {
       String category = args.length > 0 ? args[0] : "Beverage";
String sql = "SELECT p.product_code, p.product_name, p.price, p.quantity "
        + "FROM products p "
        + "JOIN categories c ON p.category_id = c.category_id "
        + "WHERE c.category_name = ? ORDER BY p.product_name";
 
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setString(1, category);
 
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("CATEGORY: " + category);
                System.out.println("------------------------------------------");
 
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%s | %s | %.2f | Stock: %d%n",
                            rs.getString("product_code"),
                            rs.getString("product_name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"));
                }
                if (!found) {
                    System.out.println("No products found in this category.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Search failed: " + e.getMessage());
        }
    }
}
