package data.service;

import data.dto.TeamDto;
import data.mapper.TeamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {
    @Autowired
    TeamMapper teamMapper;

    public void createTeam(int departmentId, String name) {
        teamMapper.createTeam(departmentId, name);
    }

    public List<TeamDto> readTeamsByDepId(int departmentId) {
        return teamMapper.readTeamsByDepId(departmentId);
    }

    public void updateTeam(TeamDto teamDto) {
        teamMapper.updateTeam(teamDto);
    }

    public void deleteTeam(int id) {
        teamMapper.deleteTeam(id);
    }
}
