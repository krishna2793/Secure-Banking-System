package edu.asu.sbs.services;

import com.google.common.collect.Lists;
import edu.asu.sbs.config.RequestType;
import edu.asu.sbs.models.Request;
import edu.asu.sbs.models.User;
import edu.asu.sbs.repositories.RequestRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RequestService {

    private final RequestRepository requestRepository;

    public RequestService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public List<Request> getAllAdminRequests() {
        List<Request> requestList = Lists.newArrayList();
        requestList.addAll(requestRepository.findAll());
        return requestList;
    }

    public Optional<Request> getRequest(Long id) {
        return requestRepository.findOneByRequestId(id);
    }

    public ArrayList<Request> getAllTier2Requests() {
        return (ArrayList<Request>) requestRepository.findByRequestTypeInAndDeletedTrue(RequestType.APPROVE_CRITICAL_TRANSACTION);
    }

    public void modifyRequest(Optional<Request> request, User user, String requestType, String action) {

        request.ifPresent(req -> {
            req.setRequestType(requestType);
            req.setApprovedBy(user);
            req.setStatus(action);
            req.setModifiedDate(Instant.now());
            requestRepository.save(req);
        });
    }
}
