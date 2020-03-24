package edu.asu.sbs.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Template;
import edu.asu.sbs.config.UserType;
import edu.asu.sbs.errors.UnauthorizedAccessExcpetion;
import edu.asu.sbs.globals.CreditDebitType;
import edu.asu.sbs.loader.HandlebarsTemplateLoader;
import edu.asu.sbs.models.Account;
import edu.asu.sbs.models.User;
import edu.asu.sbs.services.AccountService;
import edu.asu.sbs.services.UserService;
import edu.asu.sbs.services.dto.AccountDTO;
import edu.asu.sbs.services.dto.CreditDebitDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
@PreAuthorize("hasAnyAuthority('" + UserType.USER_ROLE + "')")
@RestController
@RequestMapping("/api/v1/customer")
public class CustomerController {

    private AccountService accountService;
    private UserService userService;
    private final HandlebarsTemplateLoader handlebarsTemplateLoader;
    ObjectMapper mapper = new ObjectMapper();
    public CustomerController(AccountService accountService, UserService userService, HandlebarsTemplateLoader handlebarsTemplateLoader) {
        this.handlebarsTemplateLoader = handlebarsTemplateLoader;
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping("/profile")
    @ResponseBody
    public String getProfile() throws UnauthorizedAccessExcpetion, IOException {
        User user = userService.getCurrentUser();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        mapper.setDateFormat(df);
        if (user == null) {
            log.info("GET request: Unauthorized request for admin user detail");
            throw new UnauthorizedAccessExcpetion("401", "Unauthorized Access !");
        }

        JsonNode result = mapper.valueToTree(user);
        Template template = handlebarsTemplateLoader.getTemplate("profileUser");
        log.info("GET request: Admin user detail");
        return template.apply(handlebarsTemplateLoader.getContext(result));
    }

    @GetMapping("/summary")
    @ResponseBody
    public List<Account> getAccounts() {

        User currentUser = userService.getCurrentUser();
        return accountService.getAccountsForUser(currentUser);
    }

    @PutMapping("/credit")
    void credit(@RequestBody CreditDebitDTO creditDebitRequest) {
        User currentUser = userService.getCurrentUser();
        try {
            if (creditDebitRequest.getCreditDebitType() != CreditDebitType.CREDIT)
                throw new Exception("Invalid Request");
            accountService.makeSelfTransaction(currentUser, creditDebitRequest);
            System.out.println("Credit success");
        } catch(Exception e) {
            //redirect error message
            System.out.println(e.getMessage());
        }

    }
    @PutMapping("/debit")
    void debit(@RequestBody CreditDebitDTO creditDebitRequest) {
        User currentUser = userService.getCurrentUser();
        try {
            if (creditDebitRequest.getCreditDebitType() != CreditDebitType.DEBIT)
                throw new Exception("Invalid Request");
            accountService.makeSelfTransaction(currentUser, creditDebitRequest);
            System.out.println("Credit success");
        } catch(Exception e) {
            //redirect error message
            System.out.println(e.getMessage());
        }

    }

    @PostMapping("/createAccount")
    @ResponseStatus(HttpStatus.CREATED)
    void createAccount(@RequestBody AccountDTO accountDTO) {
        User currentUser = userService.getCurrentUser();
        accountService.createAccount(currentUser,accountDTO);
    }

}
