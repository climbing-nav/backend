package com.example.climbingnav.global.client;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.climbingnav.community.dto.file.UploadResult;
import com.example.climbingnav.global.base.types.ResponseCode;
import com.example.climbingnav.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class S3Uploader {
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.prefix}")
    private String prefix;

    public UploadResult upload(MultipartFile file, Long userId) {
        validateFile(file);

        String extension = parseExtension(file.getOriginalFilename());
        String key = String.format("%s/%d/%s.%s", prefix, userId, UUID.randomUUID(), extension);

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(file.getContentType());
        meta.setContentLength(file.getSize());

        try (InputStream in = file.getInputStream()){
            amazonS3.putObject(new PutObjectRequest(bucket, key, in, meta));

        } catch (IOException e) {
            throw new CustomException(ResponseCode.INTERNAL_ERROR, "S3 업로드 실패");
        }

        String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
        return new UploadResult(key, url, file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) throw new CustomException(
                ResponseCode.BAD_REQUEST, "파일은 10MB 이하만 가능합니다.");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ResponseCode.BAD_REQUEST, "이미지 파일만 업로드 가능합니다.");
        }
    }

    private String parseExtension(String name) {
        if (name == null) return "empty";
        int i = name.lastIndexOf('.');
        if (i < 0 || i == name.length() - 1) return "empty";
        return name.substring(i + 1).toLowerCase();
    }
}
