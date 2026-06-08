package mx.uv.fei.config.annotation.provider;

import mx.uv.fei.config.annotation.Interfaces.IProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodBasedProvider implements IProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MethodBasedProvider.class);

    private final Method providerMethod;
    private final Object targetInstance;

    public MethodBasedProvider(Method providerMethod, Object targetInstance) {
        this.providerMethod = providerMethod;
        this.targetInstance = targetInstance;
        this.providerMethod.setAccessible(true);
    }

    @Override
    public Object provide() {
        Object providedInstance;

        try {
            providedInstance = providerMethod.invoke(targetInstance);
        } catch (IllegalAccessException e) {
            LOG.error("El metodo proveedor no tiene nivel de acceso permitido para: {}", getReturnType().getName(), e);
            throw new IllegalStateException("El metodo proveedor no tiene nivel de acceso permitido para: " + getReturnType().getName(), e);
        } catch (InvocationTargetException e) {
            LOG.error("El metodo proveedor lanzo una excepcion interna para: {}", getReturnType().getName(), e.getCause());
            throw new IllegalStateException("El metodo proveedor lanzo una excepcion interna para: " + getReturnType().getName(), e.getCause());
        }

        return providedInstance;
    }

    @Override
    public Class<?> getReturnType() {
        return providerMethod.getReturnType();
    }
}
