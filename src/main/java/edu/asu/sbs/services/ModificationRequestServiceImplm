package edu.asu.sbs.services;
import edu.asu.sbs.models.ModificationRequest;
import edu.asu.sbs.models.User;
import edu.asu.sbs.repositories.ModificationRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ModificationRequestServiceImplm{
    ModificationRequestRepository modificationRequestRepository;

    public void ModificationRequestService(ModificationRequestRepository modificationRequestRepository) {
        this.modificationRequestRepository = modificationRequestRepository;
    }

    @PersistenceContext
    EntityManager entityManager = null;

    public ModificationRequestServiceImplm(ModificationRequestRepository modificationRequestRepository) {
        this.modificationRequestRepository = modificationRequestRepository;
    }

    /**
     * Returns list of all requests in the tables for status
     *
     * @return modificationRequests
     */
    public List<ModificationRequest> findAll() {
        return this.entityManager.createQuery("SELECT request from ModificationRequest request where request.active = TRUE", ModificationRequest.class)
                .getResultList();
    }

    /**
     * Returns list of all request in the tables
     * @param status
     * 			Status for which to query db
     * @param userType
     * 			type of user(internal or external) for which to query db
     *
     * @return modificationRequests
     */
    public List<ModificationRequest> findAllbyStatusAndUserType(String status, String userType) {
        return this.entityManager.createQuery("SELECT request from ModificationRequest request where request.status = :status AND userType = :type AND request.active = TRUE", ModificationRequest.class)
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
    public ModificationRequest findById(UUID id) {
        try {
            return this.entityManager.createQuery("SELECT request from ModificationRequest request where request.modificationRequestId = :id AND request.active = TRUE", ModificationRequest.class)
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
     * @return modificationRequests
     */

    public List<ModificationRequest> findAllbyUser(User user) {
        return this.entityManager.createQuery("SELECT request from ModificationRequest request where request.user = :user AND request.active = TRUE", ModificationRequest.class)
                .setParameter("user", user)
                .getResultList();
    }

    /* (non-Javadoc)
     * @see securbank.dao.ModificationRequestDao#findAllbyStatusAndUserTypeAndUsers(java.lang.String, java.lang.String, java.util.List)
     */

    public List<ModificationRequest> findAllbyStatusAndUserTypeAndUsers(String status, String userType,
                                                                        List<User> users) {
        return this.entityManager.createQuery("SELECT request from ModificationRequest request " +
                "WHERE request.status = :status " +
                "AND userType = :type " +
                "AND request.active = TRUE " +
                "AND request.user IN :users", ModificationRequest.class)
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
