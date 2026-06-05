package mx.uv.fei.domain.manager;

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

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class CloudStorageManager {

    private static final String ONEDRIVE_ACCESS_TOKEN = "";
    private static final boolean IS_AZURE_CONFIGURED = false;

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
            String fileName = UUID.randomUUID() + "_" + file.getName().replaceAll("\\s+", "_");
            String endpoint = "https://graph.microsoft.com/v1.0/me/drive/root:/EvidenciasPracticas/" + fileName + ":/content";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + ONEDRIVE_ACCESS_TOKEN)
                    .header("Content-Type", "application/octet-stream")
                    .PUT(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return "https://onedrive.live.com/view?file=" + fileName;
            } else {
                throw new ManagerException("Microsoft Graph rechazó la subida. Código: " + response.statusCode());
            }

        } catch (Exception e) {
            throw new ManagerException("Fallo la conexión con los servidores de Microsoft.", e);
        }
    }

    private String simulateOneDriveUpload(File file) throws ManagerException {
        try {
            String homeDirectory = System.getProperty("user.home");
            Path uploadDirectory = Paths.get(homeDirectory, "SimuladorOneDrive_FEI");

            if (!Files.exists(uploadDirectory)) {
                Files.createDirectories(uploadDirectory);
            }

            String uniqueFileName = UUID.randomUUID().toString().substring(0, 8) + "_" + file.getName().replaceAll("\\s+", "_");
            Path destinationPath = uploadDirectory.resolve(uniqueFileName);

            Files.copy(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            return destinationPath.toUri().toString();

        } catch (Exception e) {
            throw new ManagerException("Error al simular la subida del archivo al servidor.", e);
        }
    }
}