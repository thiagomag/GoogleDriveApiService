package br.com.thiagomagdalena.googledriveapiservice.usecase;

import br.com.thiagomagdalena.googledriveapiservice.dto.ShareResourceRequest;
import reactor.core.publisher.Mono;

public interface ShareResourceUseCase extends UseCase<ShareResourceRequest, Mono<String>> {
}
