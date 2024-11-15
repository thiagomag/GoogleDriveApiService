package br.com.thiagomagdalena.googledriveapiservice.usecase.impl;

import br.com.thiagomagdalena.googledriveapiservice.dto.DownloadFileRequest;
import br.com.thiagomagdalena.googledriveapiservice.service.GoogleDriveService;
import br.com.thiagomagdalena.googledriveapiservice.usecase.DownloadFileUseCase;
import com.google.api.client.http.HttpResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class DownloadFileUseCaseImpl implements DownloadFileUseCase {

    private final GoogleDriveService googleDriveService;

    @Override
    public Mono<byte[]> execute(DownloadFileRequest downloadFileRequest) {
        final var fileId = downloadFileRequest.getFileId();
        final var projectId = downloadFileRequest.getProjectId();
        return googleDriveService.getDriveService(projectId)
                .flatMap(driveService -> Mono.fromCallable(() -> {
                            final var file = driveService.files().get(fileId).execute();
                            final var mimeType = file.getMimeType();

                            final var outputStream = new ByteArrayOutputStream();

                            if (mimeType != null && mimeType.startsWith("application/vnd.google-apps")) {
                                driveService.files().export(fileId, "application/pdf")
                                        .executeMediaAndDownloadTo(outputStream);
                            } else {
                                driveService.files().get(fileId)
                                        .executeMediaAndDownloadTo(outputStream);
                            }

                            return outputStream.toByteArray();
                        })
                        .onErrorResume(e -> {
                            if (e instanceof HttpResponseException) {
                                return Mono.error(new RuntimeException("Erro ao baixar o arquivo. Verifique o ID e permissões."));
                            }
                            return Mono.error(new RuntimeException("Erro desconhecido ao baixar o arquivo", e));
                        }));
    }
}
