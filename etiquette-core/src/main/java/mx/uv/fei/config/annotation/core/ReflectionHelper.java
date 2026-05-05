package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;

import java.lang.reflect.Constructor;

public class ReflectionHelper {

    public static Constructor<?> selectConstructor(Class<?> targetClassType) {
        Constructor<?> selectedConstructorToUse = null;

        for (Constructor<?> currentTargetConstructor : targetClassType.getConstructors()) {
            if (currentTargetConstructor.isAnnotationPresent(Inject.class)) {
                selectedConstructorToUse = currentTargetConstructor;
                break;
            }
        }

        if (selectedConstructorToUse == null) {
            Constructor<?>[] availablePublicConstructorsArray = targetClassType.getConstructors();
            if (availablePublicConstructorsArray.length == 0) {
                throw new IllegalStateException("No se encontro ningun constructor publico disponible para instanciar la clase: " + targetClassType.getName());
            }
            selectedConstructorToUse = availablePublicConstructorsArray[0];
        }

        return selectedConstructorToUse;
    }

    public static String resolveProfile(Class<?> targetClassType, String fallbackProfileConfiguration) {
        String resolvedProfileName = fallbackProfileConfiguration;

        if (targetClassType.isAnnotationPresent(Profile.class)) {
            resolvedProfileName = targetClassType.getAnnotation(Profile.class).value();
        }

        return resolvedProfileName;
    }
}
