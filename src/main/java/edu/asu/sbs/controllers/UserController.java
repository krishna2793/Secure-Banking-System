package edu.asu.sbs.controllers;

import edu.asu.sbs.models.User;
import edu.asu.sbs.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/create")
    public String createUpdateUser() {
        userService.createUpdateUser(new User());
        return "Success";
    }
}
