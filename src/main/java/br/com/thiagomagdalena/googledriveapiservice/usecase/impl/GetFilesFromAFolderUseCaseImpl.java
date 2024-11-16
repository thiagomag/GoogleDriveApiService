package br.com.thiagomagdalena.googledriveapiservice.usecase.impl;

import br.com.thiagomagdalena.googledriveapiservice.dto.FileResponse;
import br.com.thiagomagdalena.googledriveapiservice.dto.GetFilesFromAFolderRequestParams;
import br.com.thiagomagdalena.googledriveapiservice.service.GoogleDriveService;
import br.com.thiagomagdalena.googledriveapiservice.usecase.GetFilesFromAFolderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetFilesFromAFolderUseCaseImpl implements GetFilesFromAFolderUseCase {

    private final GoogleDriveService googleDriveService;

    @Override
    public Flux<FileResponse> execute(GetFilesFromAFolderRequestParams getFilesFromAFolderRequestParams) {
        final var folderId = getFilesFromAFolderRequestParams.getFolderId();
        final var projectId = getFilesFromAFolderRequestParams.getProjectId();
        return googleDriveService.getDriveService(projectId)
                .flatMap(driveService -> {
                    String query;
                    if (folderId == null) {
                        query = "trashed = false";
                    } else {
                        query = "'" + folderId + "' in parents and trashed = false";
                    }

                    return Mono.fromCallable(() -> driveService.files().list()
                            .setQ(query)
                            .setFields("files(id, name, mimeType, webViewLink, webContentLink)")
                            .execute()
                            .getFiles()
                            .stream()
                            .map(FileResponse::adapt)
                            .toList());
                })
                .flatMapMany(Flux::fromIterable)
                .onErrorResume(e -> Flux.error(new RuntimeException("Erro ao listar arquivos na pasta", e)));
    }
}
