package mx.uv.fei.domain.exceptions;

public class ManagerException extends Exception {

    public ManagerException(String message) {
        super(message);
    }

    public ManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}