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

    public List<UsersDto> readUsersByDep(String department, int offset, int size);

    public int readCountUsersByDep(String department);

    public List<UsersDto> readUsersByTeam(String team);

    public void deleteUser(int id);
}
