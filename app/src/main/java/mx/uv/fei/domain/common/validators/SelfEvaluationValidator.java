package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.exceptions.ManagerException;

public class SelfEvaluationValidator {

    private static final int MINIMUM_QUESTION_SCORE = 1;
    private static final int MAXIMUM_QUESTION_SCORE = 5;

    public static void validateEvaluationData(SelfEvaluation selfEvaluation) throws ManagerException {
        BaseValidator.validateId(selfEvaluation.getReportId(),
                "Debe seleccionar un reporte válido.");
        BaseValidator.validateId(selfEvaluation.getPractitionerId(),
                "Practicante no identificado.");
        BaseValidator.validateString(selfEvaluation.getEvidence(),
                "La evidencia es obligatoria.");

        validateScore(selfEvaluation.getQ1(), "Pregunta 1");
        validateScore(selfEvaluation.getQ2(), "Pregunta 2");
        validateScore(selfEvaluation.getQ3(), "Pregunta 3");
        validateScore(selfEvaluation.getQ4(), "Pregunta 4");
        validateScore(selfEvaluation.getQ5(), "Pregunta 5");
        validateScore(selfEvaluation.getQ6(), "Pregunta 6");
        validateScore(selfEvaluation.getQ7(), "Pregunta 7");
        validateScore(selfEvaluation.getQ8(), "Pregunta 8");
        validateScore(selfEvaluation.getQ9(), "Pregunta 9");
        validateScore(selfEvaluation.getQ10(), "Pregunta 10");
    }

    private static void validateScore(int score, String questionName) throws ManagerException {
        if (score < MINIMUM_QUESTION_SCORE || score > MAXIMUM_QUESTION_SCORE) {
            throw new ManagerException("El valor de " + questionName + " debe estar entre 1 y 5.");
        }
    }
}