package data.service;

import data.dto.DepartmentDto;
import data.dto.TeamDto;
import data.mapper.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {
    @Autowired
    DepartmentMapper departmentMapper;

    public void createDep(int userId, String name) {
        departmentMapper.createDep(userId, name);
    }

    public List<DepartmentDto> readAllDeps() {
        return departmentMapper.readAllDeps();
    }

    public void updateDep(DepartmentDto departmentDto) {
        departmentMapper.updateDep(departmentDto);
    }

    public void deleteDep(int id) {
        departmentMapper.deleteDep(id);
    }

    public DepartmentDto readDepById(int id) {
        return departmentMapper.readDepById(id);
    }

    public List<TeamDto> getTeamsByDepartmentId(int deptId) {
        return departmentMapper.readTeamsByDepartmentId(deptId);
    }
}
