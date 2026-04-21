package mx.uv.fei;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

public class TestDatabaseSetup {
    public static void initialize(IDatabaseConnection dbConnection) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("RUNSCRIPT FROM 'classpath:schema.sql'");
            stmt.execute("RUNSCRIPT FROM 'classpath:data.sql'");
        }
    }
}