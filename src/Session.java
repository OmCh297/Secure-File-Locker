/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


package session;

public class Session {

    private static int userId = -1;
    private static String fullName;

    public static void setUserId(int id) {
        userId = id;
    }

    public static int getUserId() {
        return userId;
    }

    public static void setFullName(String name) {
        fullName = name;
    }

    public static String getFullName() {
        return fullName;
    }

    public static boolean isLoggedIn() {
        return userId > 0;
    }

    public static void clear() {
        userId = -1;
        fullName = null;
    }
}

