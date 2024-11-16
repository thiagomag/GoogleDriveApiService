package br.com.thiagomagdalena.googledriveapiservice.usecase;

import br.com.thiagomagdalena.googledriveapiservice.dto.GoogleDriveDetailsResponse;
import reactor.core.publisher.Mono;

public interface GetGoogleDriveDetailsUseCase extends UseCase<String, Mono<GoogleDriveDetailsResponse>> {
}
