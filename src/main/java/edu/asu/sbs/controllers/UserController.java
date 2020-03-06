package edu.asu.sbs.controllers;

import edu.asu.sbs.config.Constants;
import edu.asu.sbs.errors.InvalidPasswordException;
import edu.asu.sbs.services.UserService;
import edu.asu.sbs.vm.LoginVM;
import edu.asu.sbs.vm.ManageUserVM;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("authenticate")
    public ResponseEntity<JSONObject> authenticate(@Valid @RequestBody LoginVM loginVM) throws JSONException {
        return userService.authenticate(loginVM);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(@Valid @RequestBody ManageUserVM manageUserVM) {
        if (!checkPasswordLength(manageUserVM.getPassword())) {
            throw new InvalidPasswordException();
        }
        userService.registerUser(manageUserVM, manageUserVM.getPassword());
    }

    private static boolean checkPasswordLength(String password) {
        return !StringUtils.isEmpty(password) &&
                password.length() >= Constants.PASSWORD_MIN_LENGTH &&
                password.length() <= Constants.PASSWORD_MAX_LENGTH;
    }
}
