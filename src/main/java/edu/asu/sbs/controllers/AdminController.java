package edu.asu.sbs.controllers;

import edu.asu.sbs.errors.*;
import edu.asu.sbs.models.*;
//import edu.asu.sbs.services.AdminService;
import edu.asu.sbs.services.UpdateRequestService;
import edu.asu.sbs.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import java.util.UUID;

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

    @GetMapping("/admin/details")
    @ResponseBody
    public JSONObject currentUserDetails() throws UnauthorizedAccessExcpetion, JSONException {

        User user = userService.getCurrentUser();

        if (user == null) {
            log.info("GET request: Unauthorized request for admin user detail");
            throw new UnauthorizedAccessExcpetion("401","Unauthorized Access !");
        }

        log.info("GET request: Admin user detail");
        //model.addAttribute("user", user);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", user);
        jsonObject.put("Array", new JSONArray().put(1));
        return jsonObject;

        //return "admin/detail";
    }

    @GetMapping("/admin")
    public String currentAdmin(){
        return "redirect:/admin/details";
    }

    @GetMapping("/admin/edit")
    public JSONObject editUser() throws UnauthorizedAccessExcpetion, JSONException {
    //public String editUser(Model model) throws UnauthorizedAccessExcpetion {
        User user = userService.getCurrentUser();

        if (user == null) {
            log.info("GET request: Unauthorized request for admin user detail");
            throw new UnauthorizedAccessExcpetion("401"," ");
        }

        log.info("GET request: Admin user detail");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", user);
        jsonObject.put("Array", new JSONArray().put(1));
        return jsonObject;
        //model.addAttribute("user", user);

        //return "admin/edit";
    }

    @PostMapping("/admin/edit")
    @ResponseBody
    public void editSubmit(@ModelAttribute User user) throws UnauthorizedAccessExcpetion {
        User current = userService.getCurrentUser();

        if (user == null) {
            log.info("GET request: Unauthorized request for admin user detail");
            throw new UnauthorizedAccessExcpetion("401"," ");
        }

        user.setUserType("ROLE_ADMIN");
        user.setId(current.getId());

        // create request
        userService.editUser(user);
        log.info("POST request: Admin edit");

        //return "redirect:/admin/details?successEdit=true";
    }

    @GetMapping("/admin/user/add")
    public JSONObject signupForm(@RequestParam(required = false) Boolean success) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (success != null) {
            //model.addAttribute("success", success);
            jsonObject.put("user", "Success");
        }
        //model.addAttribute("newUserRequest", new User());
        log.info("GET request: Admin new user request");
        jsonObject.put("user", new User());
        return jsonObject;

        //return "admin/newuserrequest";
    }

    @PostMapping("/admin/user/add")
    public void signupSubmit(@ModelAttribute User newUserRequest) throws Exceptions {

        if (userService.createNewUserRequest(newUserRequest) == null) {
            throw new Exceptions("500"," Cannot create New User");
        }

        log.info("POST request: Admin new user request");

        //return "redirect:/admin/user/add?success=true";
    }

    @GetMapping("/admin/user")
    public JSONObject getUsers(Model model) throws Exceptions, JSONException {
        List<User> users = userService.getUsersByType("Tier_2");
        if (users == null) {
            throw new Exceptions("500"," No users found for given type");
        }
        model.addAttribute("users", users);
        log.info("GET request: All internal users");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("users", users);
        return jsonObject;
        //return "admin/internalusers";
    }

    @GetMapping("/admin/user/edit/{id}")
    public JSONObject editUser(@PathVariable Long id) throws Exceptions, JSONException {
        User user = userService.getUserByIdAndActive(id);
        if (user == null) {
            throw new Exceptions("404"," ");
        }
        if (!user.getUserType().equals("internal")) {
            log.warn("GET request: Admin unauthrorised request access");
            throw new Exceptions("401","Unauthorized Request !");
        }

        //model.addAttribute("user", user);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", user);

        log.info("GET request: All internal users");
        return jsonObject;

        //return "admin/internalusers_edit";
    }

    @PostMapping("/admin/user/edit/{id}")
    public void editSubmit(@ModelAttribute User user, @PathVariable Long id) throws Exceptions {
        User current = userService.getUserByIdAndActive(id);
        if (current == null) {
            throw new Exceptions("404"," ");
        }

        if (!current.getUserType().equals("internal")) {
            log.warn("GET request: Admin unauthrorised request access");
            throw new Exceptions("401","request.unauthorized");
        }
        user.setId(id);
        log.info("POST request: Internal user edit");
        user = userService.editUser(user);
        if (user == null) {
            throw new Exceptions("500"," Internal User Edit request Failed");
        }

        //return "redirect:/admin/user?successEdit=true";
    }

    @GetMapping("/admin/user/delete/{id}")
    public JSONObject deleteUser(@PathVariable Long id) throws Exceptions, JSONException {
        User user = userService.getUserByIdAndActive(id);
        if (user == null) {
            throw new Exceptions("404"," ");
        }
        if (!user.getUserType().equals("internal")) {
            log.warn("GET request: Admin unauthrorised request access");
            throw new Exceptions("401","request.unauthorized");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", user);
        log.info("GET request: Delete internal user");
        return jsonObject;


        //return "admin/internalusers_delete";
    }

    @PostMapping("/admin/user/delete/{id}")
    public void deleteSubmit(@ModelAttribute User user, @PathVariable Long id, BindingResult bindingResult) throws Exceptions {
        User current = userService.getUserByIdAndActive(id);
        if (current == null) {
            throw new Exceptions("404"," ");
        }
        if (!current.getUserType().equals("internal")) {
            log.warn("GET request: Admin unauthrorised request access");
            throw new Exceptions("401","Unauthorized request !!");
        }

        userService.deleteUser(id);
        log.info("POST request: Employee New modification request");

        //return "redirect:/admin/user?successDelete=true";
    }


    @GetMapping("/admin/user/{id}")
    public JSONObject getUserDetail(@PathVariable Long id) throws Exceptions, JSONException {
        User user = userService.getUserByIdAndActive(id);
        if (user == null) {
            throw new Exceptions("404"," ");
        }
        if (!user.getUserType().equals("internal")) {
            log.warn("GET request: Unauthorized request for external user");
            throw new Exceptions("409"," ");
        }

        //model.addAttribute("user", user);
        log.info("GET request: Internal user details by id");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", user);
        return jsonObject;

        //return "admin/userdetail";
    }

    @GetMapping("/admin/user/request")
    public JSONObject getAllUserRequest(Model model) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        List<UpdateRequest> updateRequests = UpdateRequestService.getUpdateRequests("pending", "internal");
        if (updateRequests == null) {
            jsonObject.put("modificationRequests", new ArrayList<UpdateRequest>());
            //model.addAttribute("modificationrequests", new ArrayList<UpdateRequest>());
        }
        else {
            jsonObject.put("modificationRequests", updateRequests);
            //model.addAttribute("modificationrequests", updateRequests);
        }
        log.info("GET request: All user requests");
        return jsonObject;
        //return "admin/modificationrequests";
    }

    @GetMapping("/admin/user/request/view/{id}")
    public JSONObject getUserRequest(Model model, @PathVariable() UUID id) throws Exceptions, JSONException {
        UpdateRequest updateRequest = UpdateRequestService.getUpdateRequest(id);
        JSONObject jsonObject = new JSONObject();
        if (updateRequest == null) {
            throw new Exceptions("404","Invalid Request !");
        }
        if (!updateRequest.getUserType().equals("internal")) {
            log.warn("GET request: Admin unauthrorised request access");
            throw new Exceptions("401","Unauthorised Request !");
        }
        //model.addAttribute("modificationrequest", updateRequest);
        jsonObject.put("modificationRequests", updateRequest);
        //jsonObject.put("Array", new JSONArray().put(1));
        log.info("GET request: User modification request by ID");

        return jsonObject;
        //return "admin/modificationrequest_detail";
    }

    @PostMapping("/admin/user/request/{requestId}")
    public void approveEdit(@PathVariable UUID requestId, @ModelAttribute UpdateRequest request) throws Exceptions {
        String status = request.getStatus();
        if (status == null || !(request.getStatus().equals("approved") || request.getStatus().equals("rejected"))) {
            throw new Exceptions("400","Invalid Request Action !");
        }

        // checks validity of request
        if (UpdateRequestService.getUpdateRequest(requestId) == null) {
            throw new Exceptions("404","Invalid Request !");
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
        //model.addAttribute("modificationrequest", updateRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("modificationRequests", updateRequest);
        log.info("GET request: User modification request by ID");

        return jsonObject;
        //return "admin/modificationrequest_delete";
    }

    @PostMapping("/admin/user/request/delete/{requestId}")
    public void deleteRequest(@PathVariable UUID requestId, @ModelAttribute UpdateRequest request, BindingResult bindingResult) throws Exceptions {
        request = UpdateRequestService.getUpdateRequest(requestId);

        // checks validity of request
        if (request == null) {
            throw new Exceptions("404","Invalid Request !");
        }

        if (!UpdateRequestService.verifyUpdateRequestUserType(requestId, "internal")) {
            log.warn("GET request: Admin unauthrorised request access");
            throw new Exceptions("401","Unauthorised Request !");
        }
        UpdateRequestService.deleteUpdateRequest(request);
        log.info("POST request: Admin approves modification request");

        //return "redirect:/admin/user/request?successDelete=true";
    }

    /*
    @RequestMapping("/admin/syslogs")
    public void adminControllerSystemLogs() {
        //return "admin/systemlogs";
    }
     */

    /**Returns a list of all users
     * @return*/
    @GetMapping("/admin/user/pii")
    public JSONObject adminAccessPII(Model model) throws JSONException {
        List <User> userList = UpdateRequestService.ListAllActiveUsers();
        //model.addAttribute("users", userList);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("users", userList);
        return jsonObject;
        //return "admin/accesspii";
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