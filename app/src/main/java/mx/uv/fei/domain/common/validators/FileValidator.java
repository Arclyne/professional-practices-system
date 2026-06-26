package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.exceptions.ManagerException;

import java.io.File;

public class FileValidator {

    private static final int MAX_FILE_SIZE_MB = 10;
    private static final long BYTES_PER_MEGABYTE = 1024L * 1024L;
    private static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * BYTES_PER_MEGABYTE;
    private static final long EMPTY_FILE_SIZE_BYTES = 0L;

    public static void validateFileSize(File file) throws ManagerException {
        if (file == null || !file.exists()) {
            throw new ManagerException("El archivo seleccionado no es válido o no existe.");
        }
        if (file.length() == EMPTY_FILE_SIZE_BYTES) {
            throw new ManagerException("El archivo está vacío. Selecciona un documento con contenido.");
        }
        if (file.length() > MAX_FILE_SIZE_BYTES) {
            throw new ManagerException(String.format(
                    "El archivo no puede exceder %d MB. El archivo seleccionado pesa %.1f MB.",
                    MAX_FILE_SIZE_MB, (double) file.length() / BYTES_PER_MEGABYTE));
        }
    }
}
