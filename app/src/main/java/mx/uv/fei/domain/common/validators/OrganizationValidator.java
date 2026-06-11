package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;

public class OrganizationValidator {

    public static void validateOrganizationData(Organization organization) throws ManagerException {
        BaseValidator.validateString(organization.getNameOrganization(),
                "El nombre de la organización es obligatorio.");
        BaseValidator.validateMaxLength(organization.getNameOrganization(), FieldLengthLimits.ORGANIZATION_NAME_MAX,
                "El nombre de la organización no puede exceder " + FieldLengthLimits.ORGANIZATION_NAME_MAX + " caracteres.");
        BaseValidator.validateMaxLength(organization.getAdress(), FieldLengthLimits.ADDRESS_MAX,
                "La dirección no puede exceder " + FieldLengthLimits.ADDRESS_MAX + " caracteres.");
        BaseValidator.validateMaxLength(organization.getCity(), FieldLengthLimits.CITY_MAX,
                "La ciudad no puede exceder " + FieldLengthLimits.CITY_MAX + " caracteres.");
        BaseValidator.validateMaxLength(organization.getBusiness(), FieldLengthLimits.SECTOR_MAX,
                "El giro no puede exceder " + FieldLengthLimits.SECTOR_MAX + " caracteres.");
        BaseValidator.validateMaxLength(organization.getCellphone(), FieldLengthLimits.PHONE_MAX,
                "El teléfono no puede exceder " + FieldLengthLimits.PHONE_MAX + " caracteres.");
        validateEmail(organization.getMail());
    }

    private static void validateEmail(String email) throws ManagerException {
        BaseValidator.validateMaxLength(email, FieldLengthLimits.EMAIL_MAX,
                "El correo electrónico no puede exceder " + FieldLengthLimits.EMAIL_MAX + " caracteres.");
        if (email != null && !email.trim().isEmpty() && !BaseValidator.isValidEmail(email)) {
            throw new ManagerException("El formato del correo electrónico proporcionado no es válido.");
        }
    }
}
