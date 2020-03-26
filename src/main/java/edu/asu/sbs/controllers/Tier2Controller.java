package edu.asu.sbs.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Template;
import edu.asu.sbs.config.RequestType;
import edu.asu.sbs.config.UserType;
import edu.asu.sbs.errors.Exceptions;
import edu.asu.sbs.errors.UnauthorizedAccessExcpetion;
import edu.asu.sbs.loader.HandlebarsTemplateLoader;
import edu.asu.sbs.models.Request;
import edu.asu.sbs.models.User;
import edu.asu.sbs.services.RequestService;
import edu.asu.sbs.services.UserService;
import edu.asu.sbs.services.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/tier2")
public class Tier2Controller {

    private UserService userService = null;
    private final RequestService requestService;
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HandlebarsTemplateLoader handlebarsTemplateLoader;

    public Tier2Controller(UserService userService, RequestService requestService) throws UnauthorizedAccessExcpetion, IOException {
        this.userService = userService;
        this.requestService = requestService;
    }

    @GetMapping("/user/add")
    public String getLoginTemplate() throws IOException {
        Template template = handlebarsTemplateLoader.getTemplate("tier2AddNewUser");
        return template.apply("");
    }

    @PostMapping("/user/add")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void signupSubmit(UserDTO newUserRequest, String password, String userType, HttpServletResponse response) throws IOException {
        userService.registerUser(newUserRequest, password, userType);
        log.info("POST request: tier2 new user request");
        response.sendRedirect("/allUsers");
    }

    @GetMapping("/details")
    @ResponseBody
    public String currentUserDetails() throws UnauthorizedAccessExcpetion, IOException {

            User user = userService.getCurrentUser();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            mapper.setDateFormat(df);
            if (user == null) {
                log.info("GET request: Unauthorized request for tier2 employee user detail");
                throw new UnauthorizedAccessExcpetion("401", "Unauthorized Access !");
            }
            JsonNode result = mapper.valueToTree(user);
            Template template = handlebarsTemplateLoader.getTemplate("profileTier2");
            log.info("GET request: Tier2 Employee user detail");
            return template.apply(handlebarsTemplateLoader.getContext(result));
    }

    @GetMapping("/allUsers")
    public String getUsers() throws IOException {
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
        if (!(current.get().getUserType().equals(UserType.USER_ROLE))) {
            log.warn("GET request: tier2 employee unauthorised request access");
            throw new Exceptions("401", "Unauthorized request !!");
        }

        userService.deleteUser(id);
        log.info("POST request: Delete User request");
        response.sendRedirect("/api/v1/tier2/allUsers");
    }

    @GetMapping("/viewUser/{id}")
    public String getUserDetail(@PathVariable Long id) throws Exceptions, IOException {
        Optional<User> user = userService.getUserByIdAndActive(id);

        if (user == null) {
            throw new Exceptions("404", " ");
        }
        if (!(user.get().getUserType().equals(UserType.USER_ROLE) )) {
            log.warn("GET request: Unauthorized request for external user");
            throw new Exceptions("409", " ");
        }

            JsonNode result = mapper.valueToTree(user.get());
            Template template = handlebarsTemplateLoader.getTemplate("tier2ViewUser");
            return template.apply(handlebarsTemplateLoader.getContext(result));
        }

    @GetMapping("/transactions")
    public String getAllUserRequest() throws JSONException, IOException {
        ArrayList<Request> allRequests = (ArrayList<Request>) requestService.getAllRequests();
        HashMap<String, ArrayList<Request>> resultMap= new HashMap<>();
        resultMap.put("result", allRequests);
        JsonNode result = mapper.valueToTree(resultMap);
        Template template = handlebarsTemplateLoader.getTemplate("tier2Transactions");
        return template.apply(handlebarsTemplateLoader.getContext(result));
    }

    @GetMapping("/editUser/{id}")
    public String modifyUserDetail(@PathVariable Long id,
                                   String phoneNumber,
                                   String firstName,
                                   String lastName,
                                   String email,
                                   String userName,
                                   String userType,
                                   String ssn
                                   ) throws Exceptions, IOException {

        Optional<User> user = userService.editUser(id, phoneNumber, firstName,
                                            lastName, email, userName, userType, ssn);

        if (user == null) {
            throw new Exceptions("404", " ");
        }
        JsonNode result = mapper.valueToTree(user.get());
        Template template = handlebarsTemplateLoader.getTemplate("tier2ViewUser");
        return template.apply(handlebarsTemplateLoader.getContext(result));
    }

    @PutMapping("/tier2Requests/approve/{id}")
    public void approveEdit(@PathVariable Long id) {

        Optional<Request> request = requestService.getRequest(id);
        request.ifPresent(req -> {
            switch (req.getRequestType()) {
                case RequestType.ABOVE_LIMIT_TRANS:
                    // Add logic here for above $1000 requests
                    break;
            }
        });
    }
}
