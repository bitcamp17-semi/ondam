package data.service;

import com.amazonaws.services.s3.AmazonS3;
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
    private final NaverConfig naverConfig; // 기존 NaverS3Config → NaverConfig 로 변경

    public String uploadFile(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "_" + originalName;
        String bucket = naverConfig.getBucketName();

        // 파일 업로드
        naverS3Client.putObject(bucket, uniqueFileName, file.getInputStream(), null);

        // URL 반환
        return naverS3Client.getUrl(bucket, uniqueFileName).toString();
    }
}
