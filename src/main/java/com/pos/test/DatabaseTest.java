package com.pos.test;
import com.pos.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
public class DatabaseTest {
public static void main(String[] args) {
try (Connection conn = DatabaseConnection.getConnection()) {
if (conn != null && !conn.isClosed()) {
System.out.println("Database Connection Successful!");
System.out.println("Database: " + conn.getCatalog());
}
} catch (SQLException e) {
System.out.println("Database Connection Failed!");
System.out.println("Reason: " + e.getMessage());
}
}
}