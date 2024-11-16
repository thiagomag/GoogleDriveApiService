package br.com.thiagomagdalena.googledriveapiservice.usecase;

import br.com.thiagomagdalena.googledriveapiservice.dto.DeleteResourceRequest;
import reactor.core.publisher.Mono;

public interface DeleteResourceUseCase extends UseCase<DeleteResourceRequest, Mono<Void>> {
}
