package data.service;

import data.dto.DepartmentDto;
import data.dto.TeamDto;
import data.mapper.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentService {
    @Autowired
    DepartmentMapper departmentMapper;

    public int createDep(int userId, String name) {
    	//부서 생성 시 일정 그룹도 생성도되록 하기위해 수정함
    	Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("name", name);
        departmentMapper.createDep(map); // insert 시 id 자동 채워짐
        return (Integer) map.get("id"); // 생성된 부서 ID 반환
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
