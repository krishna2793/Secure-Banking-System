package edu.asu.sbs.controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Template;
import edu.asu.sbs.config.TransactionStatus;
import edu.asu.sbs.config.TransactionType;
import edu.asu.sbs.config.UserType;
import edu.asu.sbs.errors.UnauthorizedAccessExcpetion;
import edu.asu.sbs.loader.HandlebarsTemplateLoader;
import edu.asu.sbs.models.User;
import edu.asu.sbs.services.AccountService;
import edu.asu.sbs.services.TransactionService;
import edu.asu.sbs.services.UserService;
import edu.asu.sbs.services.dto.RequestDTO;
import edu.asu.sbs.services.dto.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

@Slf4j
@PreAuthorize("hasAnyAuthority('" + UserType.EMPLOYEE_ROLE1 + "')")
@RestController
@RequestMapping("/api/v1/tier1")
public class Tier1Controller {

    private final AccountService accountService;
    private final HandlebarsTemplateLoader handlebarsTemplateLoader;
    private final TransactionService transactionService;
    private final UserService userService;
    ObjectMapper mapper = new ObjectMapper();

    public Tier1Controller(AccountService accountService, HandlebarsTemplateLoader handlebarsTemplateLoader, TransactionService transactionService, UserService userService) {
        this.accountService = accountService;
        this.handlebarsTemplateLoader = handlebarsTemplateLoader;
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GetMapping("/profile")
    @ResponseBody
    public String getProfileTemplate() throws IOException, UnauthorizedAccessExcpetion {
        User user = userService.getCurrentUser();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        mapper.setDateFormat(df);
        if (user == null) {
            log.info("GET request: Unauthorized request for tier1 user detail");
            throw new UnauthorizedAccessExcpetion("401", "Unauthorized Access !");
        }

        JsonNode result = mapper.valueToTree(user);
        Template template = handlebarsTemplateLoader.getTemplate("profileTier1");
        log.info("GET request: Admin user detail");
        return template.apply(handlebarsTemplateLoader.getContext(result));
    }

    @GetMapping("/accounts")
    @ResponseBody
    public String getAccounts() throws IOException {
        List accounts = accountService.getAccounts();
        HashMap<String, List> resultMap = new HashMap<>();
        resultMap.put("result", accounts);
        JsonNode result = mapper.valueToTree(resultMap);
        Template template = handlebarsTemplateLoader.getTemplate("tier1UserAccounts");
        return template.apply(handlebarsTemplateLoader.getContext(result));
    }

    @GetMapping("/transactions")
    @ResponseBody
    public String viewTransactions() throws IOException {
        List transactions = transactionService.getTransactions();
        HashMap<String, List> resultMap = new HashMap<>();
        resultMap.put("result", transactions);
        JsonNode result = mapper.valueToTree(resultMap);
        Template template = handlebarsTemplateLoader.getTemplate("tier1TransactionRequests");
        return template.apply(handlebarsTemplateLoader.getContext(result));
    }

    @GetMapping("/createTransaction")
    @ResponseBody
    public String getCreateTransactionTemplate() throws IOException {
        Template template = handlebarsTemplateLoader.getTemplate("tier1CreateTransactions");
        return template.apply("");
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void createTransaction(TransactionDTO transactionDTO) {
        if (transactionDTO.getTransactionType().equals(TransactionType.CHEQUE)) {
            transactionService.issueCheque(transactionDTO);
        } else {
            transactionService.createTransaction(transactionDTO, TransactionStatus.APPROVED);
        }
    }

    @PostMapping("/issueCheck")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void issueCheque(@RequestBody TransactionDTO transactionDTO) {
        transactionService.issueCheque(transactionDTO);
    }

    @PutMapping("/clearCheck")
    @ResponseBody
    public String clearCheck(@RequestParam Long chequeId) {
        return transactionService.clearCheque(chequeId);
    }

    @PostMapping("/raiseRequest")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void raiseRequest(@RequestBody RequestDTO requestDTO) {

    }

}
