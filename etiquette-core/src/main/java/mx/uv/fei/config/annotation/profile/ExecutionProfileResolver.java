package mx.uv.fei.config.annotation.profile;

import mx.uv.fei.config.annotation.etiquette.Profile;

public class ExecutionProfileResolver {

    public static String resolveProfile(Class<?> targetEntryPointClass) {
        String resolvedExecutionProfile = "local";

        if (targetEntryPointClass.isAnnotationPresent(Profile.class)) {
            resolvedExecutionProfile = targetEntryPointClass.getAnnotation(Profile.class).value();
        }

        return resolvedExecutionProfile;
    }
}