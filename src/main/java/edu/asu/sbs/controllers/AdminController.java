package edu.asu.sbs.controllers;

import edu.asu.sbs.config.Constants;
import edu.asu.sbs.errors.AccountResourceException;
import edu.asu.sbs.errors.InvalidPasswordException;
import edu.asu.sbs.models.User;
import edu.asu.sbs.services.AdminService;
import edu.asu.sbs.services.UserService;
import edu.asu.sbs.vm.LoginVM;
import edu.asu.sbs.vm.ManageUserVM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController{

    private final UserService userService
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin/details")
    public String currentUserDetails(Model model) throws Exceptions {

        Optional<User> user = userService.getCurrentUser();

        if (user == null) {
            log.info("GET request: Unauthorized request for admin user detail");
            //return "redirect:/error?code=401";
            throw new Exceptions("401","Unauthorized Access !");
        }

        log.info("GET request: Admin user detail");
        model.addAttribute("user", user);

        return "admin/detail";
    }

    @GetMapping("/admin")
    public String currentAdmin(Model model) throws Exceptions {
        return "redirect:/admin/details";
    }

    @GetMapping("/admin/edit")
    public String editUser(Model model) throws Exceptions {
        Optional<User> user = userService.getCurrentUser();

        if (user == null) {
            log.info("GET request: Unauthorized request for admin user detail");
            //return "redirect:/error?code=401";
            throw new Exceptions("401"," ");
        }

        log.info("GET request: Admin user detail");
        model.addAttribute("user", user);

        return "admin/edit";
    }

    @PostMapping("/admin/edit")
    public String editSubmit(@ModelAttribute User user, BindingResult bindingResult) throws Exceptions {
        Optional<User> current = userService.getCurrentUser();

        if (user == null) {
            log.info("GET request: Unauthorized request for admin user detail");
            //return "redirect:/error?code=401";
            throw new Exceptions("401"," ");
        }
        editUserFormValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            return "manager/edit";
        }
        user.setUserType("ROLE_ADMIN");
        user.setId(current.getUserId());

        // create request
        userService.editUser(user);
        log.info("POST request: Admin edit");

        return "redirect:/admin/details?successEdit=true";
    }
}