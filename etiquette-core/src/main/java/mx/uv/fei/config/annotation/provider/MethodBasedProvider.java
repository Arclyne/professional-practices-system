package mx.uv.fei.config.annotation.provider;

import mx.uv.fei.config.annotation.Interfaces.IProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodBasedProvider implements IProvider {

    private final Method providerMethod;
    private final Object targetInstanceToInvoke;

    public MethodBasedProvider(Method providerMethod, Object targetInstanceToInvoke) {
        this.providerMethod = providerMethod;
        this.targetInstanceToInvoke = targetInstanceToInvoke;
        this.providerMethod.setAccessible(true);
    }

    @Override
    public Object provide() {
        Object providedInstanceResult;

        try {
            providedInstanceResult = providerMethod.invoke(targetInstanceToInvoke);
        } catch (IllegalAccessException illegalAccessException) {
            throw new IllegalStateException("El metodo proveedor no tiene nivel de acceso permitido para la clase solicitada: " + getReturnType().getName(), illegalAccessException);
        } catch (InvocationTargetException invocationTargetException) {
            throw new IllegalStateException("El metodo proveedor lanzo una excepcion interna durante su ejecucion para la clase solicitada: " + getReturnType().getName(), invocationTargetException.getCause());
        }

        return providedInstanceResult;
    }

    @Override
    public Class<?> getReturnType() {
        return providerMethod.getReturnType();
    }
}