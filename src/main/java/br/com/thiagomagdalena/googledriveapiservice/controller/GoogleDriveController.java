package br.com.thiagomagdalena.googledriveapiservice.controller;

import br.com.thiagomagdalena.googledriveapiservice.dto.CreateFolderRequest;
import br.com.thiagomagdalena.googledriveapiservice.dto.DeleteResourceRequest;
import br.com.thiagomagdalena.googledriveapiservice.dto.DownloadFileRequest;
import br.com.thiagomagdalena.googledriveapiservice.dto.FileResponse;
import br.com.thiagomagdalena.googledriveapiservice.dto.FolderResponse;
import br.com.thiagomagdalena.googledriveapiservice.dto.GetFilesFromAFolderRequestParams;
import br.com.thiagomagdalena.googledriveapiservice.dto.GoogleDriveApiResponse;
import br.com.thiagomagdalena.googledriveapiservice.dto.GoogleDriveDetailsResponse;
import br.com.thiagomagdalena.googledriveapiservice.dto.ShareResourceRequest;
import br.com.thiagomagdalena.googledriveapiservice.dto.UploadFileRequest;
import br.com.thiagomagdalena.googledriveapiservice.usecase.CreateFolderUseCase;
import br.com.thiagomagdalena.googledriveapiservice.usecase.DeleteResourceUseCase;
import br.com.thiagomagdalena.googledriveapiservice.usecase.DownloadFileUseCase;
import br.com.thiagomagdalena.googledriveapiservice.usecase.GetAllFoldersUseCase;
import br.com.thiagomagdalena.googledriveapiservice.usecase.GetFilesFromAFolderUseCase;
import br.com.thiagomagdalena.googledriveapiservice.usecase.GetGoogleDriveDetailsUseCase;
import br.com.thiagomagdalena.googledriveapiservice.usecase.ShareResourceUseCase;
import br.com.thiagomagdalena.googledriveapiservice.usecase.UploadFileUseCase;
import com.google.common.net.HttpHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Tag(name = "Google Drive", description = "Google Drive API operations")
@Slf4j
@RequestMapping("/v1/google-drive")
public class GoogleDriveController {

    private final UploadFileUseCase uploadFileUseCase;
    private final DownloadFileUseCase downloadFileUseCase;
    private final GetFilesFromAFolderUseCase getFilesFromAFolderUseCase;
    private final GetAllFoldersUseCase getAllFoldersUseCase;
    private final CreateFolderUseCase createFolderUseCase;
    private final ShareResourceUseCase shareResourceUseCase;
    private final DeleteResourceUseCase deleteResourceUseCase;
    private final GetGoogleDriveDetailsUseCase getGoogleDriveDetailsUseCase;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file to Google Drive", description = "Upload a file to Google Drive")
    @ApiResponse(responseCode = "200", description = "File uploaded successfully")
    public Mono<ResponseEntity<GoogleDriveApiResponse>> uploadFile(
            @RequestPart("file") Mono<FilePart> filePartMono,
            @RequestPart("uploadFileRequest") UploadFileRequest uploadFileRequest) {

        return filePartMono.flatMap(filePart -> {
            final var tempFile = new File(filePart.filename());
            uploadFileRequest.setFile(tempFile);
            uploadFileRequest.setMimeType(Objects.requireNonNull(filePart.headers().getContentType()).toString());

            return filePart.transferTo(tempFile)
                    .then(uploadFileUseCase.execute(uploadFileRequest)
                            .map(ResponseEntity::ok)
                            .onErrorResume(e -> {
                                log.error("Erro ao processar o arquivo: {}", e.getMessage());
                                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(GoogleDriveApiResponse.builder()
                                                .statusCode(500)
                                                .message("Erro ao fazer upload do arquivo: " + e.getMessage())
                                                .build()));
                            })
                    )
                    .doFinally(signalType -> {
                        if (tempFile.exists()) {
                            tempFile.delete();
                        }
                    });
        });
    }

    @GetMapping(value = "/download", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Download file from Google Drive", description = "Download a file from Google Drive")
    @ApiResponse(responseCode = "200", description = "File downloaded successfully")
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

    @GetMapping(value = "/files/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List files in a folder", description = "List all files in a folder")
    @ApiResponse(responseCode = "200", description = "Files listed successfully")
    public Flux<FileResponse> listFilesInFolder(@RequestParam(required = false) String folderId,
                                                @RequestParam String projectId) {
        final var getFilesFromAFolderRequestParams = GetFilesFromAFolderRequestParams.builder()
                .folderId(folderId)
                .projectId(projectId)
                .build();
        return getFilesFromAFolderUseCase.execute(getFilesFromAFolderRequestParams);
    }

    @GetMapping(value = "/folders/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List folders in a folder", description = "List all folders in a folder")
    @ApiResponse(responseCode = "200", description = "Folders listed successfully")
    public Flux<FolderResponse> listFoldersInFolder(@RequestParam String projectId) {
        return getAllFoldersUseCase.execute(projectId);
    }

    @PostMapping(value = "/folders/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a folder", description = "Create a folder in Google Drive")
    @ApiResponse(responseCode = "200", description = "Folder created successfully")
    public Mono<FolderResponse> createFolder(@RequestBody CreateFolderRequest createFolderRequest) {
        return createFolderUseCase.execute(createFolderRequest);
    }

    @PostMapping(value = "/resources/share", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Share resources", description = "Share resources in Google Drive")
    @ApiResponse(responseCode = "200", description = "Resources shared successfully")
    public Mono<String> shareResources(@RequestBody ShareResourceRequest shareResourceRequest) {
        return shareResourceUseCase.execute(shareResourceRequest);
    }

    @DeleteMapping(value = "/resources/{projectId}/delete/{resourceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete resource", description = "Delete a resource in Google Drive")
    @ApiResponse(responseCode = "200", description = "Resource deleted successfully")
    public Mono<Void> deleteResource(@PathVariable String resourceId,
                                     @PathVariable String projectId) {
        final var deleteResourceRequest = DeleteResourceRequest.builder()
                .projectId(projectId)
                .resourceId(resourceId)
                .build();
        return deleteResourceUseCase.execute(deleteResourceRequest);
    }

    @GetMapping(value = "/drive-details/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Google Drive details", description = "Get Google Drive details")
    @ApiResponse(responseCode = "200", description = "Google Drive details retrieved successfully")
    public Mono<GoogleDriveDetailsResponse> getDriveDetails(@PathVariable String projectId) {
        return getGoogleDriveDetailsUseCase.execute(projectId);
    }
}
