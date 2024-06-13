package org.example.todoapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class S3Service {

    //s3버킷을 이용해 쓰기편하도록 지원해주는 라이브러리를 많이 지원해준다.

    private final S3Client s3Client; //AWS에서 지원해주는 인터페이스.
    private final String bucketName = "hohyeon-new-bucket";

    public void uploadFile(MultipartFile file,String key)throws IOException{
//        String key = file.getOriginalFilename();//해당 키를 그냥 파일이름으로 해줬다. 그렇다면 중복된 파일이 올라올때의 문제점을 생각해볼수있다.
//                                                //파일명이 키가 될경우 공백 ,한글같은경우도 문제가 생길 수 있다.
//                                                //
//key값 바꿔주기

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );
    }

    public InputStream downloadFile(String key){
        return s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }
}
