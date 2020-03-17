package edu.asu.sbs.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.jknack.handlebars.Template;
import edu.asu.sbs.config.Constants;
import edu.asu.sbs.errors.AccountResourceException;
import edu.asu.sbs.loader.HandlebarsTemplateLoader;
import edu.asu.sbs.models.User;
import edu.asu.sbs.services.AdminService;
import edu.asu.sbs.services.UserService;
import edu.asu.sbs.services.dto.UserDTO;
import edu.asu.sbs.vm.LoginVM;
import edu.asu.sbs.vm.ManageUserVM;
import javafx.geometry.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    ObjectMapper mapper = new ObjectMapper();

    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @Autowired
    private HandlebarsTemplateLoader handlebarsTemplateLoader;


    @RequestMapping(value="/home", method=RequestMethod.GET, produces="text/html")
    public String getHomeTemplate(Model model) throws  IOException {
        Template template = handlebarsTemplateLoader.getTemplate("adminHome");
        return template.apply("");
    }

    @RequestMapping(value="/profile", method=RequestMethod.GET, produces="text/html")
    public String getAdminProfileTemplate() throws  IOException {
        Template template = handlebarsTemplateLoader.getTemplate("profileAdmin");
        return template.apply("");
    }

    @RequestMapping(value="/handleemployee", method=RequestMethod.GET, produces="text/html")
    public String getHandleEmployeeTemplate() throws  IOException {
        ArrayList<User> allEmployees = (ArrayList<User>) adminService.getAllEmployees();
        HashMap<String, ArrayList<User>> resultMap= new HashMap<>();
        resultMap.put("result", allEmployees);
        JsonNode result = mapper.valueToTree(resultMap);
        Template template = handlebarsTemplateLoader.getTemplate("adminEmployeeAccess");
        return template.apply(handlebarsTemplateLoader.getContext(result));
    }

    @RequestMapping(value="/update/{id}", method= {RequestMethod.GET}, produces="text/html")
    public String getUpdateTemplate(@PathVariable long id) throws  IOException {

        Optional<User> resultMap = adminService.getEmployeeById(id);
        JsonNode result = mapper.valueToTree(resultMap.get());
        Template template = handlebarsTemplateLoader.getTemplate("adminUpdateEmployee");
        System.out.println(resultMap.get().getDateOfBirth());
        return template.apply(handlebarsTemplateLoader.getContext(result));
    }

    @RequestMapping(value="/updateUser", method=RequestMethod.POST, produces="text/html")
    public void getTestingTemplate(UserDTO userDTO, HttpServletResponse response) throws  IOException {
        System.out.println(userDTO);
        response.sendRedirect("home");
    }

    @RequestMapping(value="/logs", method=RequestMethod.GET, produces="text/html")
    public String getLogsTemplate() throws  IOException {
        Template template = handlebarsTemplateLoader.getTemplate("adminLogs");
        return template.apply("");
    }

    @PostMapping(path = "/authenticate", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<AdminService.JWTToken> authenticate(LoginVM loginVM) throws JSONException {
        return adminService.authenticate(loginVM);
    }
}
