package com.pos.test;
 
import com.pos.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
 
// Exercise 1: count the active products
public class ActiveProductCountTest {
 
    public static void main(String[] args) {
        String sql = "SELECT COUNT(*) AS total FROM products WHERE status = 'Active'";
 
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
 
            if (rs.next()) {
                System.out.println("Active Products: " + rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
    }
}
