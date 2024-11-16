package br.com.thiagomagdalena.googledriveapiservice.usecase.impl;

import br.com.thiagomagdalena.googledriveapiservice.dto.GoogleDriveDetailsResponse;
import br.com.thiagomagdalena.googledriveapiservice.service.GoogleDriveService;
import br.com.thiagomagdalena.googledriveapiservice.usecase.GetGoogleDriveDetailsUseCase;
import com.google.api.services.drive.model.About;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetGoogleDriveDetailsUseCaseImpl implements GetGoogleDriveDetailsUseCase {

    private final GoogleDriveService googleDriveService;

    @Override
    public Mono<GoogleDriveDetailsResponse> execute(String projectId) {
        return googleDriveService.getDriveService(projectId)
                .flatMap(driveService -> Mono.fromCallable(() -> {
                    About about = driveService.about().get()
                            .setFields("user, storageQuota, maxUploadSize, importFormats, exportFormats")
                            .execute();

                    final var totalSpace = about.getStorageQuota().getLimit();
                    final var usedSpace = about.getStorageQuota().getUsage();
                    final var trashSpace = about.getStorageQuota().getUsageInDriveTrash();
                    final var availableSpace = totalSpace - usedSpace - trashSpace;

                    return GoogleDriveDetailsResponse.builder()
                            .userName(about.getUser().getDisplayName())
                            .userEmail(about.getUser().getEmailAddress())
                            .userStorageQuota(bytesToGigabytes(about.getStorageQuota().getLimit()))
                            .userStorageUsed(bytesToGigabytes(about.getStorageQuota().getUsage()))
                            .userStorageInTrash(bytesToGigabytes(about.getStorageQuota().getUsageInDriveTrash()))
                            .userStorageAvailable(bytesToGigabytes(availableSpace))
                            .maxUploadSize(bytesToGigabytes(about.getMaxUploadSize()))
                            .build();
                }))
                .onErrorResume(e -> Mono.error(new RuntimeException("Erro ao obter detalhes do Google Drive", e)));
    }

    private Double bytesToGigabytes(long bytes) {
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }
}
