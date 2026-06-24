package mx.uv.fei.domain.dto;

/**
 * Resultado inmutable de la verificación de requisitos para calificar a un practicante.
 *
 * El profesor solo puede asignar la calificación final cuando los tres requisitos
 * de cierre de prácticas se cumplen.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
public record GradingEligibility(boolean finalDocumentsAccepted, boolean finalReportAccepted,
                                 boolean selfEvaluationAccepted) {

    public boolean isEligible() {
        return finalDocumentsAccepted && finalReportAccepted && selfEvaluationAccepted;
    }
}
