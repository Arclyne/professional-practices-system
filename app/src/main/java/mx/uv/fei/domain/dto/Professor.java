package mx.uv.fei.domain.dto;

import java.util.Objects;

public class Professor extends User {
    public Professor() {
        super();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Professor that = (Professor) object;

        return Objects.equals(this.getName(), that.getName()) &&
                Objects.equals(this.getLastName(), that.getLastName());
    }
}