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
    	DepartmentDto dto = new DepartmentDto();
        dto.setUserId(userId);
        dto.setName(name);

        departmentMapper.createDep(dto); // insert 후 id가 dto에 자동으로 세팅됨

        return dto.getId(); // 바로 int로 안전하게 사용 가능
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
