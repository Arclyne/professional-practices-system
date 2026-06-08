package mx.uv.fei.config.annotation.test;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.core.ComponentScanner;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.core.EagerInstanceInitializer;

import java.lang.reflect.InvocationTargetException;


public class TestContextInitializer {

    private final TestBasePackageResolver packageResolver = new TestBasePackageResolver();
    private final TestModuleFactory moduleFactory = new TestModuleFactory();

    public DependencyInjector prepareInjector(Class<?> testClass, String activeProfile) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String basePackage = packageResolver.resolve(testClass);
        IApplicationModule applicationModule = moduleFactory.create(basePackage, activeProfile);
        ComponentScanner componentScanner = new ComponentScanner(basePackage);
        DependencyInjector dependencyInjector = new DependencyInjector(applicationModule, componentScanner);

        EagerInstanceInitializer eagerInitializer = new EagerInstanceInitializer(
                dependencyInjector,
                componentScanner.getCatalog(),
                activeProfile
        );
        eagerInitializer.initialize();

        return dependencyInjector;
    }
}