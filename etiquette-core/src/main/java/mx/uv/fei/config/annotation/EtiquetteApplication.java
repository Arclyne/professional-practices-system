package mx.uv.fei.config.annotation;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.etiquette.StartEtiquette;

import java.lang.reflect.Constructor;

public class EtiquetteApplication {

    public static DependencyInjector run(Class<?> entryPointClass) {
        String profile = resolveProfile(entryPointClass);
        IApplicationModule module = null;

        if (entryPointClass.isAnnotationPresent(StartEtiquette.class)) {
            StartEtiquette start = entryPointClass.getAnnotation(StartEtiquette.class);

            try {
                Class<?> targetClass = start.factory().equals(IApplicationModule.class)
                        ? getConventionClass(entryPointClass)
                        : start.factory();

                // Instanciar pasando el perfil al constructor
                module = instantiateModule(targetClass, profile);
            } catch (Exception e) {
                throw new IllegalStateException("Error al inicializar Etiquette: " + e.getMessage(), e);
            }
        }

        // Determinar el paquete base para el escáner
        String basePackage = entryPointClass.getPackageName();

        // Subimos un nivel en la jerarquía si el punto de entrada está en subpaquetes comunes
        // para asegurar que escaneamos todo 'mx.uv.fei'
        if (basePackage.endsWith(".app") || basePackage.endsWith(".test")) {
            basePackage = basePackage.substring(0, basePackage.lastIndexOf("."));
        }

        // Le pasamos el paquete base al inyector para que inicie el escaneo
        return new DependencyInjector(module, basePackage);
    }

    private static IApplicationModule instantiateModule(Class<?> clazz, String profile) throws Exception {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            return (IApplicationModule) constructor.newInstance(profile);
        } catch (NoSuchMethodException e) {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (IApplicationModule) constructor.newInstance();
        }
    }

    private static Class<?> getConventionClass(Class<?> entryPoint) throws ClassNotFoundException {
        String pkg = entryPoint.getPackageName();
        String basePkg = pkg.contains(".") ? pkg.substring(0, pkg.lastIndexOf(".")) : pkg;
        String conventionPath = basePkg + ".appconfiguration.ApplicationConfiguration";
        return Class.forName(conventionPath);
    }

    public static String resolveProfile(Class<?> entryPointClass) {
        if (entryPointClass.isAnnotationPresent(Profile.class)) {
            return entryPointClass.getAnnotation(Profile.class).value();
        }
        return "local"; // fallback a "local"
    }
}