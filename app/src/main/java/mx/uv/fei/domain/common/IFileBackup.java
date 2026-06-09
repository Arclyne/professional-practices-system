package mx.uv.fei.domain.common;

import java.io.File;
import mx.uv.fei.domain.exceptions.ManagerException;

public interface IFileBackup {
    void backupFile(File sourceFile, String userIdentifier) throws ManagerException;
}
