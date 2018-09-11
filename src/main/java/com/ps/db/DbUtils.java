package com.ps.db;

import com.google.gson.Gson;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DbUtils {

    private static String CREATE_DATA_TABLE = "CREATE TABLE pooldata (hash varchar(70) NOT NULL UNIQUE, userid varchar(255), data varchar(8000))";
    private static String GET_ALL_DATA_FOR_USER = "SELECT * FROM pooldata WHERE userid = '?'";
    private static String DELETE_ALL_DATA_FOR_USER = "DELETE * FROM pooldata WHERE userid = '?'";
    private static String INSERT_DATA_INTO_DB = "INSERT INTO pooldata(hash,userid,data) VALUES ('?', '?','?')";
    private static String DROP_TABLE = "DROP TABLE IF EXISTS pooldata";
    private static Gson gson = new Gson();

    public static void createPoolDataTable() {

        Connection connection = DatabaseManager.getInstance().getConnection();

        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(DROP_TABLE);
            stmt.executeUpdate(CREATE_DATA_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public static List<Map<String, String>> getAllDataForUser(String userId) {

        if (userId == null || "".equals(userId)) {
            throw new RuntimeException("User Id cannot be empty or null.");
        }


        Connection connection = DatabaseManager.getInstance().getConnection();

        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(GET_ALL_DATA_FOR_USER);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<Map<String, String>> neededRows = new ArrayList<>();
            while (rs.next()) {
                Map<String, String> rowData = gson.fromJson(rs.getString("data"), Map.class);
                neededRows.add(rowData);

                System.out.println("Read from DB: " + rs.getTimestamp("tick"));
            }

            return neededRows;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error in getting the data for the user " + userId, e);
        }


    }

    public static boolean addUserToDB(String userId, Map<String, String> data) {

        String dataAsJson = gson.toJson(data);
        Connection connection = DatabaseManager.getInstance().getConnection();

        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(INSERT_DATA_INTO_DB);
            stmt.setString(1, userId +  data.hashCode());
            stmt.setString(2, userId);
            stmt.setString(3, dataAsJson);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error in inserting the data for the user " + userId, e);
        }

    }


    public static boolean resetDBfromCache(String userId, List<Map<String, String>> data) {

        if (userId == null || "".equals(userId)) {
            throw new RuntimeException("User Id cannot be empty or null.");
        }


        Connection connection = DatabaseManager.getInstance().getConnection();

        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(DELETE_ALL_DATA_FOR_USER);
            stmt.setString(1, userId);
            int affectedRows = stmt.executeUpdate();
            data.forEach(stringStringMap -> addUserToDB(userId, stringStringMap));
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
//            throw new RuntimeException("Error in getting the data for the user " + userId, e);
        }


    }
}
