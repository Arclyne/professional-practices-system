package mx.uv.fei.config.annotation.test;

import mx.uv.fei.config.annotation.EtiquetteApplication;
import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.StartEtiquette;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EtiquetteTestExtension implements TestInstancePostProcessor {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {

        Class<?> testClass = testInstance.getClass();

        if (!testClass.isAnnotationPresent(StartEtiquette.class)) {
            return;
        }

        StartEtiquette annotation = testClass.getAnnotation(StartEtiquette.class);
        String profile = annotation.profile();
        Class<?> factoryClass = annotation.factory();

        if (factoryClass == void.class) {
            throw new IllegalArgumentException(
                    "Para pruebas, debes especificar la clase factory en @StartEtiquette. Ej: @StartEtiquette(factory = ApplicationConfigurationFactory.class)");
        }

        try {
            Method createMethod = factoryClass.getMethod("create", String.class);
            IApplicationModule module = (IApplicationModule) createMethod.invoke(null, profile);
            DependencyInjector injector = EtiquetteApplication.run(testClass, module);

            injector.injectDependencies(testInstance);

        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException(
                    "La clase Factory especificada no tiene un método público llamado 'create' que reciba un String.",
                    exception);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                    "El método 'create' de la Factory no es accesible. Asegúrate de que sea público.", exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("El método 'create' lanzó un error internamente durante su ejecución.",
                    exception.getCause());
        }
    }
}