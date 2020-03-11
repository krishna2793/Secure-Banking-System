package edu.asu.sbs.repositories;

import edu.asu.sbs.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, Integer> {

    Optional<User> findOneWithAuthoritiesByEmailIgnoreCase(String username);

    Optional<User> findOneWithUserTypeByUserName(String lowercaselogin);

    Optional<User> findOneByUserName(String userName);

    void flush();

    Optional<User> findOneByEmailIgnoreCase(String email);

    Optional<User> findOneBySsn(String ssn);

    Optional<User> findOneByPhoneNumber(String phoneNumber);

    Optional<User> findOneByUserNameOrEmailIgnoreCaseOrSsnOrPhoneNumber(String userName, String email, String ssn, String phoneNumber);

    Optional<User> findOneByActivationKey(String activationKey);

    User findByUsernameOrEmail(String username);

    User findById(Long id);

    List<User> findAllByType(String type);

    boolean findByEmail(String email);
    boolean findByPhoneNumber( String ph);
}
