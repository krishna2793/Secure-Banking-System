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
public class UserService {

    final UserRepository userRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, AuthenticationManagerBuilder authenticationManagerBuilder, TokenProvider tokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createUpdateUser(User user) {

        User u = new User();
        u.setUserName("RR");
        u.setFirstName("R");
        u.setLastName("R");
        u.setPhoneNumber("asdasd");
        u.setSsn("as");
        u.setDateOfBirth(new Date(Calendar.getInstance().getTime().getTime()));
        u.setEmail("93@asu,edu");
        u.setPasswordHash("Test");
        u.setUserType("t2");
        userRepository.save(u);
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

    @Transactional
    public void registerUser(UserDTO userDTO, String password) {
        validateUserDTO(userDTO);
        User user = new User();
        user.setUserName(userDTO.getUserName().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail().toLowerCase());
        user.setActive(false);
        user.setActivationKey(RandomUtil.generateActivationKey());
        user.setDateOfBirth(userDTO.getDateOfBirth());
        user.setSsn(userDTO.getSsn());
        user.setUserType(UserType.USER_ROLE);
        user.setPhoneNumber(userDTO.getPhoneNumber());
        userRepository.save(user);
    }

    private void validateUserDTO(UserDTO userDTO) {
        userRepository.findOneByUserNameOrEmailIgnoreCaseOrSsnOrPhoneNumber(userDTO.getUserName().toLowerCase(), userDTO.getEmail(), userDTO.getSsn(), userDTO.getPhoneNumber()).
                ifPresent(existingUser -> {
                    if (!removeNonActivatedUser(existingUser)) {
                        if (existingUser.getEmail().equalsIgnoreCase(userDTO.getEmail().toLowerCase())) {
                            throw new EmailAlreadyUsedException();
                        } else if (existingUser.getPhoneNumber().equals(userDTO.getPhoneNumber())) {
                            throw new PhoneNumberAlreadyUsedException();
                        } else if (existingUser.getSsn().equals(userDTO.getSsn())) {
                            throw new SsnAlreadyUsedException();
                        } else if (existingUser.getUserName().equalsIgnoreCase(userDTO.getUserName().toLowerCase())) {
                            throw new UsernameAlreadyUsedException();
                        }
                    }
                });
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.isActive()) {
            return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        return true;
    }

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
                .map(user -> {
                    user.setActive(true);
                    user.setActivationKey(null);
                    userRepository.save(user);
                    log.debug("Activated user: {}", user);
                    return user;
                });
    }

    @Getter
    @Setter
    public static class JWTToken {
        private String token;

        JWTToken(String token) {
            this.token = token;
        }
    }

    public User getCurrentUser() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (username == null) {
            return null;
        }

        log.info("Getting current logged in user");
        return userRepository.findByUsernameOrEmail(username);
    }

    @Transactional
    public User editUser(User userDTO) {
        User user = userRepository.findById(userDTO.getId());

        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        //user.setAddressLine1(userDTO.getAddressLine1());
        //user.setAddressLine2(userDTO.getAddressLine2());
        //user.setCity(userDTO.getCity());
        //user.setState(userDTO.getState());
        //user.setZip(userDTO.getZip());
        //user.setModifiedOn(LocalDateTime.now());
        user.setUserType(userDTO.getUserType());
        userRepository.save(userDTO);

        return user;
    }
}
