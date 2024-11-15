package br.com.thiagomagdalena.googledriveapiservice.usecase;

import br.com.thiagomagdalena.googledriveapiservice.dto.FileResponse;
import br.com.thiagomagdalena.googledriveapiservice.dto.GetFilesFromAFolderRequestParams;
import reactor.core.publisher.Flux;

public interface GetFilesFromAFolderUseCase extends UseCase<GetFilesFromAFolderRequestParams, Flux<FileResponse>> {
}
