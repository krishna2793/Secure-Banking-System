package edu.asu.sbs.controllers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.jknack.handlebars.Template;
import edu.asu.sbs.config.Constants;
import edu.asu.sbs.errors.AccountResourceException;
import edu.asu.sbs.errors.InvalidPasswordException;
import edu.asu.sbs.loader.HandlebarsTemplateLoader;
import edu.asu.sbs.models.User;
import edu.asu.sbs.services.UserService;
import edu.asu.sbs.vm.LoginVM;
import edu.asu.sbs.vm.ManageUserVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private HandlebarsTemplateLoader handlebarsTemplateLoader;

    @RequestMapping(value="/signup", method=RequestMethod.GET, produces="text/html")
    public String getHomeTemplate() throws  IOException {
        Template template = handlebarsTemplateLoader.getTemplate("signup");
        return template.apply("");
    }

    @RequestMapping(value="/login", method=RequestMethod.GET, produces="text/html")
    public String getLoginTemplate() throws  IOException {
        Template template = handlebarsTemplateLoader.getTemplate("login");
        return template.apply("");
    }

    @PostMapping(path = "/authenticate", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<UserService.JWTToken> authenticate(LoginVM loginVM) throws JSONException {
        return userService.authenticate(loginVM);
    }

    @PostMapping( path = "/register", consumes = "application/x-www-form-urlencoded")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(ManageUserVM manageUserVM) {
        if (!checkPasswordLength(manageUserVM.getPassword())) {
            throw new InvalidPasswordException();
        }
        userService.registerUser(manageUserVM, manageUserVM.getPassword());
    }

    @GetMapping("/activate")
    public void activate(@RequestParam(value = "key") String key) {
        Optional<User> user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this activation key");
        }
    }

    @GetMapping("/test")
    @ResponseBody
    public JSONObject test() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ASUH", "ASDJH");
        jsonObject.put("Array", new JSONArray().put(1));
        return jsonObject;
    }

    private static boolean checkPasswordLength(String password) {
        return !StringUtils.isEmpty(password) &&
                password.length() >= Constants.PASSWORD_MIN_LENGTH &&
                password.length() <= Constants.PASSWORD_MAX_LENGTH;
    }
}
