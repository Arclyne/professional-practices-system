package mx.uv.fei.dataacces.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import mx.uv.fei.config.FileConfigLoader;

public class DatabaseConnection {
    private String url;
    private String user;
    private String password;

    private static DatabaseConnection instance;

    private DatabaseConnection() {

        Map<String, String> config = FileConfigLoader.loadUseConfig("database.properties", "ssh");

        this.url = config.get("db.url");
        this.user = config.get("db.user");
        this.password = config.get("db.password");
    }

    public static DatabaseConnection getInstance() {

        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}