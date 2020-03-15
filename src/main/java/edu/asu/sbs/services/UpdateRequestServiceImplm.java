package edu.asu.sbs.services;
import edu.asu.sbs.models.UpdateRequest;
import edu.asu.sbs.models.User;
import edu.asu.sbs.repositories.UpdateRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
class UpdateRequestServiceImplm{
    UpdateRequestRepository updateRequestRepository;

    public void UpdateRequestService(UpdateRequestRepository updateRequestRepository) {
        this.updateRequestRepository = updateRequestRepository;
    }

    @PersistenceContext
    EntityManager entityManager = null;

    public UpdateRequestServiceImplm(UpdateRequestRepository updateRequestRepository) {
        this.updateRequestRepository = updateRequestRepository;
    }

    /**
     * Returns list of all requests in the tables for status
     *
     * @return updateRequests
     */
    public List<UpdateRequest> findAll() {
        return this.entityManager.createQuery("SELECT request from UpdateRequest request where request.active = TRUE", UpdateRequest.class)
                .getResultList();
    }

    /**
     * Returns list of all request in the tables
     * @param status
     * 			Status for which to query db
     * @param userType
     * 			type of user(internal or external) for which to query db
     *
     * @return updateRequests
     */
    public List<UpdateRequest> findAllbyStatusAndUserType(String status, String userType) {
        return this.entityManager.createQuery("SELECT request from UpdateRequest request where request.status = :status AND userType = :type AND request.active = TRUE", UpdateRequest.class)
                .setParameter("status", status)
                .setParameter("type", userType)
                .getResultList();
    }

    /**
     * Returns request for the given Id.
     *
     * @param id
     *            The id to query db
     * @return User
     */
    public UpdateRequest findById(UUID id) {
        try {
            return this.entityManager.createQuery("SELECT request from UpdateRequest request where request.updateRequestId = :id AND request.active = TRUE", UpdateRequest.class)
                    .setParameter("id", id)
                    .getSingleResult();
        }
        catch(NoResultException e) {
            // returns null if no user if found
            return null;
        }
    }

    /**
     * Returns list of all active requests in the tables for a user
     *
     * @return updateRequests
     */

    public List<UpdateRequest> findAllbyUser(User user) {
        return this.entityManager.createQuery("SELECT request from UpdateRequest request where request.user = :user AND request.active = TRUE", UpdateRequest.class)
                .setParameter("user", user)
                .getResultList();
    }

    /* (non-Javadoc)
     * @see securbank.dao.UpdateRequestDao#findAllbyStatusAndUserTypeAndUsers(java.lang.String, java.lang.String, java.util.List)
     */

    public List<UpdateRequest> findAllbyStatusAndUserTypeAndUsers(String status, String userType,
                                                                  List<User> users) {
        return this.entityManager.createQuery("SELECT request from UpdateRequest request " +
                "WHERE request.status = :status " +
                "AND userType = :type " +
                "AND request.active = TRUE " +
                "AND request.user IN :users", UpdateRequest.class)
                .setParameter("status", status)
                .setParameter("type", userType)
                .setParameter("users", users)
                .getResultList();
    }

    public List<User> accessPii() {
        return this.entityManager.createQuery("SELECT user from User user where user.active=true", User.class)
                .getResultList();
    }
}