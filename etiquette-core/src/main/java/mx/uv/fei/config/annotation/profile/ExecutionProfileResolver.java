package mx.uv.fei.config.annotation.profile;

import mx.uv.fei.config.annotation.etiquette.Profile;


public class ExecutionProfileResolver {

    private static final String DEFAULT_PROFILE = "local";

    public static String resolveProfile(Class<?> targetEntryPointClass) {
        String resolvedProfile = DEFAULT_PROFILE;

        if (targetEntryPointClass.isAnnotationPresent(Profile.class)) {
            resolvedProfile = targetEntryPointClass.getAnnotation(Profile.class).value();
        }

        return resolvedProfile;
    }
}
