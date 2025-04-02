package data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailDto {
    private String to;
    private String subject;
    private String message;
}
