package data.service;

import data.dto.UsersDto;
import data.mapper.UsersMapper;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class UsersService {
    @Autowired
    UsersMapper usersMapper;

    public boolean createUser(UsersDto usersDto) throws ParseException {
//        String birth = String.valueOf(usersDto.getBirth());
//        usersDto.setBirth(convertStringToDate(birth));
        usersDto.setPassword(hashingPassword("0000"));
        usersDto.setLoginId(generateLoginId());
        usersMapper.createUser(usersDto);
        return true;
    }

    public UsersDto readUserById(int id) {
        return usersMapper.readUserById(id);
    }

    public UsersDto readUserByLoginId(String loginId) {
        return usersMapper.readUserByLoginId(loginId);
    }

    public void updateUser(UsersDto usersDto) {
        usersMapper.updateUser(usersDto);
    }

    public void deactivateUser(int id) {
        usersMapper.deactivateUserById(id);
    }

    public List<UsersDto> readAllActiveUsers() {
        return usersMapper.readAllActiveUsers();
    }

    public List<UsersDto> readAllDeactivateUsers() {
        return usersMapper.readAllDeactivateUsers();
    }

    public List<UsersDto> readUsersByDep(String department) {
        return usersMapper.readUsersByDep(department);
    }

    public List<UsersDto> readUsersByTeam(String team) {
        return usersMapper.readUsersByTeam(team);
    }

    public Date convertStringToDate(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date utilDate = sdf.parse(date);
        return new java.sql.Date(utilDate.getTime());
    }

    public boolean isAdmin(int userId) {
        UsersDto usersDto = usersMapper.readUserById(userId);
        return usersDto.isAdmin();
    }

    public String hashingPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public String generateLoginId() {
        // 가장 큰 loginId 값 조회
        String lastLoginId = usersMapper.readLastLoginId();
        int lastNumber = 1;

        if (lastLoginId != null) {
            lastNumber = Integer.parseInt(lastLoginId.substring(2)) + 1; // 예시: BC000010 -> 10 + 1 = 11
        }
        return String.format("OD%06d", lastNumber);
    }

}
