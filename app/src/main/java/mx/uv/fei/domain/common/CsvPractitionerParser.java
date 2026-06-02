package mx.uv.fei.domain.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.exceptions.ManagerException;

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

    private static final String DEFAULT_LANGUAGE = "Ninguna";
    private static final String MSG_PARSE_ERROR = "Error al leer el archivo proporcionado.";

    @Override
    public List<Practitioner> parsePractitioners(File file) throws ManagerException {
        List<Practitioner> parsedPractitioners = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            parsedPractitioners = processFileLines(reader);
        } catch (Exception exception) {
            throw new ManagerException(MSG_PARSE_ERROR, exception);
        }

        return parsedPractitioners;
    }

    private List<Practitioner> processFileLines(BufferedReader reader) throws Exception {
        List<Practitioner> practitioners = new ArrayList<>();
        String line;
        boolean isHeaderLine = true;

        while ((line = reader.readLine()) != null) {
            if (!isHeaderLine) {
                processLine(line, practitioners);
            } else {
                isHeaderLine = false;
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

        String cleanEnrollment = rawData[ENROLLMENT_INDEX].replaceAll(NON_ALPHANUMERIC_REGEX, EMPTY_STRING);
        practitioner.setEnrollment(cleanEnrollment);

        practitioner.setName(rawData[NAME_INDEX].replace(QUOTES_REGEX, EMPTY_STRING).trim());
        practitioner.setLastName(rawData[LAST_NAME_INDEX].replace(QUOTES_REGEX, EMPTY_STRING).trim());
        practitioner.setEmail(rawData[EMAIL_INDEX].replace(QUOTES_REGEX, EMPTY_STRING).trim());

        setGenderSafe(practitioner, rawData[GENDER_INDEX]);
        setLanguageSafe(practitioner, rawData);
        setGroupSafe(practitioner, rawData);

        return practitioner;
    }

    private void setGenderSafe(Practitioner practitioner, String parsedGenderRaw) {
        String parsedGender = parsedGenderRaw.replace(QUOTES_REGEX, EMPTY_STRING).trim();

        try {
            practitioner.setGender(Gender.fromDisplayValue(parsedGender));
        } catch (IllegalArgumentException exception) {
            practitioner.setGender(Gender.OTHER);
        }
    }

    private void setLanguageSafe(Practitioner practitioner, String[] rawData) {
        String indigenousLanguage = DEFAULT_LANGUAGE;

        if (rawData.length > LANGUAGE_INDEX && !rawData[LANGUAGE_INDEX].trim().isEmpty()) {
            indigenousLanguage = rawData[LANGUAGE_INDEX].replace(QUOTES_REGEX, EMPTY_STRING).trim();
        }

        practitioner.setIndigenousLanguage(indigenousLanguage);
    }

    private void setGroupSafe(Practitioner practitioner, String[] rawData) {
        Integer groupId = null;

        if (rawData.length > GROUP_INDEX && !rawData[GROUP_INDEX].trim().isEmpty()) {
            String groupString = rawData[GROUP_INDEX].replace(QUOTES_REGEX, EMPTY_STRING).trim();

            try {
                groupId = Integer.parseInt(groupString);
            } catch (NumberFormatException exception) {
                groupId = null;
            }
        }

        practitioner.setGroupId(groupId);
    }
}