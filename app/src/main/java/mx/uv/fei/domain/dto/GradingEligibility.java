package mx.uv.fei.domain.dto;

public record GradingEligibility(boolean finalDocumentsAccepted, boolean finalReportAccepted,
                                 boolean selfEvaluationAccepted) {

    public boolean isEligible() {
        return finalDocumentsAccepted && finalReportAccepted && selfEvaluationAccepted;
    }
}
