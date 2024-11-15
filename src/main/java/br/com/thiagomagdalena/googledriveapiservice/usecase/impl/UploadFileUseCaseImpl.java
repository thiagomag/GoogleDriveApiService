package br.com.thiagomagdalena.googledriveapiservice.usecase.impl;

import br.com.thiagomagdalena.googledriveapiservice.dto.GoogleDriveApiResponse;
import br.com.thiagomagdalena.googledriveapiservice.dto.UploadFileRequest;
import br.com.thiagomagdalena.googledriveapiservice.service.GoogleDriveService;
import br.com.thiagomagdalena.googledriveapiservice.usecase.UploadFileUseCase;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UploadFileUseCaseImpl implements UploadFileUseCase {

    private final GoogleDriveService googleDriveService;

    @Override
    public Mono<GoogleDriveApiResponse> execute(UploadFileRequest uploadFileRequest) {
        final var filePath = uploadFileRequest.getFile();
        final var mimeType = uploadFileRequest.getMimeType();
        final var projectId = uploadFileRequest.getProjectId();
        final var folderId = uploadFileRequest.getFolderId();

        return googleDriveService.getDriveService(projectId)
                .flatMap(driveService -> {
                    final var fileMetadata = new File();
                    fileMetadata.setName(filePath.getName());
                    fileMetadata.setParents(Collections.singletonList(folderId));

                    final var mediaContent = new FileContent(mimeType, filePath);

                    return Mono.fromCallable(() -> driveService.files().create(fileMetadata, mediaContent)
                                    .setFields("id")
                                    .execute())
                            .flatMap(file -> {
                                final var permission = new Permission()
                                        .setType("anyone")
                                        .setRole("reader");
                                return Mono.fromCallable(() -> {
                                    driveService.permissions().create(file.getId(), permission).execute();
                                    return buildResponse(file);
                                });
                            });
                })
                .onErrorResume(e -> Mono.error(new RuntimeException("Erro ao fazer upload do arquivo", e)));
    }

    private GoogleDriveApiResponse buildResponse(File file) {
        return GoogleDriveApiResponse.builder()
                .url(GoogleDriveService.GOOGLE_DRIVE_URL + file.getId())
                .statusCode(201)
                .message("Arquivo enviado com sucesso")
                .build();
    }
}
