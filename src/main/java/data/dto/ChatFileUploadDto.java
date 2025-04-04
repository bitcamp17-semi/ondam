package data.dto;

import org.apache.ibatis.type.Alias;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
@Alias("ChatFileUploadDto")
public class ChatFileUploadDto {
	private MultipartFile file;
}
