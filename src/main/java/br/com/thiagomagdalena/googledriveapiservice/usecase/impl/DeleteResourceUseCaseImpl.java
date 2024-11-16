package br.com.thiagomagdalena.googledriveapiservice.usecase.impl;

import br.com.thiagomagdalena.googledriveapiservice.dto.DeleteResourceRequest;
import br.com.thiagomagdalena.googledriveapiservice.service.GoogleDriveService;
import br.com.thiagomagdalena.googledriveapiservice.usecase.DeleteResourceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DeleteResourceUseCaseImpl implements DeleteResourceUseCase {

    private final GoogleDriveService googleDriveService;

    @Override
    public Mono<Void> execute(DeleteResourceRequest deleteResourceRequest) {
        final var projectId = deleteResourceRequest.getProjectId();
        final var resourceId = deleteResourceRequest.getResourceId();
        return googleDriveService.getDriveService(projectId)
                .flatMap(driveService -> Mono.fromCallable(() -> driveService.files().delete(resourceId).execute()))
                .onErrorResume(e -> Mono.error(new RuntimeException("Erro ao excluir o recurso", e)))
                .then();
    }
}
