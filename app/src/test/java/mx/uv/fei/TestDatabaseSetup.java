package mx.uv.fei;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;

public class TestDatabaseSetup {

    /** Hash Argon2id con el que se siembran las contraseñas de prueba (texto plano: "ClaveFei2026"). */
    public static final String SEED_PASSWORD_HASH =
            "$argon2id$v=19$m=19456,t=3,p=1$DCg1S9In6YGYy7www34CHw$G+vLCRPOM6Lidsn4xrKHC3Q7n3Pbc8Q66I0wbEAMTGw";

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
