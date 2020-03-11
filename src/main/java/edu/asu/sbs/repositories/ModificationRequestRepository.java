package edu.asu.sbs.repositories;

import edu.asu.sbs.models.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
public interface ModificationRequestRepository extends CrudRepository<ModificationRequest, Integer> {

    	List<ModificationRequest> findAllbyStatusAndUserType(String status, String userType);
    	List<ModificationRequest> findAllbyUser(User user);
    	List<ModificationRequest> findAllbyStatusAndUserTypeAndUsers(String status, String userType, List<User> users);
    	List<ModificationRequest> findAll();
		public ModificationRequest getModificationRequest(UUID requestId);

}