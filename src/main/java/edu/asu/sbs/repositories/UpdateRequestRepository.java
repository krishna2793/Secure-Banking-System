package edu.asu.sbs.repositories;

import edu.asu.sbs.models.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
public interface UpdateRequestRepository extends CrudRepository<UpdateRequest, Integer> {

    List<UpdateRequest> findAllbyStatusAndUserType(String status, String userType);
    List<UpdateRequest> findAllbyUser(User user);
    List<UpdateRequest> findAllbyStatusAndUserTypeAndUsers(String status, String userType, List<User> users);
    List<UpdateRequest> findAll();
    public UpdateRequest getUpdateRequest(UUID requestId);

}