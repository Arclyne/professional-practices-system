package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.exceptions.ManagerException;

public class SelfEvaluationValidator {

    public static void validateEvaluationData(SelfEvaluation eval) throws ManagerException {
        BaseValidator.validateId(eval.getReportId(), "Debe seleccionar un reporte válido para esta autoevaluación.");
        BaseValidator.validateId(eval.getPractitionerId(), "Practicante no identificado.");
        BaseValidator.validateString(eval.getEvidence(), "La evidencia es obligatoria.");
    }
}