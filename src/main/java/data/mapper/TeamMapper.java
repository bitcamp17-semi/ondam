package data.mapper;

import data.dto.TeamDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TeamMapper {
	public void createTeam(int departmentId, String name);

    public List<TeamDto> readTeamsByDepId(int departmentId);
    
    public void updateTeam(TeamDto teamDto);

    public void deleteTeam(int id);
}
