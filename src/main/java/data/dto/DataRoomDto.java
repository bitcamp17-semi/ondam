package data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.List;

@Data
@Alias("DataRoomDto")
@AllArgsConstructor
@NoArgsConstructor
public class DataRoomDto {
    private int id;
    private String name;
    private int parentId;
    private boolean hasChild;
    private String type;
    private List<DataRoomDto> subFolders;
}
