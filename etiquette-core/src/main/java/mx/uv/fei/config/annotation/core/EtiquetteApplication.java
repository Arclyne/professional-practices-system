package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.profile.ExecutionProfileResolver;
import mx.uv.fei.config.annotation.resolver.BasePackageResolver;

public class EtiquetteApplication {

    public static DependencyInjector run(Class<?> targetEntryPointClass) {
        String activeExecutionProfile = ExecutionProfileResolver.resolveProfile(targetEntryPointClass);
        IApplicationModule applicationModuleInstance = ApplicationModuleFactory.createModuleInstance(targetEntryPointClass, activeExecutionProfile);
        String basePackageToScan = BasePackageResolver.calculateBasePackage(targetEntryPointClass);

        return new DependencyInjector(applicationModuleInstance, basePackageToScan);
    }
}