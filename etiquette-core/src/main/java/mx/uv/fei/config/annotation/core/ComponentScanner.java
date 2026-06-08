package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.etiquette.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ComponentScanner {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentScanner.class);

    private final Map<Class<?>, Class<?>> catalog = new ConcurrentHashMap<>();

    public ComponentScanner(String basePackage) {
        try {
            for (Class<?> clazz : PackageScanner.findClasses(basePackage)) {
                if (clazz.isAnnotationPresent(Component.class)) {
                    registerComponent(clazz);
                }
            }
        } catch (ClassNotFoundException e) {
            LOG.error("No se pudo cargar una clase durante el escaneo del paquete: {}", basePackage, e);
            throw new IllegalStateException("No se pudo cargar una clase durante el escaneo del paquete: " + basePackage, e);
        } catch (IOException e) {
            LOG.error("Fallo de entrada/salida al escanear el paquete: {}", basePackage, e);
            throw new IllegalStateException("Fallo de entrada/salida al escanear el paquete: " + basePackage, e);
        }
    }

    public Class<?> getImpl(Class<?> type) {
        return catalog.get(type);
    }


    public Map<Class<?>, Class<?>> getCatalog() {
        return Collections.unmodifiableMap(catalog);
    }

    private void registerComponent(Class<?> componentClass) {
        catalog.put(componentClass, componentClass);
        registerInterfaceHierarchy(componentClass, componentClass);
        registerSuperclassHierarchy(componentClass, componentClass);
    }

    private void registerInterfaceHierarchy(Class<?> sourceClass, Class<?> componentClass) {
        for (Class<?> iface : sourceClass.getInterfaces()) {
            catalog.putIfAbsent(iface, componentClass);
            registerInterfaceHierarchy(iface, componentClass);
        }
    }

    private void registerSuperclassHierarchy(Class<?> sourceClass, Class<?> componentClass) {
        Class<?> superclass = sourceClass.getSuperclass();

        if (superclass != null && superclass != Object.class) {
            catalog.putIfAbsent(superclass, componentClass);
            registerInterfaceHierarchy(superclass, componentClass);
            registerSuperclassHierarchy(superclass, componentClass);
        }
    }
}
