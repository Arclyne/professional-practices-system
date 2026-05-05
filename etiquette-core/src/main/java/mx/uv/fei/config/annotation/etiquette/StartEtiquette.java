package mx.uv.fei.config.annotation.etiquette;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StartEtiquette {
    Class<? extends IApplicationModule> factory() default IApplicationModule.class;
}