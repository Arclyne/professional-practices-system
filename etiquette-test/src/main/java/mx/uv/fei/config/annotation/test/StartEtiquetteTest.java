package mx.uv.fei.config.annotation.test;

import mx.uv.fei.config.annotation.etiquette.StartEtiquette;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@StartEtiquette
@ExtendWith(EtiquetteTestExtension.class)
public @interface StartEtiquetteTest {

    String profile() default "test";
}
