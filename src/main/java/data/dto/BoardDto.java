package data.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Alias("BoardDto")
@Data
public class BoardDto {
	private int id;
	private String category;
	private String title;
	private String content;
	private int authorId;
	private boolean hidden;
	private boolean important;
	private int replyCount;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
	private Timestamp createdAt;
	private Timestamp updatedAt;
	private Timestamp deletedAt;
}