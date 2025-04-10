package data.service;

import data.dto.UsersDto;
import data.mapper.UsersMapper;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class UsersService {
    @Autowired
    UsersMapper usersMapper;
    @Autowired
    EmailService emailService;

    public boolean createUser(UsersDto usersDto) throws ParseException {
        String rawPassword = generatePassword();
        String hashedPassword = hashingPassword(rawPassword);
//        String birth = String.valueOf(usersDto.getBirth());
//        usersDto.setBirth(convertStringToDate(birth));
        usersDto.setPassword(hashedPassword);
        usersDto.setLoginId(generateLoginId());
        usersMapper.createUser(usersDto);
        emailService.signUpMail(usersDto.getName(), usersDto.getLoginId(), usersDto.getEmail(), rawPassword); // 가입 안내 메일 발송
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

    public List<UsersDto> readUsersByDep(int departmentId, int offset, int size) {
        return usersMapper.readUsersByDep(departmentId, offset, size);
    }

    public List<UsersDto> readAllUsersByDep(int departmentId) {
        return usersMapper.readAllUsersByDep(departmentId);
    }

    public int readCountUsersByDep(int departmentId) {
        return usersMapper.readCountUsersByDep(departmentId);
    }

    public List<UsersDto> readUsersByTeam(String team) {
        return usersMapper.readUsersByTeam(team);
    }
    public List<UsersDto> readUsersByTeamId(int teamId) {
        return usersMapper.readUsersByTeamId(teamId);
    }

    public void deleteUser(int id) {
        usersMapper.deleteUser(id);
    }

    public void deleteUsers(List<Integer> list) {
        for (Integer id : list) {
            usersMapper.deleteUser(id);
        }
    }

    public List<UsersDto> readUsersByName(String name) {
        return usersMapper.readUsersByName(name);
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

    public static String generatePassword() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        int PASSWORD_LENGTH = 8;
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        return sb.toString();
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
