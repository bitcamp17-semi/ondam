package data.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import config.NaverConfig;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final AmazonS3 naverS3Client;
    private final NaverConfig naverConfig;

    public String uploadFile(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String chat = UUID.randomUUID() + "_" + originalName;
        String bucket = naverConfig.getBucketName();

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // S3 업로드
        naverS3Client.putObject(bucket, chat, file.getInputStream(), metadata);

        // 업로드된 파일의 URL 반환
        return naverS3Client.getUrl(bucket, chat).toString();
    }
}
