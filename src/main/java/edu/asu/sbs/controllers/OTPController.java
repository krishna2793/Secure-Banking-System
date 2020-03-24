package edu.asu.sbs.controllers;

import edu.asu.sbs.services.MailService;
import edu.asu.sbs.services.OTPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/otp")
public class OTPController {

    private final OTPService otpService;
    private final MailService mailService;

    public OTPController(OTPService otpService, MailService mailService) {
        this.otpService = otpService;
        this.mailService = mailService;
    }


    @GetMapping("/generateOTP")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void generateOTP() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        otpService.generateOTP(auth).ifPresent(mailService::sendOTPMail);
    }
}
