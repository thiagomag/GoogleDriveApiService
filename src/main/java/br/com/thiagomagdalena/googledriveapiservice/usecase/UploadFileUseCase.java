package br.com.thiagomagdalena.googledriveapiservice.usecase;

import br.com.thiagomagdalena.googledriveapiservice.dto.GoogleDriveApiResponse;
import br.com.thiagomagdalena.googledriveapiservice.dto.UploadFileRequest;
import reactor.core.publisher.Mono;

public interface UploadFileUseCase extends UseCase<UploadFileRequest, Mono<GoogleDriveApiResponse>> {
}
