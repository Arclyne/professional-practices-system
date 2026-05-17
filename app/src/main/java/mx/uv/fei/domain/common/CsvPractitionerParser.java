package mx.uv.fei.domain.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class CsvPractitionerParser implements IPractitionerParser {

    @Override
    public List<Practitioner> parsePractitioners(File file) throws ManagerException {
        List<Practitioner> parsedPractitioners = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeaderLine = true;

            while ((line = reader.readLine()) != null) {
                if (isHeaderLine) {
                    isHeaderLine = false;
                    continue;
                }

                String[] rawData = line.split(",", -1);
                if (rawData.length >= 5) {
                    parsedPractitioners.add(mapToPractitioner(rawData));
                }
            }
        } catch (Exception exception) {
            throw new ManagerException("Error al leer el archivo proporcionado.");
        }

        return parsedPractitioners;
    }

    private Practitioner mapToPractitioner(String[] rawData) {
        Practitioner practitioner = new Practitioner();

        String matriculaLimpia = rawData[0].replaceAll("[^a-zA-Z0-9]", "");
        practitioner.setEnrollment(matriculaLimpia);

        practitioner.setName(rawData[1].replace("\"", "").trim());
        practitioner.setLastName(rawData[2].replace("\"", "").trim());
        practitioner.setEmail(rawData[3].replace("\"", "").trim());
        practitioner.setGender(rawData[4].replace("\"", "").trim());

        String indigenousLanguage = (rawData.length > 5 && !rawData[5].trim().isEmpty())
                ? rawData[5].replace("\"", "").trim()
                : "Ninguna";
        practitioner.setIndigenousLanguage(indigenousLanguage);

        return practitioner;
    }
}
