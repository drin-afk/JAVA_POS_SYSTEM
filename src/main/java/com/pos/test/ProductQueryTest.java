
package com.pos.test;
import com.pos.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class ProductQueryTest {
public static void main(String[] args) {
String sql = "SELECT product_code, product_name, price, quantity "
+ "FROM products ORDER BY product_name";
try (Connection conn = DatabaseConnection.getConnection();
PreparedStatement stmt = conn.prepareStatement(sql);
ResultSet rs = stmt.executeQuery()) {
System.out.println("PRODUCT LIST");
System.out.println("------------------------------------------");
while (rs.next()) {
String code = rs.getString("product_code");
String name = rs.getString("product_name");
double price = rs.getDouble("price");
int quantity = rs.getInt("quantity");
System.out.printf("%s | %s | %.2f | Stock: %d%n",
code, name, price, quantity);
}
} catch (SQLException e) {
System.out.println("Query failed: " + e.getMessage());
}
}
}