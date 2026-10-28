/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class FileDAO {

    // Database connection settings
    private static final String URL =
            "jdbc:mysql://localhost:3307/secure_file_locker_db";

    private static final String DB_USER = "root";

    // Apna actual MySQL password yahan likho
    private static final String DB_PASSWORD = "1234";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                URL, DB_USER, DB_PASSWORD
        );
    }

    // Save encrypted file details in MySQL
    public boolean saveFile(
            int userId,
            String fileName,
            String fileType,
            long fileSize,
            String filePath,
            String encryptionAlgorithm,
            byte[] iv,
            String encryptionKeyId,
            String fileHash) {

        String sql = "INSERT INTO files "
                + "(user_id, file_name, file_type, file_size, "
                + "file_path, encryption_algorithm, iv, "
                + "encryption_key_id, file_hash) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, fileName);
            ps.setString(3, fileType);
            ps.setLong(4, fileSize);
            ps.setString(5, filePath);
            ps.setString(6, encryptionAlgorithm);
            ps.setBytes(7, iv);
            ps.setString(8, encryptionKeyId);
            ps.setString(9, fileHash);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("File metadata saved successfully!");
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error saving file metadata: "
                    + e.getMessage());
        }

        return false;
    }

    // Display files belonging to a particular user
    public void getFilesByUserId(int userId) {

        String sql = "SELECT file_id, user_id, file_name, "
                + "file_type, file_size, file_path, "
                + "encryption_algorithm, uploaded_at "
                + "FROM files WHERE user_id = ? "
                + "ORDER BY uploaded_at DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    System.out.println("-------------------------");
                    System.out.println("File ID: "
                            + rs.getInt("file_id"));
                    System.out.println("User ID: "
                            + rs.getInt("user_id"));
                    System.out.println("File Name: "
                            + rs.getString("file_name"));
                    System.out.println("File Type: "
                            + rs.getString("file_type"));
                    System.out.println("File Size: "
                            + rs.getLong("file_size"));
                    System.out.println("File Path: "
                            + rs.getString("file_path"));
                    System.out.println("Algorithm: "
                            + rs.getString("encryption_algorithm"));
                    System.out.println("Uploaded At: "
                            + rs.getTimestamp("uploaded_at"));
                }

                if (!found) {
                    System.out.println(
                            "No files found for user ID: " + userId);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching files: "
                    + e.getMessage());
        }
    }

    // Testing the database connection
    public static void main(String[] args) {

        FileDAO dao = new FileDAO();

        try (Connection con = dao.getConnection()) {
            System.out.println(
                    "Database connection successful!");

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM files");
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    System.out.println(
                            "Total file records: " + rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Database connection failed: "
                    + e.getMessage());
        }
    }
    
    // Fetch file details for UserDashboard JTable
    public java.util.List<Object[]> getFileRowsByUserId(int userId)
            throws SQLException {

        java.util.List<Object[]> fileList =
                new java.util.ArrayList<>();

        String sql = "SELECT file_id, file_name, file_type, "
                + "file_size, uploaded_at "
                + "FROM files WHERE user_id = ? "
                + "ORDER BY uploaded_at DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    Object[] row = {
                        rs.getInt("file_id"),
                        rs.getString("file_name"),
                        rs.getString("file_type"),
                        rs.getLong("file_size"),
                        rs.getTimestamp("uploaded_at")
                    };

                    fileList.add(row);
                }
            }
        }

        return fileList;
    }

}
