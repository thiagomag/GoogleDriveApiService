package br.com.thiagomagdalena.googledriveapiservice.repository;

import br.com.thiagomagdalena.googledriveapiservice.model.GoogleDriveApiCredentials;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface GoogleDriveApiCredentialsRepository extends ReactiveCrudRepository<GoogleDriveApiCredentials, String> {

    Mono<GoogleDriveApiCredentials> findGoogleDriveApiCredentialsByProjectId(String projectId);
}
