package br.com.thiagomagdalena.googledriveapiservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.File;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/google-drive")
public class GoogleDriveController {

    private final GoogleDriveService googleDriveService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> uploadFile(@RequestPart("file") Mono<FilePart> filePartMono) {
        return filePartMono.flatMap(filePart -> {
            final var convFile = new File(filePart.filename());
            return filePart.transferTo(convFile)
                    .then(googleDriveService.uploadFile(convFile, filePart.headers().getContentType().toString()))
                    .map(ResponseEntity::ok)
                    .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body("Erro ao fazer upload do arquivo")));
        });
    }

}
