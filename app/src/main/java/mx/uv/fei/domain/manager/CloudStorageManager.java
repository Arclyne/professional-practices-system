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

    private static final String ONEDRIVE_ACCESS_TOKEN = "EwBoBMl6BAAU9BatlgMxts2T1B5e3Mucgfs4jcAAATAJaaqMAzBMuwfG8CoEN8bAsa8l4wgD62FrzmZq0ZgMP57h4f6I3A+JNDwbzoMOS6Pnzfq5ITix6pCjWzIfsGwtYAvs263Gu1LVXaVpDcF6GFCl+TYtTnylQMaOAoemkK3m3yWryRBVtDdp5UexJZF9e1XmDanN5HrIDRbcJLDuK0ZCU10yK6sjHqouvhl2QtT+zK+Vh3dAtZq8YcNxRtmei4qGhx7PhtRj3/AxWEY028sPSWje58NYXbpsXgwabF+GAOEAJimXNA6dvgrxxCm0oXSUc+4VPyC+G6mMYv9zblN5g8ddA3Y7ltv/MfF4sebe5YeG7ILHYVmIsFYZguYQZgAAELdP0Ofgn1bgUd4e9EroEwYwAzTqYmAlNiLIAh/trM4CIR4g1S+ZPJrp2a9U8fxphMrlnWzROQHO/3P8eyxuVjVDuCNnFtegYAH9dQOGpU5dtGEAA5uxUPv6sefCE/r/sO1I3yJ0Wzy2sGSV3pvnIfzaBNs93WA9oGqYZ6MoquWfnkDFQkLFgI6vkRY2QhhaLdjiJmWNOBR1N4l/5xQYvvYMqC5XkoCaLSRMYWPyNABewYW5dhdav+wtpv9+EIB75dInnDHYZaPRPdmxGVKaHUWf5bNprSIdRdXSjb1Fq6vaBAFh949oNPRlE81f39i4QhXmRnIwkl/eUPRe4/qM1KNXYR72xkS1LauYemI9UBBwdzROpmfrv8QSZ98u6qCQuv4wdJU4ngjS5NFK+9kEOsPj33hXLSmaTnSPguoW8Ug9c47z7iVTnZnBAP8oSzCPgxNQhpz/eA3KQCzcAU/HmAtm2IOKFc9zUBYM1byx1WyqJNvYNyebxDBgQScy92Br1JkFwyXC8fFljki9UJLJH928b+EuQ078i+GCm9LNxxa/ijG2QssqCobf3+kJms8qpV9pCNUWtFQA4GJ8bSZDqS9POVviLNqbR2RzNSfi86UwJqjh2EGaemOBzITCnh9OJFUVOef/9E+fLGY0czGT1IMs3KAouDaYWbHRljLWqnf7XIlmREkRP6Tn9oZcWfZ9S7b+1vsdVFVSruzVgNXCv2lEMHKsUUMc1xfMjWDTie6EKDuafUQPLZG5h34A7TK8VHvje2escEX/55MXeaa5doOgVr9HeZNrpEhWuw+HeCrVKs8iP8YN/oikLEnprTtFKn66Mu6C+wV6jwha3tw3tW1Yfbs2g37HeDcIcubm0Gt9qrtX7707ZjflHbEon+9NRrYNcPhEeAlwkF8zZpqmfUsoXmUdtX6fUyD5qxScgGeTv2d9k6L7wskOlNu4HMbUXU4fD8b7G7Y8Ho+boksFoxs18x+a9bakSGU4As70yNhLNA1iIlN1c2Hxsh5HOx3JW4anZp088HNXMuQIsAXqawdj6zmRhI2giUbQ+MmGuyCUPesTGanpKhwQj7eiiraYE+x6VNPUqtouXjWU/Hm8zLLtt5ED.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTAwMDAtYzAwMC0wMDAwMDAwMDAwMDAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8zYzkwNzY1MS1kOGM2LTRjYTYtYThhNC02YTI0MjQzMGU2NTMvIiwiaWF0IjoxNzgyMzIwMDIyLCJuYmYiOjE3ODIzMjAwMjIsImV4cCI6MTc4MjQwNjcyMiwiYWNjdCI6MCwiYWNyIjoiMSIsImFjcnMiOlsicGZkciJdLCJhaW8iOiJBVVFBdS84Y0FBQUF2NzJYQUNFWThJMTNIeGpQZE5RR2thSnh6UlZ1SmdVT1p2N0pXYUgzQXIvcEQyNWVDSFlzS2g4NzdGcUtVajVUc1ZRSkt6MVg5SlVHZDFSTWlzc1Ztdz09IiwiYW1yIjpbInB3ZCJdLCJhcHBfZGlzcGxheW5hbWUiOiJHcmFwaCBFeHBsb3JlciIsImFwcGlkIjoiZGU4YmM4YjUtZDlmOS00OGIxLWE4YWQtYjc0OGRhNzI1MDY0IiwiYXBwaWRhY3IiOiIwIiwiZmFtaWx5X25hbWUiOiJBR1VJTEFSIEhFUk5BTkRFWiIsImdpdmVuX25hbWUiOiJBTkdFTCBHQUJSSUVMIiwiaWR0eXAiOiJ1c2VyIiwiaXBhZGRyIjoiMjgwNjozNzA6NzI0ZDozZGY1OjQ4OGI6YmU1MjpjNzNkOmVjNzIiLCJuYW1lIjoiQUdVSUxBUiBIRVJOQU5ERVogQU5HRUwgR0FCUklFTCIsIm9pZCI6IjYxZTk0YWUxLTAxZjQtNDcxNy05YmUxLWU5MmY4NjA5MzQ2ZiIsIm9ucHJlbV9zaWQiOiJTLTEtNS0yMS0zMTY5OTgxNTg5LTExNTg5Mjc5MjgtMzc1NjgxNjkwNS0zNzk0ODYiLCJwbGF0ZiI6IjUiLCJwdWlkIjoiMTAwMzIwMDM5RkM5OTdCMyIsInJoIjoiMS5BUndBVVhhUVBNYllwa3lvcEdva0pERG1Vd01BQUFBQUFBQUF3QUFBQUFBQUFBQUFBTTBjQUEuIiwic2NwIjoib3BlbmlkIHByb2ZpbGUgVXNlci5SZWFkIGVtYWlsIiwic2lkIjoiMDA0ZWM0MmEtNjZlMC0xYzI1LWNlZGYtMDNjMGY5YTYzOTYzIiwic2lnbmluX3N0YXRlIjpbImttc2kiXSwic3ViIjoialZRT0RCZUVqWW83M3Q0UXBrV1lfM2pBZ041TEVkUU5qcFhIRlp6N01PRSIsInRlbmFudF9yZWdpb25fc2NvcGUiOiJOQSIsInRpZCI6IjNjOTA3NjUxLWQ4YzYtNGNhNi1hOGE0LTZhMjQyNDMwZTY1MyIsInVuaXF1ZV9uYW1lIjoielMyNDAxMzMyNEBlc3R1ZGlhbnRlcy51di5teCIsInVwbiI6InpTMjQwMTMzMjRAZXN0dWRpYW50ZXMudXYubXgiLCJ1dGkiOiJiVkFaT0VqVWgwU2xJN3loemw4REFBIiwidmVyIjoiMS4wIiwid2lkcyI6WyJiNzlmYmY0ZC0zZWY5LTQ2ODktODE0My03NmIxOTRlODU1MDkiXSwieG1zX2FjZCI6MTQ3NDY2NDA3MywieG1zX2FjdF9mY3QiOiIzIDciLCJ4bXNfY2MiOlsiQ1AxIl0sInhtc19mdGQiOiJHUjJvbnRfaldObzlQaURxeGpjOHNTWWZjM1MtdGFZWW1RZkxRN052M1BZQmRYTnpiM1YwYUMxa2MyMXoiLCJ4bXNfaWRyZWwiOiIzMiAxIiwieG1zX3BmdGV4cCI6MTc4MjQ5MzEyMiwieG1zX3NzbSI6IjEiLCJ4bXNfc3QiOnsic3ViIjoiVDdLVWh6U1hLY3NXUnBLVzhGNk9NSXBuNTllZldVaVN4MUpRU0RjX3pRMCJ9LCJ4bXNfc3ViX2ZjdCI6IjMgMTgiLCJ4bXNfdGNkdCI6MTQyNTQ5MTY1NCwieG1zX3RudF9mY3QiOiIzIDEyIn0.TUGwGDsmrwdHllYrV3grJf32t_yLbL4LYx3kZXOj12yoolWMQV5uYSwL4YsXS9aXQBQkvvJ2iqD3lrYiIc8_-Zon4Hdvi54OP1rJEaPRZbNSOwwKmUPo-RQkgDAlhJ4P65FsWXd30c6Os6-c9juXDnT3pa9m27V1hAyQet_qyr8vmw77byi08UBAwN4efij5LeVXzChkBW9vKG_DFLuNk0GV32ZpG8bvzO4uLcojlSM_IFr_1QtAfW_KljXAXis-L5xWmIJtLY2cwnQk1TdVXAaN-aapXbSZaclVVclJbvReouEXSGyxao2lSH4iMIIYRPHJZN2j5F8WUCfHRdmyXQ";
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