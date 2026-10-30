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
    
    
    // Fetch encrypted file details for download and decryption
    public FileDownloadData getFileForDownload(
            int fileId, int userId) throws SQLException {

        String sql = "SELECT file_name, file_path, iv "
                + "FROM files "
                + "WHERE file_id = ? AND user_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, fileId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new FileDownloadData(
                            rs.getString("file_name"),
                            rs.getString("file_path"),
                            rs.getBytes("iv")
                    );
                }
            }
        }

        return null;
    }

    // Store retrieved file details
    public static class FileDownloadData {

        private final String fileName;
        private final String filePath;
        private final byte[] iv;

        public FileDownloadData(
                String fileName,
                String filePath,
                byte[] iv) {

            this.fileName = fileName;
            this.filePath = filePath;
            this.iv = iv;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public byte[] getIV() {
            return iv == null ? null : iv.clone();
        }
    }
    
    
        // Fetch file details before deletion
    public FileDeleteData getFileForDeletion(
            int fileId, int userId) throws SQLException {

        String sql = "SELECT file_name, file_path "
                + "FROM files WHERE file_id = ? AND user_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, fileId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new FileDeleteData(
                            rs.getString("file_name"),
                            rs.getString("file_path")
                    );
                }
            }
        }

        return null;
    }

    // Save deletion history and remove active database record
    public boolean moveFileToDeleteHistory(
            int fileId, int userId) throws SQLException {

        String insertSql =
                "INSERT INTO deleted_files "
                + "(original_file_id, user_id, file_name, "
                + "file_type, file_size) "
                + "SELECT file_id, user_id, file_name, "
                + "file_type, file_size "
                + "FROM files WHERE file_id = ? AND user_id = ?";

        String deleteSql =
                "DELETE FROM files WHERE file_id = ? AND user_id = ?";

        try (Connection con = getConnection()) {

            con.setAutoCommit(false);

            try {
                int insertedRows;

                try (PreparedStatement ps =
                        con.prepareStatement(insertSql)) {

                    ps.setInt(1, fileId);
                    ps.setInt(2, userId);
                    insertedRows = ps.executeUpdate();
                }

                if (insertedRows != 1) {
                    con.rollback();
                    return false;
                }

                int deletedRows;

                try (PreparedStatement ps =
                        con.prepareStatement(deleteSql)) {

                    ps.setInt(1, fileId);
                    ps.setInt(2, userId);
                    deletedRows = ps.executeUpdate();
                }

                if (deletedRows != 1) {
                    con.rollback();
                    return false;
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // Store file details required for deletion
    public static class FileDeleteData {

        private final String fileName;
        private final String filePath;

        public FileDeleteData(String fileName, String filePath) {
            this.fileName = fileName;
            this.filePath = filePath;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFilePath() {
            return filePath;
        }
    }


}
