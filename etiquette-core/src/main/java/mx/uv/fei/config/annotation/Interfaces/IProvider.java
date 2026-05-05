package mx.uv.fei.config.annotation.Interfaces;

public interface IProvider {
    Object provide();
    Class<?> getReturnType();
}