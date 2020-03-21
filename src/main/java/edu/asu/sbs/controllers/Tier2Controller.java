package edu.asu.sbs.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.jknack.handlebars.Template;
import edu.asu.sbs.config.Constants;
import edu.asu.sbs.errors.AccountResourceException;
import edu.asu.sbs.errors.Exceptions;
import edu.asu.sbs.errors.InvalidPasswordException;
import edu.asu.sbs.errors.UnauthorizedAccessExcpetion;
import edu.asu.sbs.loader.HandlebarsTemplateLoader;
import edu.asu.sbs.models.User;
import edu.asu.sbs.services.UserService;
import edu.asu.sbs.services.dto.UserDTO;
import edu.asu.sbs.vm.LoginVM;
import edu.asu.sbs.vm.ManageUserVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

    @RestController
    @RequestMapping("/api/v1/tier2")
    public class Tier2Controller {

        @Autowired
        private HandlebarsTemplateLoader handlebarsTemplateLoader;

        @RequestMapping(value = "/signup", method = RequestMethod.GET, produces = "text/html")
        public String getHomeTemplate() throws IOException {
            Template template = handlebarsTemplateLoader.getTemplate("adminHome");
            return template.apply("");
        }

        @PostMapping("/user/add")
        @ResponseStatus(HttpStatus.ACCEPTED)
        public void signupSubmit(UserDTO newUserRequest, String password, String userType, HttpServletResponse response) throws Exceptions, IOException {
            userService.registerUser(newUserRequest, password, userType);
            log.info("POST request: tier2 new user request");
            response.sendRedirect("/allUsers");
        }

        @GetMapping("/employee/details")
        @ResponseBody
        public String currentUserDetails() throws UnauthorizedAccessExcpetion, JSONException, IOException {

            User user = userService.getCurrentUser();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            mapper.setDateFormat(df);
            if (user == null) {
                log.info("GET request: Unauthorized request for tier2 employee user detail");
                throw new UnauthorizedAccessExcpetion("401", "Unauthorized Access !");
            }

        }
        @GetMapping("/allUsers")
        public String getUsers() throws Exceptions, JSONException, IOException {
            ArrayList<User> allUsers = (ArrayList<User>) userService.getAllUsers();
            HashMap<String, ArrayList<User>> resultMap= new HashMap<>();
            resultMap.put("result", allUsers);
            JsonNode result = mapper.valueToTree(resultMap);
            Template template = handlebarsTemplateLoader.getTemplate("tier2UserAccess");
            return template.apply(handlebarsTemplateLoader.getContext(result));
        }


        @GetMapping("/delete/{id}")
        public void deleteUser(@PathVariable Long id, HttpServletResponse response) throws Exceptions, IOException {
            Optional<User> current = userService.getUserByIdAndActive(id);
            if (current == null) {
                throw new Exceptions("404", " ");
            }
            if (!(current.get().getUserType().equals("USER"))) {
                log.warn("GET request: tier2 employee unauthorised request access");
                throw new Exceptions("401", "Unauthorized request !!");
            }

            userService.deleteUser(id);
            log.info("POST request: User New modification request");
            response.sendRedirect("../allUsers");
        }

        @GetMapping("/viewUsers/{id}")
        public String getUserDetail(@PathVariable Long id) throws Exceptions, JSONException, IOException {
            Optional<User> user = userService.getUserByIdAndActive(id);

            if (user == null) {
                throw new Exceptions("404", " ");
            }
            if (!(user.get().getUserType().equals("USER") )) {
                log.warn("GET request: Unauthorized request for external user");
                throw new Exceptions("409", " ");
            }

            JsonNode result = mapper.valueToTree(user.get());
            Template template = handlebarsTemplateLoader.getTemplate("tier2ViewUser");
            return template.apply(handlebarsTemplateLoader.getContext(result));
        }


    }
