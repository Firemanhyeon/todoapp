package org.example.todoapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.todoapp.entity.FileEntity;
import org.example.todoapp.service.FileDatabaseService;
import org.example.todoapp.service.S3Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
public class S3Controller {
    private final S3Service s3Service;

//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadFile(@RequestParam("file")MultipartFile file){
//        try {
//            s3Service.uploadFile(file);
//            return ResponseEntity.ok("file upload successfully:"+file.getOriginalFilename());
//        }catch(Exception e){
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    @GetMapping("/download/{key}")
//    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable String key){
//        try{
//            InputStream inputStream = s3Service.downloadFile(key);
//            StreamingResponseBody responseBody = outputStream -> {
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while((bytesRead = inputStream.read(buffer))!=-1){
//                    outputStream.write(buffer,0,bytesRead);
//                }
//                inputStream.close();
//            };
//            return ResponseEntity.ok()
//                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                    .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + key +"\"")
//                    .body(responseBody);
//        }catch (Exception e){
//            return ResponseEntity.status(500).body(null);
//        }
//    }

    private final FileDatabaseService fileDatabaseService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            //파일저장방식 변경
            String uuid = UUID.randomUUID().toString(); //uuid자동으로발급
            String datePath = LocalDate.now().toString().replace("-", "/");//년월일 생성
            String key = datePath + "/" + uuid;//년월일+/uuid

            s3Service.uploadFile(file, key);//버킷에저장하는 서비스호출
            fileDatabaseService.saveFileMetadata(uuid, key, file.getOriginalFilename(), file.getSize(), file.getContentType());//디비에도 저장

            return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
        }
    }

    @GetMapping("/download/{uuid}")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("uuid") String uuid) {
        try {
            Optional<FileEntity> fileEntityOptional = fileDatabaseService.getFileMetadata(uuid);
            if (!fileEntityOptional.isPresent()) {
                return ResponseEntity.status(404).body(null);
            }

            FileEntity fileEntity = fileEntityOptional.get();
            InputStream inputStream = s3Service.downloadFile(fileEntity.getPath());

            StreamingResponseBody responseBody = outputStream -> {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
            };

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getOriginalFilename() + "\"")
                    .body(responseBody);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}


