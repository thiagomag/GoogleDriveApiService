package br.com.thiagomagdalena.googledriveapiservice.usecase.impl;

import br.com.thiagomagdalena.googledriveapiservice.dto.CreateFolderRequest;
import br.com.thiagomagdalena.googledriveapiservice.dto.FolderResponse;
import br.com.thiagomagdalena.googledriveapiservice.service.GoogleDriveService;
import br.com.thiagomagdalena.googledriveapiservice.usecase.CreateFolderUseCase;
import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CreateFolderUseCaseImpl implements CreateFolderUseCase {

    private final GoogleDriveService googleDriveService;

    @Override
    public Mono<FolderResponse> execute(CreateFolderRequest createFolderRequest) {
        final var projectId = createFolderRequest.getProjectId();
        final var parentFolderId = createFolderRequest.getParentFolderId();
        final var folderName = createFolderRequest.getName();
        return googleDriveService.getDriveService(projectId)
                .flatMap(driveService -> Mono.fromCallable(() -> {
                            final var folder = new File();
                            folder.setName(folderName);
                            folder.setParents(Collections.singletonList(parentFolderId));
                            folder.setMimeType("application/vnd.google-apps.folder");

                            return driveService.files().create(folder)
                                    .setFields("id, name")
                                    .execute();
                        })
                        .map(FolderResponse::adapt));
    }
}