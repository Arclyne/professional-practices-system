package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.exceptions.ManagerException;

public class SelfEvaluationValidator {

    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;

    public static void validateEvaluationData(SelfEvaluation evaluation) throws ManagerException {
        BaseValidator.validateId(evaluation.getReportId(), "Debe seleccionar un reporte válido.");
        BaseValidator.validateId(evaluation.getPractitionerId(), "Practicante no identificado.");
        BaseValidator.validateString(evaluation.getEvidence(), "La evidencia es obligatoria.");

        validateScore(evaluation.getQ1(), "Pregunta 1");
        validateScore(evaluation.getQ2(), "Pregunta 2");
        validateScore(evaluation.getQ3(), "Pregunta 3");
        validateScore(evaluation.getQ4(), "Pregunta 4");
        validateScore(evaluation.getQ5(), "Pregunta 5");
        validateScore(evaluation.getQ6(), "Pregunta 6");
        validateScore(evaluation.getQ7(), "Pregunta 7");
        validateScore(evaluation.getQ8(), "Pregunta 8");
        validateScore(evaluation.getQ9(), "Pregunta 9");
        validateScore(evaluation.getQ10(), "Pregunta 10");
    }

    private static void validateScore(int score, String fieldName) throws ManagerException {
        boolean isValidScore = score >= MIN_SCORE && score <= MAX_SCORE;

        if (!isValidScore) {
            throw new ManagerException("El valor de " + fieldName + " debe estar entre 1 y 5.");
        }
    }
}