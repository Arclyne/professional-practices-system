package mx.uv.fei.config.annotation.provider;

import mx.uv.fei.config.annotation.Interfaces.IProvider;
import mx.uv.fei.config.annotation.etiquette.Provide;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class ProviderScanner {

    public static List<IProvider> scanForProviders(Object targetInstance) {
        List<IProvider> discoveredProviders = new ArrayList<>();

        if (targetInstance != null) {
            for (Method method : targetInstance.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Provide.class)) {
                    discoveredProviders.add(new MethodBasedProvider(method, targetInstance));
                }
            }
        }

        return discoveredProviders;
    }
}
