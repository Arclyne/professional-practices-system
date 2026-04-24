package mx.uv.fei.domain.exceptions;

public class ManagerExeption extends Exception {

    public ManagerExeption(String message) {
        super(message);
    }

    public ManagerExeption(String message, Throwable cause) {
        super(message, cause);
    }
}