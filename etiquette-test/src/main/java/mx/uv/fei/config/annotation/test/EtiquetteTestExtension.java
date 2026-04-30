package mx.uv.fei.config.annotation.test;

import mx.uv.fei.config.annotation.EtiquetteApplication;
import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.StartEtiquette;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class EtiquetteTestExtension implements TestInstancePostProcessor {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {

        Class<?> testClass = testInstance.getClass();

        if (!testClass.isAnnotationPresent(StartEtiquette.class)) {
            return;
        }

        try {
            StartEtiquette annotation = testClass.getAnnotation(StartEtiquette.class);

            String profile = annotation.profile();

            IApplicationModule module = EtiquetteApplication.bootstrap(profile);

            DependencyInjector injector = EtiquetteApplication.run(testClass, module);

            injector.injectDependencies(testInstance);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}