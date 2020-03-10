package edu.asu.sbs.controllers;

import edu.asu.sbs.errors.*;
import edu.asu.sbs.models.User;
import edu.asu.sbs.services.AdminService;
import edu.asu.sbs.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController{

    private final UserService userService;

    public AdminController(UserService userService, AdminService adminService) {
        this.userService = userService;
    }

    @GetMapping("/admin/details")
    public String currentUserDetails(Model model) throws UnauthorizedAccessExcpetion {

        User user = userService.getCurrentUser();

        if (user == null) {
            log.info("GET request: Unauthorized request for admin user detail");
            //return "redirect:/error?code=401";
            throw new UnauthorizedAccessExcpetion("401","Unauthorized Access !");
        }

        log.info("GET request: Admin user detail");
        model.addAttribute("user", user);

        return "admin/detail";
    }

    @GetMapping("/admin")
    public String currentAdmin(Model model){
        return "redirect:/admin/details";
    }

    @GetMapping("/admin/edit")
    public String editUser(Model model) throws UnauthorizedAccessExcpetion {
        User user = userService.getCurrentUser();

        if (user == null) {
            log.info("GET request: Unauthorized request for admin user detail");
            //return "redirect:/error?code=401";
            throw new UnauthorizedAccessExcpetion("401"," ");
        }

        log.info("GET request: Admin user detail");
        model.addAttribute("user", user);

        return "admin/edit";
    }

    @PostMapping("/admin/edit")
    public String editSubmit(@ModelAttribute User user, BindingResult bindingResult) throws UnauthorizedAccessExcpetion {
        User current = userService.getCurrentUser();

        if (user == null) {
            log.info("GET request: Unauthorized request for admin user detail");
            //return "redirect:/error?code=401";
            throw new UnauthorizedAccessExcpetion("401"," ");
        }

        user.setUserType("ROLE_ADMIN");
        user.setId(current.getId());

        // create request
        userService.editUser(user);
        log.info("POST request: Admin edit");

        return "redirect:/admin/details?successEdit=true";
    }

}