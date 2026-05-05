package mx.uv.fei.config.annotation.provider;

import mx.uv.fei.config.annotation.Interfaces.IProvider;
import mx.uv.fei.config.annotation.etiquette.Provide;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ProviderScanner {

    public static List<IProvider> scanForProviders(Object targetInstanceToScan) {
        List<IProvider> discoveredProvidersList = new ArrayList<>();

        if (targetInstanceToScan != null) {
            Method[] declaredMethodsInInstanceArray = targetInstanceToScan.getClass().getDeclaredMethods();

            for (Method currentMethodToInspect : declaredMethodsInInstanceArray) {
                if (currentMethodToInspect.isAnnotationPresent(Provide.class)) {
                    discoveredProvidersList.add(new MethodBasedProvider(currentMethodToInspect, targetInstanceToScan));
                }
            }
        }

        return discoveredProvidersList;
    }
}