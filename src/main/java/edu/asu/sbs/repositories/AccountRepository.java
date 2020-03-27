package edu.asu.sbs.repositories;

import edu.asu.sbs.models.Account;
import edu.asu.sbs.models.User;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, Long> {
    Optional<Account> findOneByAccountNumberEquals(String s);
    @Query("select bankAccount from Account bankAccount where bankAccount.user = :user")
    List<Account> findByUser(@Param("user") User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select bankAccount from Account bankAccount where bankAccount.user = :user")
    List<Account> findByUserAndLock(@Param("user") User user);
}
