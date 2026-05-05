package mx.uv.fei.config.annotation.resolver;

public class BasePackageResolver {

    public static String calculateBasePackage(Class<?> targetEntryPointClass) {
        String resolvedBasePackageName = targetEntryPointClass.getPackageName();

        if (resolvedBasePackageName.endsWith(".app") || resolvedBasePackageName.endsWith(".test")) {
            resolvedBasePackageName = resolvedBasePackageName.substring(0, resolvedBasePackageName.lastIndexOf("."));
        }

        return resolvedBasePackageName;
    }
}