package br.com.thiagomagdalena.googledriveapiservice.usecase;

import br.com.thiagomagdalena.googledriveapiservice.dto.FolderResponse;
import reactor.core.publisher.Flux;

public interface GetAllFoldersUseCase extends UseCase<String, Flux<FolderResponse>> {
}
