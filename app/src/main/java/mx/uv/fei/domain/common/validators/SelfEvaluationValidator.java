package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.exceptions.ManagerException;

public class SelfEvaluationValidator {

    public static void validateEvaluationData(SelfEvaluation eval) throws ManagerException {
        BaseValidator.validateId(eval.getReportId(), "Debe seleccionar un reporte válido.");
        BaseValidator.validateId(eval.getPractitionerId(), "Practicante no identificado.");
        BaseValidator.validateString(eval.getEvidence(), "La evidencia es obligatoria.");

        validateScore(eval.getQ1(), "Pregunta 1");
        validateScore(eval.getQ2(), "Pregunta 2");
        validateScore(eval.getQ3(), "Pregunta 3");
        validateScore(eval.getQ4(), "Pregunta 4");
        validateScore(eval.getQ5(), "Pregunta 5");
        validateScore(eval.getQ6(), "Pregunta 6");
        validateScore(eval.getQ7(), "Pregunta 7");
        validateScore(eval.getQ8(), "Pregunta 8");
        validateScore(eval.getQ9(), "Pregunta 9");
        validateScore(eval.getQ10(), "Pregunta 10");
    }

    private static void validateScore(int score, String fieldName) throws ManagerException {
        if (score < 1 || score > 5) {
            throw new ManagerException("El valor de " + fieldName + " debe estar entre 1 y 5.");
        }
    }
}