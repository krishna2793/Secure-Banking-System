package edu.asu.sbs.services;

import com.google.common.collect.Lists;
import edu.asu.sbs.config.RequestType;
import edu.asu.sbs.models.Request;
import edu.asu.sbs.models.User;
import edu.asu.sbs.repositories.RequestRepository;
import edu.asu.sbs.services.dto.Tier2RequestsDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

    public List<Tier2RequestsDTO> getAllTier2Requests() {
        List<Tier2RequestsDTO> tier2RequestsDTOList = Lists.newArrayList();
        List<Request> requestList = requestRepository.findByRequestTypeInAndIsDeleted(Lists.newArrayList(RequestType.APPROVE_CRITICAL_TRANSACTION), false);
        for (Request request : requestList) {
            Tier2RequestsDTO tier2RequestsDTO = new Tier2RequestsDTO();
            tier2RequestsDTO.setTransactionId(request.getLinkedTransaction().getTransactionId());
            tier2RequestsDTO.setStatus(request.getStatus());
            tier2RequestsDTO.setDescription(request.getDescription());
            tier2RequestsDTO.setFromAccount(request.getLinkedTransaction().getFromAccount().getId());
            tier2RequestsDTO.setToAccount(request.getLinkedTransaction().getToAccount().getId());
            tier2RequestsDTO.setAmount(request.getLinkedTransaction().getTransactionAmount());
            tier2RequestsDTO.setType(request.getLinkedTransaction().getTransactionType());
            tier2RequestsDTO.setCreatedDate(request.getCreatedDate());
            tier2RequestsDTO.setModifiedDate(request.getModifiedDate());
            tier2RequestsDTOList.add(tier2RequestsDTO);
        }
        return tier2RequestsDTOList;
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
