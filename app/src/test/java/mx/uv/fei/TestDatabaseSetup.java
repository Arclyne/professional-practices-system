package mx.uv.fei;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;

public class TestDatabaseSetup {

    private static final String SQL_RUN_SCHEMA_SCRIPT = "RUNSCRIPT FROM 'classpath:schema.sql'";
    private static final String SQL_RUN_DATA_SCRIPT = "RUNSCRIPT FROM 'classpath:data.sql'";

    public static void initialize(IDatabaseConnection databaseConnection) throws SQLException {
        try (Connection connection = databaseConnection.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(SQL_RUN_SCHEMA_SCRIPT);
            statement.execute(SQL_RUN_DATA_SCRIPT);
        }
    }
}
