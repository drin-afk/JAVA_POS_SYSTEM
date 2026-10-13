
package com.pos.test;
import com.pos.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class ProductSearchTest {
public static void main(String[] args) {
String productCode = "P001";
String sql = "SELECT product_name, price, quantity "
+ "FROM products WHERE product_code = ?";
try (Connection conn = DatabaseConnection.getConnection();
PreparedStatement stmt = conn.prepareStatement(sql)) {
stmt.setString(1, productCode);
try (ResultSet rs = stmt.executeQuery()) {
if (rs.next()) {
System.out.println("Product: "
+ rs.getString("product_name"));
System.out.println("Price: "
+ rs.getDouble("price"));
System.out.println("Stock: "
+ rs.getInt("quantity"));
} else {
System.out.println("Product not found.");
}
}
} catch (SQLException e) {
System.out.println("Search failed: " + e.getMessage());
}
}
}