package mx.uv.fei.config.annotation.test;

import mx.uv.fei.config.annotation.etiquette.Profile;


class TestProfileResolver {

    private static final String DEFAULT_TEST_PROFILE = "test";

    String resolve(Class<?> testClass) {
        String resolvedProfile = DEFAULT_TEST_PROFILE;

        if (testClass.isAnnotationPresent(Profile.class)) {
            resolvedProfile = testClass.getAnnotation(Profile.class).value();
        }

        return resolvedProfile;
    }
}
