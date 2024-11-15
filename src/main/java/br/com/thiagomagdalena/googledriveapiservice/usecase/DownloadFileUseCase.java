package br.com.thiagomagdalena.googledriveapiservice.usecase;

import br.com.thiagomagdalena.googledriveapiservice.dto.DownloadFileRequest;
import reactor.core.publisher.Mono;

public interface DownloadFileUseCase extends UseCase<DownloadFileRequest, Mono<byte[]>> {
}
