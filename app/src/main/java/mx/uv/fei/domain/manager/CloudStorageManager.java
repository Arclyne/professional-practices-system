package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.common.validators.FileValidator;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CloudStorageManager {

    private static final Logger log = LoggerFactory.getLogger(CloudStorageManager.class);

    private static final String ONEDRIVE_ACCESS_TOKEN = "";
    private static final boolean IS_AZURE_CONFIGURED = false;
    private static final String ONEDRIVE_UPLOAD_ENDPOINT =
            "https://graph.microsoft.com/v1.0/me/drive/root:/EvidenciasPracticas/";
    private static final String ONEDRIVE_VIEW_BASE_URL = "https://onedrive.live.com/view?file=";
    private static final String SIMULATOR_DIRECTORY_NAME = "SimuladorOneDrive_FEI";
    private static final String WHITESPACE_REGEX = "\\s+";
    private static final String UNDERSCORE = "_";
    private static final int UNIQUE_PREFIX_START = 0;
    private static final int UNIQUE_PREFIX_LENGTH = 8;
    private static final int CONNECTION_TIMEOUT_SECONDS = 15;
    private static final int REQUEST_TIMEOUT_SECONDS = 60;

    public String uploadEvidenceFile(File file) throws ManagerException {
        FileValidator.validateFileSize(file);
        if (IS_AZURE_CONFIGURED) {
            return uploadToMicrosoftGraph(file);
        } else {
            return simulateOneDriveUpload(file);
        }
    }

    private String uploadToMicrosoftGraph(File file) throws ManagerException {
        String uniqueFileName = buildUniqueFileName(file.getName());

        try {
            HttpResponse<String> response = sendUploadRequest(file, uniqueFileName);
            return resolveUploadResponse(response, uniqueFileName);
        } catch (IOException e) {
            log.error("Falló la subida del documento al almacenamiento en la nube.", e);
            throw new ManagerException("No se pudo subir el documento. Revisa tu conexión a internet e inténtalo de nuevo.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("La subida del documento al almacenamiento en la nube fue interrumpida.", e);
            throw new ManagerException("La subida del documento fue interrumpida. Inténtalo de nuevo.", e);
        }
    }

    private HttpResponse<String> sendUploadRequest(File file, String uniqueFileName) throws IOException, InterruptedException {
        String endpoint = ONEDRIVE_UPLOAD_ENDPOINT + uniqueFileName + ":/content";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .header("Authorization", "Bearer " + ONEDRIVE_ACCESS_TOKEN)
                .header("Content-Type", "application/octet-stream")
                .PUT(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String resolveUploadResponse(HttpResponse<String> response, String uniqueFileName) throws ManagerException {
        int statusCode = response.statusCode();
        if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_CREATED) {
            return ONEDRIVE_VIEW_BASE_URL + uniqueFileName;
        }
        throw new ManagerException("El servidor de almacenamiento rechazó la subida del documento. Inténtalo más tarde.");
    }

    private String simulateOneDriveUpload(File file) throws ManagerException {
        try {
            Path uploadDirectory = resolveSimulatorDirectory();
            Path destinationPath = uploadDirectory.resolve(buildUniqueFileName(file.getName()));
            Files.copy(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            return destinationPath.toUri().toString();
        } catch (IOException e) {
            log.error("No se pudo guardar el documento en el almacenamiento local.", e);
            throw new ManagerException("No se pudo guardar el documento en el almacenamiento.", e);
        }
    }

    private Path resolveSimulatorDirectory() throws IOException {
        Path uploadDirectory = Paths.get(System.getProperty("user.home"), SIMULATOR_DIRECTORY_NAME);
        if (!Files.exists(uploadDirectory)) {
            Files.createDirectories(uploadDirectory);
        }
        return uploadDirectory;
    }

    private String buildUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString().substring(UNIQUE_PREFIX_START, UNIQUE_PREFIX_LENGTH) +
                UNDERSCORE +
                originalFileName.replaceAll(WHITESPACE_REGEX, UNDERSCORE);
    }
}