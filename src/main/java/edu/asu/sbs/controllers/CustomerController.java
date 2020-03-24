package edu.asu.sbs.controllers;

import edu.asu.sbs.config.UserType;
import edu.asu.sbs.globals.CreditDebitType;
import edu.asu.sbs.models.Account;
import edu.asu.sbs.models.User;
import edu.asu.sbs.services.AccountService;
import edu.asu.sbs.services.UserService;
import edu.asu.sbs.services.dto.AccountDTO;
import edu.asu.sbs.services.dto.CreditDebitDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@PreAuthorize("hasAnyAuthority('" + UserType.USER_ROLE + "')")
@RestController
@RequestMapping("/api/v1/customer")
public class CustomerController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

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
