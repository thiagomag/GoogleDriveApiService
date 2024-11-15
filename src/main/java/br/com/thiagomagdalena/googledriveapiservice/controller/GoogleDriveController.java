package br.com.thiagomagdalena.googledriveapiservice.controller;

import br.com.thiagomagdalena.googledriveapiservice.dto.*;
import br.com.thiagomagdalena.googledriveapiservice.usecase.DownloadFileUseCase;
import br.com.thiagomagdalena.googledriveapiservice.usecase.GetFilesFromAFolderUseCase;
import br.com.thiagomagdalena.googledriveapiservice.usecase.UploadFileUseCase;
import com.google.common.net.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/google-drive")
public class GoogleDriveController {

    private final UploadFileUseCase uploadFileUseCase;
    private final DownloadFileUseCase downloadFileUseCase;
    private final GetFilesFromAFolderUseCase getFilesFromAFolderUseCase;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<GoogleDriveApiResponse>> uploadFile(@RequestPart("file") Mono<FilePart> filePartMono,
                                                                   @RequestPart("uploadFileRequest") UploadFileRequest uploadFileRequest) {
        return filePartMono.flatMap(filePart -> {
            final var convFile = new File(filePart.filename());
            uploadFileRequest.setFile(convFile);
            uploadFileRequest.setMimeType(Objects.requireNonNull(filePart.headers().getContentType()).toString());
            return filePart.transferTo(convFile)
                    .then(uploadFileUseCase.execute(uploadFileRequest)
                            .map(ResponseEntity::ok)
                            .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(GoogleDriveApiResponse.builder()
                                            .statusCode(500)
                                            .message("Erro ao fazer upload do arquivo")
                                    .build()))));
        });
    }

    @GetMapping("/download")
    public Mono<ResponseEntity<ByteArrayResource>> downloadFile(@RequestParam String fileId,
                                                                @RequestParam String projectId) {
        final var downloadFileRequest = DownloadFileRequest.builder()
                .fileId(fileId)
                .projectId(projectId)
                .build();
        return downloadFileUseCase.execute(downloadFileRequest)
                .map(data -> {
                    ByteArrayResource resource = new ByteArrayResource(data);

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileId + "\"")
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .contentLength(data.length)
                            .body(resource);
                });
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<FileResponse> listFilesInFolder(@RequestParam String folderId,
                                                @RequestParam String projectId) {
        final var getFilesFromAFolderRequestParams = GetFilesFromAFolderRequestParams.builder()
                .folderId(folderId)
                .projectId(projectId)
                .build();
        return getFilesFromAFolderUseCase.execute(getFilesFromAFolderRequestParams);
    }

}
