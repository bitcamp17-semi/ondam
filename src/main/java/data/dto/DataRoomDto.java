package data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Data
@Alias("DataRoomDto")
@AllArgsConstructor
@NoArgsConstructor
public class DataRoomDto {
    private int id;
    private String name;
}
