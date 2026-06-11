package mx.uv.fei.domain.dto;

import java.util.Objects;

/**
 * Representa un usuario con rol de coordinador de prácticas profesionales.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public class Coordinator extends User {

    public Coordinator() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            Coordinator other = (Coordinator) obj;
            isEqual = Objects.equals(this.getEmail(), other.getEmail());
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail());
    }
}