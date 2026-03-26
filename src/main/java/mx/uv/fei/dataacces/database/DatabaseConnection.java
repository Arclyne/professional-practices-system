package mx.uv.fei.dataacces.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import mx.uv.fei.config.FileConfigLoader;


public class DatabaseConnection {
    private String url;
    private String user;
    private String password;

    private static DatabaseConnection instance;

    private DatabaseConnection() {
        Properties databaseProperties = FileConfigLoader.loadProperties("database.properties");
    
        this.url = databaseProperties.getProperty("db.url");
        this.user = databaseProperties.getProperty("db.user");
        this.password = databaseProperties.getProperty("db.password");
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