package br.com.thiagomagdalena.googledriveapiservice.usecase;

import br.com.thiagomagdalena.googledriveapiservice.dto.CreateFolderRequest;
import br.com.thiagomagdalena.googledriveapiservice.dto.FolderResponse;
import reactor.core.publisher.Mono;

public interface CreateFolderUseCase extends UseCase<CreateFolderRequest, Mono<FolderResponse>> {
}
