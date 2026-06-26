package mx.uv.fei.domain.dto;

import java.util.Objects;


public class Administrator extends User {

    public Administrator() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            Administrator other = (Administrator) obj;
            isEqual = Objects.equals(this.getEmail(), other.getEmail());
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail());
    }
}