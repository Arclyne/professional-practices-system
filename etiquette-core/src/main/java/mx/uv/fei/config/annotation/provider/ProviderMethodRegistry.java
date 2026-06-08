package mx.uv.fei.config.annotation.provider;

import mx.uv.fei.config.annotation.Interfaces.IProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ProviderMethodRegistry {

    private final Map<Class<?>, IProvider> registeredProviders = new ConcurrentHashMap<>();

    public ProviderMethodRegistry(Object applicationModuleInstance) {
        registerProvidersFrom(applicationModuleInstance);
    }

    public boolean contains(Class<?> targetType) {
        return registeredProviders.containsKey(targetType);
    }

    public Object provide(Class<?> targetType) {
        IProvider targetProvider = registeredProviders.get(targetType);

        if (targetProvider == null) {
            throw new IllegalStateException("No se encontro un proveedor registrado para: " + targetType.getName());
        }

        return targetProvider.provide();
    }

    public void registerProvidersFrom(Object targetInstance) {
        List<IProvider> scannedProviders = ProviderScanner.scanForProviders(targetInstance);

        for (IProvider provider : scannedProviders) {
            registeredProviders.put(provider.getReturnType(), provider);
        }
    }
}
