package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;

import java.lang.reflect.Constructor;


public class ReflectionHelper {

    public static Constructor<?> selectConstructor(Class<?> targetClass) {
        Constructor<?> selectedConstructor = findAnnotatedConstructor(targetClass);

        if (selectedConstructor == null) {
            selectedConstructor = selectFirstPublicConstructor(targetClass);
        }

        return selectedConstructor;
    }

    public static String resolveProfile(Class<?> targetClass, String fallbackProfile) {
        String resolvedProfile = fallbackProfile;

        if (targetClass.isAnnotationPresent(Profile.class)) {
            resolvedProfile = targetClass.getAnnotation(Profile.class).value();
        }

        return resolvedProfile;
    }

    private static Constructor<?> findAnnotatedConstructor(Class<?> targetClass) {
        Constructor<?> annotatedConstructor = null;

        for (Constructor<?> constructor : targetClass.getConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class) && annotatedConstructor == null) {
                annotatedConstructor = constructor;
            }
        }

        return annotatedConstructor;
    }

    private static Constructor<?> selectFirstPublicConstructor(Class<?> targetClass) {
        Constructor<?>[] publicConstructors = targetClass.getConstructors();

        if (publicConstructors.length == 0) {
            throw new IllegalStateException("No se encontro ningun constructor publico para instanciar: " + targetClass.getName());
        }

        return publicConstructors[0];
    }
}
