package edu.asu.sbs.services;

import edu.asu.sbs.models.Account;
import edu.asu.sbs.models.Transaction;
import edu.asu.sbs.config.UserType;
import edu.asu.sbs.errors.EmailAlreadyUsedException;
import edu.asu.sbs.errors.PhoneNumberAlreadyUsedException;
import edu.asu.sbs.errors.SsnAlreadyUsedException;
import edu.asu.sbs.errors.UsernameAlreadyUsedException;
import edu.asu.sbs.models.User;
import edu.asu.sbs.repositories.AccountRepository;
import edu.asu.sbs.repositories.TransactionRepository;
import edu.asu.sbs.repositories.UserRepository;
import edu.asu.sbs.security.jwt.JWTFilter;
import edu.asu.sbs.security.jwt.TokenProvider;
import edu.asu.sbs.services.dto.UserDTO;
import edu.asu.sbs.util.RandomUtil;
import edu.asu.sbs.vm.LoginVM;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Optional;

@Slf4j
@Service
public class AdminService {

    final UserRepository userRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    final AccountRepository accountRepository;
    final TransactionRepository transactionRepository;


    public AdminService(UserRepository userRepository,AccountRepository accountRepository, TransactionRepository transactionRepository, AuthenticationManagerBuilder authenticationManagerBuilder, TokenProvider tokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public ResponseEntity<JWTToken> authenticate(LoginVM loginVM) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginVM.getUserName(), loginVM.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        boolean rememberMe = loginVM.getRememberMe() != null;
        String jwt = tokenProvider.createToken(authentication, rememberMe);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
    }

    public Iterable<User> getAllEmployees(){
        return userRepository.findAll();
    }

    public Optional<User> getEmployeeById(Long Id){
        return userRepository.findById(Id);
    }

    @Getter
    @Setter
    public static class JWTToken {
        private String token;

        JWTToken(String token) {
            this.token = token;
        }
    }
}
