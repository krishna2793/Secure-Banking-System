package edu.asu.sbs.services;

import edu.asu.sbs.models.ModificationRequest;
import edu.asu.sbs.models.User;
import edu.asu.sbs.repositories.ModificationRequestRepository;
import edu.asu.sbs.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ModificationRequestService {
    static UserService userService;
    final ModificationRequestRepository modificationRequestRepository;
    static UserRepository userRepository;
    static ModificationRequestServiceImplm modificationRequestServiceImplm;
    public ModificationRequestService(UserService userService, ModificationRequestRepository modificationRequestRepository, UserRepository userRepository, ModificationRequestServiceImplm modificationRequestServiceImplm) {
        this.userService = userService;
        this.modificationRequestRepository = modificationRequestRepository;
        this.userRepository = userRepository;
        this.modificationRequestServiceImplm = modificationRequestServiceImplm;
    }

    /**
     * Get all pending internal user request
     *
     * @param type
     *            The type of user
     * @param status
     *            The status of request
     * @return List<modificationRequest>
     */
    public static List<ModificationRequest> getModificationRequests(String status, String type) {
        log.info("Getting all modification request by user type and status of request");

        return modificationRequestServiceImplm.findAllbyStatusAndUserType(status, type);
    }

    /**
     * Get request by Id
     *
     * @param requestId
     *            The id of the request to be retrieved
     * @return modificationRequest
     */
    public static ModificationRequest getModificationRequest(UUID requestId) {
        log.info("Getting modification request by ID");

        return modificationRequestServiceImplm.findById(requestId);
    }

    /**
     * Verify the usertype of request
     *
     * @param requestId
     *            The id of the request to be verified
     * @param type
     *            The type of user of the request
     * @return boolean
     */
    public static boolean verifyModificationRequestUserType(UUID requestId, String type) {
        ModificationRequest request = modificationRequestServiceImplm.findById(requestId);
        if (request == null) {
            return false;
        }
        if (!request.getUserType().equals(type)) {
            return false;
        }

        log.info("Verifying type of user for request");

        return true;
    }

    public static List<User> ListAllPII() {
        return modificationRequestServiceImplm.accessPii();
    }

    /**
     * Verify email and update request to pending
     *
     * @param status
     *            The status of the request
     * @param requestId
     *            The id of the request to be verified
     * @return boolean
     */
    public boolean verifyModificationRequest(String status, UUID requestId) {
        ModificationRequest request = modificationRequestServiceImplm.findById(requestId);
        if (request == null) {
            return false;
        }
        if (!request.getStatus().equals(status)) {
            return false;
        }

        log.info("Verifying changes email address of user");

        request.setStatus("pending");
        //update(request); //ankit- add method

        return true;
    }

    /**
     * Deletes a request
     *
     * @param request
     *            The request to be deleted
     */
    public static void deleteModificationRequest(ModificationRequest request) {
        //modificationRequestServiceImplm.remove(request);  //ankit-add method
        return;
    }

    /**
     * Approves user request
     *
     * @param request
     *            The id of the request to be approved
     * @return modificationRequest
     */

    public static ModificationRequest approveModificationRequest(ModificationRequest request) {
        ModificationRequest current = modificationRequestServiceImplm.findById(request.getModificationRequestId());
        User user = current.getUser();

        // If email has been taken
        if ((!request.getEmail().equals(user.getEmail()) && (userRepository.findByEmail(request.getEmail()) || userRepository.findByEmail(request.getEmail())))
                || (!request.getPhone().equals(user.getPhoneNumber()) && userRepository.findByPhoneNumber(request.getPhone()))) {
            log.info("Rejecting request due to unique contraint conflict");

            /*
            // Sends an email if email and phone clash with existing users
            SimpleMailMessage message = new SimpleMailMessage();
            message.setText(env.getProperty("modification.request.failure.body"));
            message.setSubject(env.getProperty("modification.request.failure.subject"));
            message.setTo(user.getEmail());
            emailService.sendEmail(message);
             */

            // update request
            request.setActive(false);
            request.setStatus("rejected");
            request.setModifiedOn(LocalDateTime.now());
            //modificationRequestServiceImplm.update(request);  // ankit - add method

            return null;
        }
        log.info("Request for approving modification request");

        current.setEmail(request.getEmail());
        current.setPhone(request.getPhone());
        current.setFirstName(request.getFirstName());
        current.setMiddleName(request.getMiddleName());
        current.setLastName(request.getLastName());
        current.setAddressLine1(request.getAddressLine1());
        current.setAddressLine2(request.getAddressLine2());
        current.setCity(request.getCity());
        current.setState(request.getState());
        current.setZip(request.getZip());

        // Update User
        user.setFirstName(request.getFirstName());
        //user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhone());
        //user.setAddressLine1(request.getAddressLine1());
        //user.setAddressLine2(request.getAddressLine2());
        //user.setCity(request.getCity());
        //user.setState(request.getState());
        //user.setZip(request.getZip());
        if (request.getUserType().equals("internal")) {
            user.setUserType(request.getRole());
        }
        //user.setModifiedOn(LocalDateTime.now());
        userRepository.save(user);

        // Update request
        current.setActive(false);
        current.setModifiedOn(LocalDateTime.now());
        current.setApprovedBy(userService.getCurrentUser());
        current.setStatus("approved");
        //current = modificationRequestServiceImplm.update(current);  //ankit - add method

        return current;
    }

    /**
     * Rejects user request
     *
     * @param request
     *            The id of the request to be approved
     * @return modificationRequest
     */

    public static ModificationRequest rejectModificationRequest(ModificationRequest request) {
        ModificationRequest current = modificationRequestServiceImplm.findById(request.getModificationRequestId());
        User user = current.getUser();

        log.info("Request for rejecting modification request");

        // update request
        current.setActive(false);
        current.setStatus("rejected");
        current.setModifiedOn(LocalDateTime.now());
        current.setApprovedBy(userService.getCurrentUser());
        /*  // ankit- add method
        //current = modificationRequestServiceImplm.update(current);
        // Sends an email if request is rejected
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText(env.getProperty("modification.request.reject.body"));
        message.setSubject(env.getProperty("modification.request.reject.subject"));
        message.setTo(user.getEmail());
        emailService.sendEmail(message);
         */

        return current;
    }
}
