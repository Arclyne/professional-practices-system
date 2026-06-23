package mx.uv.fei.domain.common.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;

public class FileValidatorTest {

    private static final long ONE_MEGABYTE = 1024L * 1024L;
    private static final long WITHIN_LIMIT_BYTES = 5 * ONE_MEGABYTE;
    private static final long AT_LIMIT_BYTES = 10 * ONE_MEGABYTE;
    private static final long OVER_LIMIT_BYTES = 11 * ONE_MEGABYTE;
    private static final long EMPTY_BYTES = 0L;

    @Test
    void validateFileSize_FileWithinLimit_DoesNotThrow() {
        File file = mockFileWithSize(WITHIN_LIMIT_BYTES);

        assertDoesNotThrow(() -> FileValidator.validateFileSize(file));
    }

    @Test
    void validateFileSize_FileAtLimit_DoesNotThrow() {
        File file = mockFileWithSize(AT_LIMIT_BYTES);

        assertDoesNotThrow(() -> FileValidator.validateFileSize(file));
    }

    @Test
    void validateFileSize_FileExceedingLimit_ThrowsManagerException() {
        File file = mockFileWithSize(OVER_LIMIT_BYTES);

        assertThrows(ManagerException.class, () -> FileValidator.validateFileSize(file));
    }

    @Test
    void validateFileSize_EmptyFile_ThrowsManagerException() {
        File file = mockFileWithSize(EMPTY_BYTES);

        assertThrows(ManagerException.class, () -> FileValidator.validateFileSize(file));
    }

    @Test
    void validateFileSize_NullFile_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> FileValidator.validateFileSize(null));
    }

    private File mockFileWithSize(long sizeInBytes) {
        File file = mock(File.class);
        when(file.exists()).thenReturn(true);
        when(file.length()).thenReturn(sizeInBytes);
        return file;
    }
}
