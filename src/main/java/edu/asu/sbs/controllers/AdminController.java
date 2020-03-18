package edu.asu.sbs.controllers;

import com.google.gson.Gson;
import edu.asu.sbs.errors.Exceptions;
import edu.asu.sbs.errors.UnauthorizedAccessExcpetion;
import edu.asu.sbs.models.UpdateRequest;
import edu.asu.sbs.models.User;
import edu.asu.sbs.services.UpdateRequestService;
import edu.asu.sbs.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//import edu.asu.sbs.services.AdminService;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController{

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    //public AdminController(UserService userService, AdminService adminService) {
    //    this.userService = userService;
    //}

    @GetMapping("/employee/details")
    @ResponseBody
    public JSONObject currentUserDetails() throws UnauthorizedAccessExcpetion, JSONException {

        User user = userService.getCurrentUser();

        if (user == null) {
            log.info("GET request: Unauthorized request for admin user detail");
            throw new UnauthorizedAccessExcpetion("401", "Unauthorized Access !");
        }

        log.info("GET request: Admin user detail");
        return new JSONObject(new Gson().toJson(user));
    }

    @GetMapping("/employee/edit")
    public JSONObject editEmployee() throws UnauthorizedAccessExcpetion, JSONException {

        User user = userService.getCurrentUser();

        if (user == null) {
            log.info("GET request: Unauthorized request for admin user detail");
            throw new UnauthorizedAccessExcpetion("401", " ");
        }

        log.info("GET request: Admin user detail");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", user);
        return jsonObject;
    }

    @PostMapping("/employee/edit")
    @ResponseBody
    public void editSubmit(User user) throws UnauthorizedAccessExcpetion {
        User current = userService.getCurrentUser();

        if (user == null) {
            log.info("GET request: Unauthorized request for admin user detail");
            throw new UnauthorizedAccessExcpetion("401", " ");
        }

        user.setUserType("ROLE_ADMIN");
        user.setId(current.getId());

        // create request
        userService.editUser(user);
        log.info("POST request: Admin edit");
    }

    @GetMapping("/employee/add")
    public JSONObject signupForm(@RequestParam(required = false) Boolean success) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (success != null) {
            jsonObject.put("user", "Success");
        }
        log.info("GET request: Admin new user request");
        jsonObject.put("user", new User());
        return jsonObject;
    }

    @PostMapping("/employee/add")
    public void signupSubmit(User newUserRequest) throws Exceptions {

        if (userService.createNewUserRequest(newUserRequest) == null) {
            throw new Exceptions("500", " Cannot create New User");
        }

        log.info("POST request: Admin new user request");
    }

    @GetMapping("/allEmployees")
    public JSONObject getUsers() throws Exceptions, JSONException {
        List<User> users = userService.getUsersByType("Tier_2");
        if (users == null) {
            throw new Exceptions("500", " No users found for given type");
        }
        log.info("GET request: All internal users");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("users", users);
        return jsonObject;
    }

    @GetMapping("/edit/{id}")
    public JSONObject editUser(@PathVariable Long id) throws Exceptions, JSONException {
        Optional<User> user = userService.getUserByIdAndActive(id);
        if (user == null) {
            throw new Exceptions("404", " ");
        }
        if (!user.get().getUserType().equals("EMPLOYEE_ROLE1")) {
            log.warn("GET request: Admin unauthrorised request access");
            throw new Exceptions("401", "Unauthorized Request !");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", user);

        log.info("GET request: All internal users");
        return jsonObject;
    }

    @PostMapping("/delete/{id}")
    public void deleteUser(@PathVariable Long id) throws Exceptions {
        Optional<User> current = userService.getUserByIdAndActive(id);
        if (current == null) {
            throw new Exceptions("404", " ");
        }
        if (!current.get().getUserType().equals("EMPLOYEE_ROLE1")) {
            log.warn("GET request: Admin unauthrorised request access");
            throw new Exceptions("401", "Unauthorized request !!");
        }

        userService.deleteUser(id);
        log.info("POST request: Employee New modification request");
    }


    @GetMapping("/viewEmployee/{id}")
    public JSONObject getUserDetail(@PathVariable Long id) throws Exceptions, JSONException {
        Optional<User> user = userService.getUserByIdAndActive(id);
        if (user == null) {
            throw new Exceptions("404", " ");
        }
        if (!user.get().getUserType().equals("EMPLOYEE_ROLE1")) {
            log.warn("GET request: Unauthorized request for external user");
            throw new Exceptions("409", " ");
        }

        log.info("GET request: Internal user details by id");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", user);
        return jsonObject;
    }

    @GetMapping("/requests")
    public JSONObject getAllUserRequest() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        List<UpdateRequest> updateRequests = UpdateRequestService.getUpdateRequests("pending", "internal");
        if (updateRequests == null) {
            jsonObject.put("modificationRequests", new ArrayList<UpdateRequest>());
        } else {
            jsonObject.put("modificationRequests", updateRequests);
        }
        log.info("GET request: All user requests");
        return jsonObject;
    }

    @GetMapping("/employee/requests/view/{id}")
    public JSONObject getUserRequest(@PathVariable() UUID id) throws Exceptions, JSONException {
        UpdateRequest updateRequest = UpdateRequestService.getUpdateRequest(id);
        JSONObject jsonObject = new JSONObject();
        if (updateRequest == null) {
            throw new Exceptions("404", "Invalid Request !");
        }
        if (!updateRequest.getUserType().equals("internal")) {
            log.warn("GET request: Admin unauthrorised request access");
            throw new Exceptions("401", "Unauthorised Request !");
        }
        jsonObject.put("modificationRequests", updateRequest);
        log.info("GET request: User modification request by ID");

        return jsonObject;
    }

    @PostMapping("/requests/{requestId}")
    public void approveEdit(@PathVariable UUID requestId, UpdateRequest request) throws Exceptions {
        String status = request.getStatus();
        if (status == null || !(request.getStatus().equals("approved") || request.getStatus().equals("rejected"))) {
            throw new Exceptions("400", "Invalid Request Action !");
        }

        // checks validity of request
        if (UpdateRequestService.getUpdateRequest(requestId) == null) {
            throw new Exceptions("404", "Invalid Request !");
        }
        request.setUpdateRequestId(requestId);

        // checks if admin is authorized for the request to approve
        if (!UpdateRequestService.verifyUpdateRequestUserType(requestId, "internal")) {
            log.warn("GET request: Admin unauthrorised request access");
            throw new Exceptions("401","Unauthorised Request !");
        }

        request.setUserType("internal");
        request.setStatus(status);
        if (status.equals("approved")) {
            UpdateRequestService.approveUpdateRequest(request);
        }
        // rejects request
        else {
            UpdateRequestService.rejectUpdateRequest(request);
        }
        log.info("POST request: Admin approves modification request");
        //JSONObject jsonObject = new JSONObject();
        //jsonObject.put("modificationRequests", updateRequest);
        //return "redirect:/admin/user/request?successAction=true";
    }

    @GetMapping("/admin/user/request/delete/{id}")
    public JSONObject getDeleteRequest(@PathVariable() UUID id) throws Exceptions, JSONException {
        UpdateRequest updateRequest = UpdateRequestService.getUpdateRequest(id);

        if (updateRequest == null) {
            throw new Exceptions("404","Invalid Request !");
        }
        if (!updateRequest.getUserType().equals("internal")) {
            log.warn("GET request: Admin unauthrorised request access");
            throw new Exceptions("401","Unauthorised Request !");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("modificationRequests", updateRequest);
        log.info("GET request: User modification request by ID");

        return jsonObject;
    }

    @PostMapping("/admin/user/request/delete/{requestId}")
    public void deleteRequest(@PathVariable UUID requestId) throws Exceptions {
        UpdateRequest request = UpdateRequestService.getUpdateRequest(requestId);

        // checks validity of request
        if (request == null) {
            throw new Exceptions("404", "Invalid Request !");
        }

        if (!UpdateRequestService.verifyUpdateRequestUserType(requestId, "internal")) {
            log.warn("GET request: Admin unauthrorised request access");
            throw new Exceptions("401", "Unauthorised Request !");
        }
        UpdateRequestService.deleteUpdateRequest(request);
        log.info("POST request: Admin approves modification request");
    }

    /*
    @RequestMapping("/admin/syslogs")
    public void adminControllerSystemLogs() {
        //return "admin/systemlogs";
    }
     */

    /**
     * Returns a list of all users
     *
     * @return
     */
    @GetMapping("/allActiveEmployees")
    public JSONObject adminAccessActiveEmployees() throws JSONException {
        List<User> userList = UpdateRequestService.ListAllActiveUsers();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("users", userList);
        return jsonObject;
    }

    @GetMapping("/admin/logDownload")
    public void doDownload(HttpServletRequest request,
                           HttpServletResponse response) throws IOException {

        /**
         * Size of a byte buffer to read/write file
         */
        final int BUFFER_SIZE = 4096;

        /**
         * Path of the file to be downloaded, relative to application's directory
         */
        String filePath = "logs/application.log";


        // get absolute path of the application
        ServletContext context = request.getServletContext();

        ClassLoader classLoader = getClass().getClassLoader();

        // construct the complete absolute path of the file
        File downloadFile = new File(classLoader.getResource(filePath).getFile());
        FileInputStream inputStream = new FileInputStream(downloadFile);

        // get MIME type of the file
        String mimeType = context.getMimeType("text/plain");
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        System.out.println("MIME type: " + mimeType);

        // set content attributes for the response
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());

        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"",
                downloadFile.getName());
        response.setHeader(headerKey, headerValue);

        // get output stream of the response
        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;

        // write bytes read from the input stream into the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outStream.close();

    }
}