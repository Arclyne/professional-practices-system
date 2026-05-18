package mx.uv.fei.domain.common;

import java.io.File;
import java.util.List;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.exceptions.ManagerException;

public interface IPractitionerParser {
    List<Practitioner> parsePractitioners(File file) throws ManagerException;
}