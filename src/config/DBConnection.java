/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

import java.sql.*;

public class DBConnection {

    public static Connection getConnection() {

        try {
            // Step 1: Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Step 2: Connect to Database
            String url = "jdbc:mysql://localhost:3307/secure_file_locker_db";
            String id = "root";
            String password = "1234";

            Connection connection =
                    DriverManager.getConnection(url, id, password);

            return connection;
           

        } catch (Exception e) {
            System.out.println("Database Connection Error!");
            System.out.println(e.getMessage());
            return null;
        }
    }  
}

