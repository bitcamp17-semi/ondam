package config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;

@Configuration
@PropertySource("classpath:naver.properties")
@ConfigurationProperties(prefix = "ncp")
@Data
public class NaverConfig {
    private String accessKey;
    private String secretKey;
    private String regionName;
    private String endPoint;
    private String bucketName;
    private String optimizerEndPoint;

    // S3 버킷의 전체 URL을 생성하는 메서드 추가
    public String getBucketUrl() {
        return "https://" + this.endPoint + "/" + this.bucketName;
    }
}
