package mx.uv.fei.dataaccess.database;

import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection implements IDatabaseConnection {
    private final String url;
    private final String user;
    private final String password;

    public DatabaseConnection(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
