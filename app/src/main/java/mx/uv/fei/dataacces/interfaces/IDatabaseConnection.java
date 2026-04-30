package mx.uv.fei.dataacces.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabaseConnection {

    Connection getConnection() throws SQLException;

}
