package mx.uv.fei.config.annotation.test;

import mx.uv.fei.config.annotation.EtiquetteApplication;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.StartEtiquette;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class EtiquetteTestExtension implements TestInstancePostProcessor {
    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {

        DependencyInjector injector = EtiquetteApplication.run(testInstance.getClass());


        injector.retrieveInstance(DataconnectionConfig.class);

        injector.injectDependencies(testInstance);
    }
}