package mx.uv.fei.config.annotation.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConventionModuleResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ConventionModuleResolver.class);
    private static final String CONFIGURATION_SUFFIX = ".appconfiguration.ApplicationConfiguration";

    public static Class<?> resolveByConvention(Class<?> targetEntryPointClass) {
        String basePackage = resolveBasePackage(targetEntryPointClass.getPackageName());
        String conventionPath = basePackage + CONFIGURATION_SUFFIX;
        Class<?> resolvedClass;

        try {
            resolvedClass = Class.forName(conventionPath);
        } catch (ClassNotFoundException e) {
            LOG.error("No se pudo encontrar la clase de configuracion por convencion en: {}", conventionPath, e);
            throw new IllegalStateException("No se pudo encontrar la clase de configuracion por convencion en: " + conventionPath, e);
        }

        return resolvedClass;
    }

    private static String resolveBasePackage(String currentPackageName) {
        String basePackage = currentPackageName;

        if (currentPackageName.contains(".")) {
            basePackage = currentPackageName.substring(0, currentPackageName.lastIndexOf('.'));
        }

        return basePackage;
    }
}
