package data.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import config.NaverConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final String CHAT_FOLDER = "chat/";
    private static final String FILE_NAME_DELIMITER = "_";

    private final AmazonS3 naverS3Client;
    private final NaverConfig naverConfig;

    /**
     * 파일을 S3에 업로드하고 접근 가능한 URL 반환
     * @param file 업로드할 파일
     * @return S3에 저장된 파일의 공개 URL
     * @throws RuntimeException 파일 업로드 실패 시
     */
    public String uploadFile(MultipartFile file) {
        validateFile(file);
        
        String objectKey = generateObjectKey(file.getOriginalFilename());
        String fileUrl = generateFileUrl(objectKey);

        try (InputStream inputStream = file.getInputStream()) {
            uploadToS3(inputStream, objectKey, file.getContentType(), file.getSize());
            log.info("File uploaded successfully. URL: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }
    }

    /**
     * S3에 저장될 객체 키 생성
     */
    private String generateObjectKey(String originalFilename) {
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID() + FILE_NAME_DELIMITER + sanitizeFilename(originalFilename);
        return CHAT_FOLDER + uniqueFileName;
    }

    /**
     * 파일명 정제 (특수문자 제거)
     */
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * S3 파일 URL 생성
     */
    private String generateFileUrl(String objectKey) {
        // 이전 코드와 동일한 형식으로 URL 생성
        return String.format("%s/%s/%s",
                naverConfig.getEndPoint(),
                naverConfig.getBucketName(),
                objectKey);
    }

    /**
     * S3에 파일 업로드
     */
    private void uploadToS3(InputStream inputStream, String objectKey, 
                          String contentType, long fileSize) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(fileSize);

        PutObjectRequest request = new PutObjectRequest(
            naverConfig.getBucketName(),
            objectKey,
            inputStream,
            metadata
        ).withCannedAcl(CannedAccessControlList.PublicRead);

        naverS3Client.putObject(request);
    }
}