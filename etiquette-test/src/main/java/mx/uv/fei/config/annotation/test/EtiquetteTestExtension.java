package mx.uv.fei.config.annotation.test;

import mx.uv.fei.config.annotation.core.DependencyInjector;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class EtiquetteTestExtension implements TestInstancePostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(EtiquetteTestExtension.class);

    private final TestContextInitializer contextInitializer = new TestContextInitializer();
    private final TestProfileResolver profileResolver = new TestProfileResolver();

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        Class<?> testClass = testInstance.getClass();

        if (testClass.isAnnotationPresent(StartEtiquetteTest.class)) {
            initializeTestContext(testInstance, testClass);
        }
    }

    private void initializeTestContext(Object testInstance, Class<?> testClass) {
        try {
            String activeProfile = profileResolver.resolve(testClass);
            DependencyInjector injector = contextInitializer.prepareInjector(testClass, activeProfile);
            injector.injectDependencies(testInstance);
        } catch (ClassNotFoundException e) {
            LOG.error("No se encontro la clase de configuracion para el contexto de prueba: {}", testClass.getName(), e);
            throw new IllegalStateException("No se encontro la clase de configuracion para el contexto de prueba: " + testClass.getName(), e);
        } catch (NoSuchMethodException e) {
            LOG.error("No se encontro el constructor requerido en el modulo de configuracion: {}", testClass.getName(), e);
            throw new IllegalStateException("No se encontro el constructor requerido en el modulo de configuracion: " + testClass.getName(), e);
        } catch (InstantiationException e) {
            LOG.error("No se pudo instanciar el modulo de configuracion para la prueba: {}", testClass.getName(), e);
            throw new IllegalStateException("No se pudo instanciar el modulo de configuracion para la prueba: " + testClass.getName(), e);
        } catch (IllegalAccessException e) {
            LOG.error("El constructor del modulo de configuracion no es accesible: {}", testClass.getName(), e);
            throw new IllegalStateException("El constructor del modulo de configuracion no es accesible: " + testClass.getName(), e);
        } catch (InvocationTargetException e) {
            LOG.error("El constructor del modulo de configuracion lanzo una excepcion interna: {}", testClass.getName(), e.getCause());
            throw new IllegalStateException("El constructor del modulo de configuracion lanzo una excepcion interna: " + testClass.getName(), e.getCause());
        }
    }
}
