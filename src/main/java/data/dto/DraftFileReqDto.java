package data.dto;

import lombok.Data;

import java.util.List;

@Data
public class DraftFileReqDto {
    private DraftsDto draft;
    private List<ApprovalsDto> approvals;
}
