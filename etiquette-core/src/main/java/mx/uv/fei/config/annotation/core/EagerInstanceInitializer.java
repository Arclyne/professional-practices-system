package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.etiquette.Keep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EagerInstanceInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(EagerInstanceInitializer.class);

    private final DependencyInjector dependencyInjector;
    private final Map<Class<?>, Class<?>> componentCatalog;
    private final String activeProfile;

    public EagerInstanceInitializer(DependencyInjector dependencyInjector, Map<Class<?>, Class<?>> componentCatalog, String activeProfile) {
        this.dependencyInjector = dependencyInjector;
        this.componentCatalog = componentCatalog;
        this.activeProfile = activeProfile;
    }

    public void initialize() {
        List<Class<?>> keepClasses = collectKeepClasses();

        for (Class<?> keepClass : keepClasses) {
            boolean alreadyResolved = dependencyInjector.isRegistered(keepClass);

            if (!alreadyResolved) {
                instantiateAndRegister(keepClass);
            }
        }
    }

    private List<Class<?>> collectKeepClasses() {
        List<Class<?>> keepClasses = new ArrayList<>();

        for (Class<?> candidate : componentCatalog.values()) {
            if (candidate.isAnnotationPresent(Keep.class)) {
                keepClasses.add(candidate);
            }
        }

        return keepClasses;
    }

    private void instantiateAndRegister(Class<?> keepClass) {
        Constructor<?> constructor = keepClass.getDeclaredConstructors()[0];
        Object[] arguments = resolveKeepConstructorArguments(constructor, keepClass);
        Object keepInstance;

        try {
            constructor.setAccessible(true);
            keepInstance = constructor.newInstance(arguments);
        } catch (InstantiationException e) {
            LOG.error("No se puede instanciar la clase @Keep porque es abstracta: {}", keepClass.getName(), e);
            throw new IllegalStateException("No se puede instanciar la clase @Keep porque es abstracta: " + keepClass.getName(), e);
        } catch (IllegalAccessException e) {
            LOG.error("El constructor de la clase @Keep no es accesible: {}", keepClass.getName(), e);
            throw new IllegalStateException("El constructor de la clase @Keep no es accesible: " + keepClass.getName(), e);
        } catch (InvocationTargetException e) {
            LOG.error("El constructor de la clase @Keep lanzo una excepcion interna: {}", keepClass.getName(), e.getCause());
            throw new IllegalStateException("El constructor de la clase @Keep lanzo una excepcion interna: " + keepClass.getName(), e.getCause());
        }

        dependencyInjector.registerKeepInstance(keepClass, keepInstance);
        dependencyInjector.registerAdditionalProviders(keepInstance);

        LOG.info("Clase @Keep inicializada y registrada: {}", keepClass.getSimpleName());
    }

    private Object[] resolveKeepConstructorArguments(Constructor<?> constructor, Class<?> keepClass) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            arguments[i] = resolveKeepArgument(parameterTypes[i], keepClass);
        }

        return arguments;
    }

    private Object resolveKeepArgument(Class<?> parameterType, Class<?> keepClass) {
        Object resolvedArgument;

        if (parameterType == String.class) {
            resolvedArgument = activeProfile;
        } else {
            boolean dependencyIsKeep = isKeepClass(parameterType);

            if (dependencyIsKeep && !dependencyInjector.isRegistered(parameterType)) {
                Class<?> implementation = componentCatalog.get(parameterType);

                if (implementation == null) {
                    implementation = parameterType;
                }

                instantiateAndRegister(implementation);
            }

            resolvedArgument = dependencyInjector.resolve(parameterType);
        }

        return resolvedArgument;
    }

    private boolean isKeepClass(Class<?> type) {
        boolean isKeep = type.isAnnotationPresent(Keep.class);

        if (!isKeep && componentCatalog.containsKey(type)) {
            isKeep = componentCatalog.get(type).isAnnotationPresent(Keep.class);
        }

        return isKeep;
    }
}
