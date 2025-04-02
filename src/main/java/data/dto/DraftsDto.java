package data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.sql.Timestamp;

@Alias("DraftsDto")
@Data
public class DraftsDto {
    public enum DraftStatus {
        PENDING, APPROVED, REJECTED, CANCELED
    }
    private int id;
    private String title;
    private String content;
    private int authorId;
    private int templateId;
    private DraftStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private Timestamp createdAt;
}
