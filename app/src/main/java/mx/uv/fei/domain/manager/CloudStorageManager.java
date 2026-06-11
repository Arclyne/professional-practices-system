package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class CloudStorageManager {

    private static final String ONEDRIVE_ACCESS_TOKEN = "";
    private static final boolean IS_AZURE_CONFIGURED = false;
    private static final String ONEDRIVE_UPLOAD_ENDPOINT =
            "https://graph.microsoft.com/v1.0/me/drive/root:/EvidenciasPracticas/";
    private static final String ONEDRIVE_VIEW_BASE_URL = "https://onedrive.live.com/view?file=";
    private static final String SIMULATOR_DIRECTORY_NAME = "SimuladorOneDrive_FEI";
    private static final String WHITESPACE_REGEX = "\\s+";
    private static final String UNDERSCORE = "_";

    public String uploadEvidenceFile(File file) throws ManagerException {
        if (file == null || !file.exists()) {
            throw new ManagerException("El archivo seleccionado no es válido o no existe.");
        }
        if (IS_AZURE_CONFIGURED) {
            return uploadToMicrosoftGraph(file);
        } else {
            return simulateOneDriveUpload(file);
        }
    }

    private String uploadToMicrosoftGraph(File file) throws ManagerException {
        try {
            String uniqueFileName = buildUniqueFileName(file.getName());
            String endpoint = ONEDRIVE_UPLOAD_ENDPOINT + uniqueFileName + ":/content";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + ONEDRIVE_ACCESS_TOKEN)
                    .header("Content-Type", "application/octet-stream")
                    .PUT(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return ONEDRIVE_VIEW_BASE_URL + uniqueFileName;
            } else {
                throw new ManagerException("Microsoft Graph rechazó la subida. Código: " + response.statusCode());
            }
        } catch (ManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new ManagerException("Falló la conexión con los servidores de Microsoft.", e);
        }
    }

    private String simulateOneDriveUpload(File file) throws ManagerException {
        try {
            Path uploadDirectory = resolveSimulatorDirectory();
            Path destinationPath = uploadDirectory.resolve(buildUniqueFileName(file.getName()));
            Files.copy(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            return destinationPath.toUri().toString();
        } catch (Exception e) {
            throw new ManagerException("Error al simular la subida del archivo al servidor.", e);
        }
    }

    private Path resolveSimulatorDirectory() throws Exception {
        Path uploadDirectory = Paths.get(System.getProperty("user.home"), SIMULATOR_DIRECTORY_NAME);
        if (!Files.exists(uploadDirectory)) {
            Files.createDirectories(uploadDirectory);
        }
        return uploadDirectory;
    }

    private String buildUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString().substring(0, 8) +
                UNDERSCORE +
                originalFileName.replaceAll(WHITESPACE_REGEX, UNDERSCORE);
    }
}