package edu.asu.sbs.services;

import com.google.common.collect.Lists;
import edu.asu.sbs.config.RequestType;
import edu.asu.sbs.config.StatusType;
import edu.asu.sbs.config.TransactionType;
import edu.asu.sbs.errors.GenericRuntimeException;
import edu.asu.sbs.models.Request;
import edu.asu.sbs.models.Transaction;
import edu.asu.sbs.models.TransactionAccountLog;
import edu.asu.sbs.models.User;
import edu.asu.sbs.repositories.RequestRepository;
import edu.asu.sbs.repositories.TransactionAccountLogRepository;
import edu.asu.sbs.repositories.TransactionRepository;
import edu.asu.sbs.services.dto.Tier2RequestsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionAccountLogRepository transactionAccountLogRepository;

    public RequestService(RequestRepository requestRepository, TransactionRepository transactionRepository, TransactionAccountLogRepository transactionAccountLogRepository) {
        this.requestRepository = requestRepository;
        this.transactionRepository = transactionRepository;
        this.transactionAccountLogRepository = transactionAccountLogRepository;
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
            tier2RequestsDTO.setRequestId(request.getRequestId());
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

    @Transactional
    public void modifyRequest(Request request, User user, String requestType, String action) {
        request.setRequestType(requestType);
        request.setApprovedBy(user);
        request.setStatus(action);
        request.setModifiedDate(Instant.now());
        Transaction transaction = request.getLinkedTransaction();
        TransactionAccountLog transactionAccountLog = transaction.getLog();

        switch (action) {
            case StatusType.APPROVED:
                if (transaction.getTransactionType().equals(TransactionType.DEBIT)) {
                    transaction.getToAccount().setAccountBalance(transaction.getToAccount().getAccountBalance() + transaction.getTransactionAmount());
                } else {
                    transaction.getFromAccount().setAccountBalance(transaction.getFromAccount().getAccountBalance() + transaction.getTransactionAmount());
                }
                transaction.setStatus(StatusType.APPROVED);
                break;
            case StatusType.DECLINED:
                if (transaction.getTransactionType().equals(TransactionType.DEBIT)) {
                    transaction.getFromAccount().setAccountBalance(transaction.getFromAccount().getAccountBalance() + transaction.getTransactionAmount());
                } else {
                    transaction.getToAccount().setAccountBalance(transaction.getToAccount().getAccountBalance() + transaction.getTransactionAmount());
                }
                transaction.setStatus(StatusType.DECLINED);
                break;
            default:
                throw new GenericRuntimeException("Invalid Action");
        }
        transactionAccountLog.setLogTime(Instant.now());
        transactionAccountLog.setLogDescription(transactionAccountLog.getLogDescription() + "\n Transaction Approved on " + Instant.now());
        transactionAccountLogRepository.save(transactionAccountLog);
        transactionRepository.save(transaction);
        requestRepository.save(request);
    }
}
