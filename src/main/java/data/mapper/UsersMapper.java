package data.mapper;

import data.dto.UsersDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsersMapper {
    public void createUser(UsersDto usersDto);

    public UsersDto readUserById(int id);

    public UsersDto readUserByLoginId(String loginId);

    public String readLastLoginId();

    public void updateUser(UsersDto usersDto);

    public void deactivateUserById(int id);
}
