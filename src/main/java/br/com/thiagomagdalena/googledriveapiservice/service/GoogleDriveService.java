package br.com.thiagomagdalena.googledriveapiservice.service;

import br.com.thiagomagdalena.googledriveapiservice.model.GoogleDriveApiCredentials;
import br.com.thiagomagdalena.googledriveapiservice.repository.GoogleDriveApiCredentialsRepository;
import br.com.thiagomagdalena.googledriveapiservice.utils.PrivateKeyUtil;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivateKey;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleDriveService {

    private static final String APPLICATION_NAME = "Google-Drive-API";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    public static final String GOOGLE_DRIVE_URL = "https://drive.google.com/uc?id=";

    private final GoogleDriveApiCredentialsRepository googleDriveApiCredentialsRepository;

    public Mono<Drive> getDriveService(String projectId) {
        return googleDriveApiCredentialsRepository.findGoogleDriveApiCredentialsByProjectId(projectId)
                .flatMap(credentials -> Mono.fromCallable(() -> {
                    final var privateKey = PrivateKeyUtil.getPrivateKeyFromString(credentials.getPrivateKey());
                    final var googleCredentials = buildGoogleCredentials(credentials, privateKey);

                    return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, new HttpCredentialsAdapter(googleCredentials))
                            .setApplicationName(APPLICATION_NAME)
                            .build();
                }))
                .onErrorResume(e -> Mono.error(new RuntimeException("Erro ao criar serviço do Google Drive", e)));
    }

    private ServiceAccountCredentials buildGoogleCredentials(GoogleDriveApiCredentials credentials, PrivateKey privateKey) throws URISyntaxException {
        return ServiceAccountCredentials.newBuilder()
                .setProjectId(credentials.getProjectId())
                .setPrivateKeyId(credentials.getPrivateKeyId())
                .setPrivateKey(privateKey)
                .setClientEmail(credentials.getClientEmail())
                .setTokenServerUri(new URI(credentials.getTokenUri()))
                .setScopes(Collections.singleton(DriveScopes.DRIVE_FILE))
                .build();
    }
}