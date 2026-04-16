package mx.uv.fei.dataacces.interfaces;

import java.sql.SQLException;
import java.sql.ResultSet;

@FunctionalInterface
public interface ISelectedList<T> {

    T mapRow(ResultSet resultset) throws SQLException;
}
