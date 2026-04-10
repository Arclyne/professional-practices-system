package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Professor;

public interface IProfessorDAO {
   int insertProfessor(Professor professor) throws DAOException;
}