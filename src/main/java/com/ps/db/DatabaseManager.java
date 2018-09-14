package com.ps.db;

import com.ps.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {


    private DatabaseManager() {

    }

    private static DatabaseManager instance;

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();

        }
        return instance;

    }


    private Connection connection;

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                if (Config.POSTGRES_DB_CONNECTION_URL == null) {
                    throw new RuntimeException("JDBC_DATABASE_URL environment variable is not set for the postgres db");
                }
                connection = DriverManager.getConnection(Config.POSTGRES_DB_CONNECTION_URL);
                connection.setAutoCommit(true);
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create Database connection.", e);
        }
    }


}
