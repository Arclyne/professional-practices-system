package mx.uv.fei.domain.dto;

import java.util.Objects;

public class Coordinator extends User {

    public Coordinator() {
        super();
    }

    @Override
    public boolean equals(Object objectToCompare) {
        boolean isEqual = false;

        if (this == objectToCompare) {
            isEqual = true;
        } else if (objectToCompare != null && getClass() == objectToCompare.getClass()) {
            isEqual = super.equals(objectToCompare);
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
}
