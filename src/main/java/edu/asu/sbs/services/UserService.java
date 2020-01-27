package edu.asu.sbs.services;

import edu.asu.sbs.models.User;
import edu.asu.sbs.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void createUpdateUser(User user) {
        User u = new User();
        u.setUserName("RR");
        u.setEmailAddress("93@asu.edu");
        u.setPassword("Test");
        userRepository.save(u);
    }


}
