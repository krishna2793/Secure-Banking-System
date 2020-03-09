package edu.asu.sbs.repositories;

import edu.asu.sbs.models.Account;
import edu.asu.sbs.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account,Integer> {
    Optional<Account> findOneByAccountNumberEquals(String s);
}
