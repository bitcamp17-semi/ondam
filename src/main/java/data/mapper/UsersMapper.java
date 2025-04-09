package data.mapper;

import data.dto.UsersDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UsersMapper {
    public void createUser(UsersDto usersDto);

    public UsersDto readUserById(int id);

    public UsersDto readUserByLoginId(String loginId);

    public String readLastLoginId();

    public void updateUser(UsersDto usersDto);

    public void deactivateUserById(int id);

    public List<UsersDto> readAllActiveUsers();

    public List<UsersDto> readAllDeactivateUsers();

    public List<UsersDto> readUsersByDep(int departmentId, int offset, int size);

    public int readCountUsersByDep(int departmentId);

    public List<UsersDto> readUsersByTeam(String team);

    public List<UsersDto> readUsersByTeamId(int teamId);

    public void deleteUser(int id);

    public List<UsersDto> readUsersByName(String name);
}
