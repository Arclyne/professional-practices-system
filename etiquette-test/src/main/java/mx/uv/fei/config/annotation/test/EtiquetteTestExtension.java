package mx.uv.fei.config.annotation.test;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.Profile;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.reflect.Constructor;

public class EtiquetteTestExtension implements TestInstancePostProcessor {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        Class<?> testClass = testInstance.getClass();

        if (!testClass.isAnnotationPresent(StartEtiquetteTest.class)) {
            return;
        }

        try {
            String profile = "local";
            if (testClass.isAnnotationPresent(Profile.class)) {
                profile = testClass.getAnnotation(Profile.class).value();
            }


            Class<?> configClass = Class.forName("mx.uv.fei.appconfiguration.ApplicationConfiguration");
            Constructor<?> constructor = configClass.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            IApplicationModule module = (IApplicationModule) constructor.newInstance(profile);


            String basePackage = "mx.uv.fei";


            DependencyInjector injector = new DependencyInjector(module, basePackage);


            Class<?> dataConfigClass = Class.forName("mx.uv.fei.appconfiguration.DataconnectionConfig");
            injector.retrieveInstance(dataConfigClass);

            injector.injectDependencies(testInstance);

        } catch (Exception e) {
            throw new RuntimeException("Error fatal inicializando el contexto de EtiquetteTest", e);
        }
    }
}