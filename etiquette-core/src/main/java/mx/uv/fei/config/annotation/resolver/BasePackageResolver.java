package mx.uv.fei.config.annotation.resolver;


public class BasePackageResolver {

    private static final String SUFFIX_APP = ".app";
    private static final String SUFFIX_TEST = ".test";

    public static String calculateBasePackage(Class<?> targetEntryPointClass) {
        String packageName = targetEntryPointClass.getPackageName();
        String resolvedPackage = packageName;

        if (packageName.endsWith(SUFFIX_APP) || packageName.endsWith(SUFFIX_TEST)) {
            resolvedPackage = packageName.substring(0, packageName.lastIndexOf('.'));
        }

        return resolvedPackage;
    }
}
