package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.etiquette.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ComponentScanner {

    private final Map<Class<?>, Class<?>> componentCatalog = new ConcurrentHashMap<>();

    public ComponentScanner(String basePackageToScan) {
        scanPackages(basePackageToScan);
    }

    private void scanPackages(String basePackageToScan) {
        try {
            List<Class<?>> discoveredClassesList = PackageScanner.findClasses(basePackageToScan);
            for (Class<?> currentDiscoveredClass : discoveredClassesList) {
                if (currentDiscoveredClass.isAnnotationPresent(Component.class)) {
                    componentCatalog.put(currentDiscoveredClass, currentDiscoveredClass);
                    for (Class<?> implementedInterface : currentDiscoveredClass.getInterfaces()) {
                        componentCatalog.put(implementedInterface, currentDiscoveredClass);
                    }
                }
            }
        } catch (ClassNotFoundException | IOException scanningException) {
            throw new IllegalStateException("Ocurrio un error fatal al intentar escanear los paquetes del sistema base.", scanningException);
        }
    }

    public Class<?> getImplementation(Class<?> targetType) {
        return componentCatalog.get(targetType);
    }

    public Iterable<Class<?>> getCatalogValues() {
        return componentCatalog.values();
    }
}