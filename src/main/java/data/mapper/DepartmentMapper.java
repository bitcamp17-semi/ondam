package data.mapper;

import data.dto.DepartmentDto;
import data.dto.TeamDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DepartmentMapper {
    public void createDep(DepartmentDto dto);

    public List<DepartmentDto> readAllDeps();

    public void updateDep(DepartmentDto dep);

    public void deleteDep(int id);

    public DepartmentDto readDepById(int id);

    List<TeamDto> readTeamsByDepartmentId(@Param("deptId") int deptId);

    public void updateDepLeader(int userId, int departmentId);
}
