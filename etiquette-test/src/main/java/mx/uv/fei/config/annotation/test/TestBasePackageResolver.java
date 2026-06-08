package mx.uv.fei.config.annotation.test;


class TestBasePackageResolver {

    private static final int PACKAGE_DEPTH = 3;
    private static final String PACKAGE_SEPARATOR = ".";

    String resolve(Class<?> testClass) {
        String[] packageSegments = testClass.getPackageName().split("\\.");
        StringBuilder basePackage = new StringBuilder();

        for (int i = 0; i < PACKAGE_DEPTH && i < packageSegments.length; i++) {
            if (i > 0) {
                basePackage.append(PACKAGE_SEPARATOR);
            }

            basePackage.append(packageSegments[i]);
        }

        return basePackage.toString();
    }
}
