package mx.uv.fei.dataacces.interfaces;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface IInsterGeneric {
    void insertGeneric(PreparedStatement statement) throws SQLException;
}
