package mx.uv.fei.domain.common.validators;

/**
 * Limites maximos de longitud para los campos de texto, alineados con el tamano de las
 * columnas de la base de datos. Permiten validar antes de la persistencia para evitar que
 * un valor demasiado largo provoque un error de base de datos enmascarado.
 *
 * @author Angel Gabriel Aguilar Hernandez
 */
public final class FieldLengthLimits {

    public static final int USERNAME_MAX = 20;
    public static final int PASSWORD_MAX = 255;
    public static final int NAME_MAX = 100;
    public static final int LAST_NAME_MAX = 100;
    public static final int EMAIL_MAX = 150;
    public static final int PHONE_MAX = 20;
    public static final int INDIGENOUS_LANGUAGE_MAX = 100;
    public static final int ORGANIZATION_NAME_MAX = 150;
    public static final int ADDRESS_MAX = 255;
    public static final int CITY_MAX = 100;
    public static final int SECTOR_MAX = 100;
    public static final int MANAGER_NAME_MAX = 150;
    public static final int PROJECT_NAME_MAX = 200;
    public static final int PERIOD_NAME_MAX = 50;
    public static final int SECTION_MAX = 50;
    public static final int GRADE_PERIOD_MAX = 100;
    public static final int ACTIVITY_TITLE_MAX = 255;
    public static final int MESSAGE_SUBJECT_MAX = 255;
    public static final int SIGNED_FILE_URL_MAX = 500;
    public static final int LONG_TEXT_MAX = 3000;

    private FieldLengthLimits() {
    }
}
