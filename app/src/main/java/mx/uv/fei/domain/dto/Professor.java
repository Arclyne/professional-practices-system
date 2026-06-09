package mx.uv.fei.domain.dto;

import java.util.Objects;

public class Professor extends User {

    public Professor() {
        super();
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            isEqual = super.equals(object);
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
}
