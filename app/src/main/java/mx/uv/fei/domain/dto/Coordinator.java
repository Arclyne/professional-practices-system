package mx.uv.fei.domain.dto;

import java.util.Objects;

public class Coordinator extends User {

    public Coordinator() {
        super();
    }

    @Override
    public boolean equals(Object objectToCompare) {
        if (this == objectToCompare) {
            return true;
        }

        if (objectToCompare == null || getClass() != objectToCompare.getClass()) {
            return false;
        }

        return super.equals(objectToCompare);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
}