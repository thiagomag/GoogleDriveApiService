package br.com.thiagomagdalena.googledriveapiservice.usecase.impl;

import br.com.thiagomagdalena.googledriveapiservice.dto.FolderResponse;
import br.com.thiagomagdalena.googledriveapiservice.service.GoogleDriveService;
import br.com.thiagomagdalena.googledriveapiservice.usecase.GetAllFoldersUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetAllFoldersUseCaseImpl implements GetAllFoldersUseCase {

    private final GoogleDriveService googleDriveService;

    @Override
    public Flux<FolderResponse> execute(String projectId) {
        return googleDriveService.getDriveService(projectId)
                .flatMap(driveService -> Mono.fromCallable(() -> {
                    final var query = "mimeType='application/vnd.google-apps.folder' and trashed=false";

                    final var result = driveService.files().list()
                            .setQ(query)
                            .setFields("files(id, name)")
                            .execute();

                    return result.getFiles();
                }))
                .flatMapMany(Flux::fromIterable)
                .map(FolderResponse::adapt)
                .onErrorResume(e -> Flux.error(new RuntimeException("Erro ao listar pastas no Google Drive", e)));
    }
}
