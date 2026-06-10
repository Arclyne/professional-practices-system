package mx.uv.fei.domain.common;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvPractitionerParser implements IPractitionerParser {

    private static final int MINIMUM_COLUMNS_REQUIRED = 5;
    private static final int ENROLLMENT_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int LAST_NAME_INDEX = 2;
    private static final int EMAIL_INDEX = 3;
    private static final int GENDER_INDEX = 4;
    private static final int LANGUAGE_INDEX = 5;
    private static final int GROUP_INDEX = 6;

    private static final String CSV_SEPARATOR = ",";
    private static final String QUOTES_REGEX = "\"";
    private static final String NON_ALPHANUMERIC_REGEX = "[^a-zA-Z0-9]";
    private static final String EMPTY_STRING = "";
    private static final String DEFAULT_INDIGENOUS_LANGUAGE = "Ninguna";

    @Override
    public List<Practitioner> parsePractitioners(File file) throws ManagerException {
        List<Practitioner> parsedPractitioners = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            parsedPractitioners = processFileLines(reader);
        } catch (Exception e) {
            throw new ManagerException("Error al leer el archivo proporcionado.", e);
        }

        return parsedPractitioners;
    }

    private List<Practitioner> processFileLines(BufferedReader reader) throws Exception {
        List<Practitioner> practitioners = new ArrayList<>();
        String line;
        boolean isHeaderLine = true;

        while ((line = reader.readLine()) != null) {
            if (isHeaderLine) {
                isHeaderLine = false;
            } else {
                processLine(line, practitioners);
            }
        }

        return practitioners;
    }

    private void processLine(String line, List<Practitioner> practitioners) {
        String[] rawData = line.split(CSV_SEPARATOR, -1);
        if (rawData.length >= MINIMUM_COLUMNS_REQUIRED) {
            practitioners.add(mapToPractitioner(rawData));
        }
    }

    private Practitioner mapToPractitioner(String[] rawData) {
        Practitioner practitioner = new Practitioner();
        practitioner.setEnrollment(rawData[ENROLLMENT_INDEX].replaceAll(NON_ALPHANUMERIC_REGEX, EMPTY_STRING));
        practitioner.setName(cleanField(rawData[NAME_INDEX]));
        practitioner.setLastName(cleanField(rawData[LAST_NAME_INDEX]));
        practitioner.setEmail(cleanField(rawData[EMAIL_INDEX]));
        setGenderSafe(practitioner, rawData[GENDER_INDEX]);
        setLanguageSafe(practitioner, rawData);
        setGroupSafe(practitioner, rawData);
        return practitioner;
    }

    private String cleanField(String rawField) {
        return rawField.replace(QUOTES_REGEX, EMPTY_STRING).trim();
    }

    private void setGenderSafe(Practitioner practitioner, String rawGender) {
        try {
            practitioner.setGender(Gender.fromDisplayValue(cleanField(rawGender)));
        } catch (IllegalArgumentException e) {
            practitioner.setGender(Gender.OTHER);
        }
    }

    private void setLanguageSafe(Practitioner practitioner, String[] rawData) {
        String indigenousLanguage = DEFAULT_INDIGENOUS_LANGUAGE;
        if (rawData.length > LANGUAGE_INDEX && !rawData[LANGUAGE_INDEX].trim().isEmpty()) {
            indigenousLanguage = cleanField(rawData[LANGUAGE_INDEX]);
        }
        practitioner.setIndigenousLanguage(indigenousLanguage);
    }

    private void setGroupSafe(Practitioner practitioner, String[] rawData) {
        practitioner.setGroupId(parseGroupId(rawData));
    }

    private Integer parseGroupId(String[] rawData) {
        Integer groupId = null;
        if (rawData.length > GROUP_INDEX && !rawData[GROUP_INDEX].trim().isEmpty()) {
            groupId = Integer.parseInt(cleanField(rawData[GROUP_INDEX]));
        }
        return groupId;
    }
}