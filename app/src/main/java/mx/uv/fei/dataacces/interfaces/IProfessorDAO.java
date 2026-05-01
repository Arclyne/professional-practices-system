package mx.uv.fei.dataacces.interfaces;

import java.util.List;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Professor;

public interface IProfessorDAO {
   int insertProfessor(Professor professor) throws DAOException;

   Professor recoverProfessor(int professorId) throws DAOException;

   List<Professor> getAllProfessors() throws DAOException;

   boolean updateProfessor(Professor professorToUpdate, int id) throws DAOException;
}