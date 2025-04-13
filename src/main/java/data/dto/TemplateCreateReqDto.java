package data.dto;

import lombok.Data;

import java.util.List;

@Data
public class TemplateCreateReqDto {
    private DraftTemplatesDto template;
    private List<ApprovalsDto> approvals;
}