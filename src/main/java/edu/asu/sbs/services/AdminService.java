package edu.asu.sbs.services;

import edu.asu.sbs.config.UserType;
import edu.asu.sbs.errors.EmailAlreadyUsedException;
import edu.asu.sbs.errors.PhoneNumberAlreadyUsedException;
import edu.asu.sbs.errors.SsnAlreadyUsedException;
import edu.asu.sbs.errors.UsernameAlreadyUsedException;
import edu.asu.sbs.models.User;
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
import java.util.Calendar;
import java.util.Optional;

@Slf4j
@Service
public class AdminService {

    final AdminRepository adminRepository;
}