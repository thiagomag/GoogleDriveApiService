package br.com.thiagomagdalena.googledriveapiservice.usecase.impl;

import br.com.thiagomagdalena.googledriveapiservice.dto.ShareResourceRequest;
import br.com.thiagomagdalena.googledriveapiservice.service.GoogleDriveService;
import br.com.thiagomagdalena.googledriveapiservice.usecase.ShareResourceUseCase;
import com.google.api.services.drive.model.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ShareResourceUseCaseImpl implements ShareResourceUseCase {

    private final GoogleDriveService googleDriveService;

    @Override
    public Mono<String> execute(ShareResourceRequest shareResourceRequest) {
        final var projectId = shareResourceRequest.getProjectId();
        final var resourceId = shareResourceRequest.getResourceId();
        final var email = shareResourceRequest.getEmailAddress();
        final var type = shareResourceRequest.getType();
        final var role = shareResourceRequest.getRole();
        return googleDriveService.getDriveService(projectId)
                .flatMap(driveService -> {
                    final var permission = new Permission();
                    if ("anyone".equalsIgnoreCase(type)) {
                        permission.setType(type).setRole(role);
                    } else {
                        permission.setType(type).setRole(role).setEmailAddress(email);
                    }

                    return Mono.fromCallable(() -> {
                        driveService.permissions().create(resourceId, permission).execute();
                        return "Permissão alterada com sucesso para o email " + email;
                    });
                });
    }
}