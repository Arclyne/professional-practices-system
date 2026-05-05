package mx.uv.fei.config.annotation.resolver;

public class ConventionModuleResolver {

    public static Class<?> resolveByConvention(Class<?> targetEntryPointClass) {
        Class<?> resolvedConventionClass;
        String currentPackageName = targetEntryPointClass.getPackageName();
        String basePackageName = currentPackageName;

        if (currentPackageName.contains(".")) {
            basePackageName = currentPackageName.substring(0, currentPackageName.lastIndexOf("."));
        }

        String conventionModulePath = basePackageName + ".appconfiguration.ApplicationConfiguration";

        try {
            resolvedConventionClass = Class.forName(conventionModulePath);
        } catch (ClassNotFoundException classNotFoundException) {
            throw new IllegalStateException("No se pudo encontrar la clase de configuracion del modulo por convencion en la ruta esperada: " + conventionModulePath, classNotFoundException);
        }

        return resolvedConventionClass;
    }
}