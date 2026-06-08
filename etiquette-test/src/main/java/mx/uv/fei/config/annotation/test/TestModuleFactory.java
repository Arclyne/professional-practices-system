package mx.uv.fei.config.annotation.test;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


class TestModuleFactory {

    private static final Logger LOG = LoggerFactory.getLogger(TestModuleFactory.class);
    private static final String CONFIGURATION_CLASS_SUFFIX = ".appconfiguration.ApplicationConfiguration";

    IApplicationModule create(String basePackage, String activeProfile) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> configurationClass = locateConfigurationClass(basePackage);
        IApplicationModule applicationModule = instantiateModule(configurationClass, activeProfile);

        return applicationModule;
    }

    private Class<?> locateConfigurationClass(String basePackage) throws ClassNotFoundException {
        String configurationPath = basePackage + CONFIGURATION_CLASS_SUFFIX;
        Class<?> configurationClass;

        try {
            configurationClass = Class.forName(configurationPath);
        } catch (ClassNotFoundException e) {
            LOG.error("No se encontro la clase de configuracion en la ruta: {}", configurationPath, e);
            throw new ClassNotFoundException("No se encontro la clase de configuracion en la ruta: " + configurationPath, e);
        }

        return configurationClass;
    }

    private IApplicationModule instantiateModule(Class<?> configurationClass, String activeProfile) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> profileConstructor = configurationClass.getDeclaredConstructor(String.class);
        profileConstructor.setAccessible(true);
        IApplicationModule applicationModule = (IApplicationModule) profileConstructor.newInstance(activeProfile);

        return applicationModule;
    }
}
