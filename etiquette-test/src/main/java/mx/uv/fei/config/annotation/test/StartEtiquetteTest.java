package mx.uv.fei.config.annotation.test;

import mx.uv.fei.config.annotation.etiquette.StartEtiquette;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@StartEtiquette
@ExtendWith(EtiquetteTestExtension.class)
public @interface StartEtiquetteTest {
}