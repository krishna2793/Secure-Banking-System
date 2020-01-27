package edu.asu.sbs.repositories;

import edu.asu.sbs.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
}
