package edu.asu.sbs.controllers;

import edu.asu.sbs.config.UserType;
import edu.asu.sbs.models.Request;
import edu.asu.sbs.services.RequestService;
import edu.asu.sbs.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@PreAuthorize("hasAnyAuthority('" + UserType.EMPLOYEE_ROLE2 + "')")
@RestController
@RequestMapping("/api/v1/tier2")
public class Tier2Controller {

    private final TransactionService transactionService;
    private final RequestService requestService;

    public Tier2Controller(TransactionService transactionService, RequestService requestService) {
        this.transactionService = transactionService;
        this.requestService = requestService;
    }

    @GetMapping("/viewRequests")
    @ResponseBody
    public List<Request> viewRequests() {
        return requestService.getAllRequests();
    }

    @PutMapping("/approveCriticalTransaction")
    public void approveCriticalTransaction(@RequestParam Long requestId) {
        transactionService.approveCriticalTransaction(requestId);
    }

}
