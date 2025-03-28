package data.mapper;

import data.dto.DepartmentDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepartmentMapper {
    public void createDep(int userId, String name);
    public List<DepartmentDto> readAllDeps();
}
