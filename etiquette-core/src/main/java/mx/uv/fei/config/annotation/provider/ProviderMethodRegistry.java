package mx.uv.fei.config.annotation.provider;

import mx.uv.fei.config.annotation.Interfaces.IProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderMethodRegistry {

    private final Map<Class<?>, IProvider> registeredProvidersMap = new ConcurrentHashMap<>();

    public ProviderMethodRegistry(Object applicationModuleInstance) {
        registerDynamicProviders(applicationModuleInstance);
    }

    public boolean contains(Class<?> targetClassType) {
        return registeredProvidersMap.containsKey(targetClassType);
    }

    public Object provide(Class<?> targetClassType) {
        IProvider targetProviderToExecute = registeredProvidersMap.get(targetClassType);

        if (targetProviderToExecute == null) {
            throw new IllegalStateException("No se encontro un proveedor valido registrado en el sistema para la clase solicitada: " + targetClassType.getName());
        }

        return targetProviderToExecute.provide();
    }

    public void registerDynamicProviders(Object targetInstanceToScan) {
        List<IProvider> scannedProvidersList = ProviderScanner.scanForProviders(targetInstanceToScan);

        for (IProvider currentScannedProvider : scannedProvidersList) {
            registeredProvidersMap.put(currentScannedProvider.getReturnType(), currentScannedProvider);
        }
    }
}