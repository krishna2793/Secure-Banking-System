package edu.asu.sbs.services;

import edu.asu.sbs.globals.UserType;
import edu.asu.sbs.models.User;
import edu.asu.sbs.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.Calendar;

@Service
public class UserService {

    final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void createUpdateUser(User user) {

        UserType userType = new UserType();

        User u = new User();
        u.setUserName("RR");
        u.setFirstName("R");
        u.setLastName("R");
        u.setPhoneNumber("asdasd");
        u.setSsn("as");
        u.setDateOfBirth(new Date(Calendar.getInstance().getTime().getTime()));
        u.setEmailAddress("93@asu.edu");
        u.setPassword("Test");
        u.setUserType("t2");
        userRepository.save(u);
    }


}
