package edu.asu.sbs.controllers;

import edu.asu.sbs.errors.*;
import edu.asu.sbs.models.*;
import edu.asu.sbs.services.AdminService;
import edu.asu.sbs.services.ModificationRequestService;
import edu.asu.sbs.services.UserService;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/admin/user/add")
    public String signupForm(Model model, @RequestParam(required = false) Boolean success) {
        if (success != null) {
            model.addAttribute("success", success);
        }
        model.addAttribute("newUserRequest", new User());
        log.info("GET request: Admin new user request");

        return "admin/newuserrequest";
    }

    @PostMapping("/admin/user/add")
    public String signupSubmit(@ModelAttribute User newUserRequest) throws Exceptions {

        if (userService.createNewUserRequest(newUserRequest) == null) {
            //return "redirect:/error?code=500";
            throw new Exceptions("500"," Cannot create New User");
        }

        log.info("POST request: Admin new user request");

        return "redirect:/admin/user/add?success=true";
    }

    @GetMapping("/admin/user")
    public String getUsers(Model model) throws Exceptions {
        List<User> users = userService.getUsersByType("internal");
        if (users == null) {
            //return "redirect:/error?code=500";
            throw new Exceptions("500"," No users found for given type");
        }
        model.addAttribute("users", users);
        log.info("GET request: All internal users");

        return "admin/internalusers";
    }

    @GetMapping("/admin/user/edit/{id}")
    public String editUser(Model model, @PathVariable Long id) throws Exceptions {
        User user = userService.getUserByIdAndActive(id);
        if (user == null) {
            //return "redirect:/error?code=404";
            throw new Exceptions("404"," ");
        }
        if (!user.getUserType().equals("internal")) {
            log.warn("GET request: Admin unauthrorised request access");

            //return "redirect:/error?code=401&path=request-unauthorised";
            throw new Exceptions("401","Unauthorized Request !");
        }

        model.addAttribute("user", user);
        log.info("GET request: All internal users");

        return "admin/internalusers_edit";
    }

    @PostMapping("/admin/user/edit/{id}")
    public String editSubmit(@ModelAttribute User user, @PathVariable Long id) throws Exceptions {
        User current = userService.getUserByIdAndActive(id);
        if (current == null) {
            //return "redirect:/error?code=404";
            throw new Exceptions("404"," ");
        }

        /*
        editUserFormValidator.validate(user);
        if (bindingResult.hasErrors()) {
            //return "redirect:/error?code=400?path=form-validation";
            throw new Exceptions("400","form.validation.Error");
        }
         */
        if (!current.getUserType().equals("internal")) {
            log.warn("GET request: Admin unauthrorised request access");

            //return "redirect:/error?code=401&path=request-unauthorised";
            throw new Exceptions("401","request.unauthorized");
        }
        user.setId(id);
        log.info("POST request: Internal user edit");
        user = userService.editUser(user);
        if (user == null) {
            //return "redirect:/error?code=500";
            throw new Exceptions("500"," Internal User Edit request Failed");
        }

        return "redirect:/admin/user?successEdit=true";
    }

    @GetMapping("/admin/user/delete/{id}")
    public String deleteUser(Model model, @PathVariable Long id) throws Exceptions {
        User user = userService.getUserByIdAndActive(id);
        if (user == null) {
            //return "redirect:/error?code=404";
            throw new Exceptions("404"," ");
        }
        if (!user.getUserType().equals("internal")) {
            log.warn("GET request: Admin unauthrorised request access");

            //return "redirect:/error?code=401&path=request-unauthorised";
            throw new Exceptions("401","request.unauthorized");
        }

        model.addAttribute("user", user);
        log.info("GET request: Delete internal user");

        return "admin/internalusers_delete";
    }

    @PostMapping("/admin/user/delete/{id}")
    public String deleteSubmit(@ModelAttribute User user, @PathVariable Long id, BindingResult bindingResult) throws Exceptions {
        User current = userService.getUserByIdAndActive(id);
        if (current == null) {
            //return "redirect:/error?code=404";
            throw new Exceptions("404"," ");
        }
        if (!current.getUserType().equals("internal")) {
            log.warn("GET request: Admin unauthrorised request access");

            //return "redirect:/error?code=401&path=request-unauthorised";
            throw new Exceptions("401","Unauthorized request !!");
        }

        userService.deleteUser(id);
        log.info("POST request: Employee New modification request");

        return "redirect:/admin/user?successDelete=true";
    }


    @GetMapping("/admin/user/{id}")
    public String getUserDetail(Model model, @PathVariable Long id) throws Exceptions {
        User user = userService.getUserByIdAndActive(id);
        if (user == null) {
            //return "redirect:/error?code=404";
            throw new Exceptions("404"," ");
        }
        if (!user.getUserType().equals("internal")) {
            log.warn("GET request: Unauthorized request for external user");

            //return "redirect:/error?code=409";
            throw new Exceptions("409"," ");
        }

        model.addAttribute("user", user);
        log.info("GET request: Internal user details by id");

        return "admin/userdetail";
    }

    @GetMapping("/admin/user/request")
    public String getAllUserRequest(Model model) {
        List<ModificationRequest> modificationRequests = ModificationRequestService.getModificationRequests("pending", "internal");
        if (modificationRequests == null) {
            model.addAttribute("modificationrequests", new ArrayList<ModificationRequest>());
        }
        else {
            model.addAttribute("modificationrequests", modificationRequests);
        }
        log.info("GET request: All user requests");

        return "admin/modificationrequests";
    }

    @GetMapping("/admin/user/request/view/{id}")
    public String getUserRequest(Model model, @PathVariable() UUID id) throws Exceptions {
        ModificationRequest modificationRequest = ModificationRequestService.getModificationRequest(id);

        if (modificationRequest == null) {
            //return "redirect:/error?code=404&path=request-invalid";
            throw new Exceptions("404","Invalid Request !");
        }
        if (!modificationRequest.getUserType().equals("internal")) {
            log.warn("GET request: Admin unauthrorised request access");

            //return "redirect:/error?code=401&path=request-unauthorised";
            throw new Exceptions("401","Unauthorised Request !");
        }
        model.addAttribute("modificationrequest", modificationRequest);
        log.info("GET request: User modification request by ID");

        return "admin/modificationrequest_detail";
    }

    @PostMapping("/admin/user/request/{requestId}")
    public String approveEdit(@PathVariable UUID requestId, @ModelAttribute ModificationRequest request) throws Exceptions {
        String status = request.getStatus();
        if (status == null || !(request.getStatus().equals("approved") || request.getStatus().equals("rejected"))) {
            //return "redirect:/error?code=400&path=request-action-invalid";
            throw new Exceptions("400","Invalid Request Action !");
        }

        // checks validity of request
        if (ModificationRequestService.getModificationRequest(requestId) == null) {
            //return "redirect:/error?code=404&path=request-invalid";
            throw new Exceptions("404","Invalid Request !");
        }
        request.setModificationRequestId(requestId);

        // checks if admin is authorized for the request to approve
        if (!ModificationRequestService.verifyModificationRequestUserType(requestId, "internal")) {
            log.warn("GET request: Admin unauthrorised request access");

            //return "redirect:/error?code=401&path=request-unauthorised";
            throw new Exceptions("401","Unauthorised Request !");
        }

        request.setUserType("internal");
        request.setStatus(status);
        if (status.equals("approved")) {
            ModificationRequestService.approveModificationRequest(request);
        }
        // rejects request
        else {
            ModificationRequestService.rejectModificationRequest(request);
        }
        log.info("POST request: Admin approves modification request");

        return "redirect:/admin/user/request?successAction=true";
    }

    @GetMapping("/admin/user/request/delete/{id}")
    public String getDeleteRequest(Model model, @PathVariable() UUID id) throws Exceptions {
        ModificationRequest modificationRequest = ModificationRequestService.getModificationRequest(id);

        if (modificationRequest == null) {
            //return "redirect:/error?code=404&path=request-invalid";
            throw new Exceptions("404","Invalid Request !");
        }
        if (!modificationRequest.getUserType().equals("internal")) {
            log.warn("GET request: Admin unauthrorised request access");

            //return "redirect:/error?code=401&path=request-unauthorised";
            throw new Exceptions("401","Unauthorised Request !");
        }
        model.addAttribute("modificationrequest", modificationRequest);
        log.info("GET request: User modification request by ID");


        return "admin/modificationrequest_delete";
    }

    @PostMapping("/admin/user/request/delete/{requestId}")
    public String deleteRequest(@PathVariable UUID requestId, @ModelAttribute ModificationRequest request, BindingResult bindingResult) throws Exceptions {
        request = ModificationRequestService.getModificationRequest(requestId);

        // checks validity of request
        if (request == null) {
            //return "redirect:/error?code=404&path=request-invalid";
            throw new Exceptions("404","Invalid Request !");
        }

        if (!ModificationRequestService.verifyModificationRequestUserType(requestId, "internal")) {
            log.warn("GET request: Admin unauthrorised request access");

            //return "redirect:/error?code=401&path=request-unauthorised";
            throw new Exceptions("401","Unauthorised Request !");
        }
        ModificationRequestService.deleteModificationRequest(request);
        log.info("POST request: Admin approves modification request");

        return "redirect:/admin/user/request?successDelete=true";
    }

    @RequestMapping("/admin/syslogs")
    public String adminControllerSystemLogs(Model model) {
        return "admin/systemlogs";
    }

    /**Returns a list of all users */
    @GetMapping("/admin/user/pii")
    public String adminAccessPII(Model model){
        List <User> userList = ModificationRequestService.ListAllPII();
        model.addAttribute("users", userList);

        return "admin/accesspii";
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