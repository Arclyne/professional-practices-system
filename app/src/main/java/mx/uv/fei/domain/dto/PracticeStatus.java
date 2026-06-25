package mx.uv.fei.domain.dto;

public record PracticeStatus(boolean enrolled, boolean periodConcluded, boolean finished,
                             Double finalGrade, int opportunityNumber, boolean secondOpportunity) {

    public boolean allowsSubmissions() {
        return enrolled && !periodConcluded && !finished;
    }
}
