package edu.asu.sbs.services;

import com.google.common.collect.Lists;
import edu.asu.sbs.config.RequestType;
import edu.asu.sbs.config.StatusType;
import edu.asu.sbs.config.TransactionStatus;
import edu.asu.sbs.config.TransactionType;
import edu.asu.sbs.errors.GenericRuntimeException;
import edu.asu.sbs.models.*;
import edu.asu.sbs.repositories.*;
import edu.asu.sbs.services.dto.ProfileRequestDTO;
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
    private final AccountService accountService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ProfileRequestRepository profileRequestRepository;
    private final AccountRepository accountRepository;

    public RequestService(RequestRepository requestRepository, TransactionRepository transactionRepository, TransactionAccountLogRepository transactionAccountLogRepository, AccountService accountService, UserRepository userRepository, UserService userService, ProfileRequestRepository profileRequestRepository, AccountRepository accountRepository) {
        this.requestRepository = requestRepository;
        this.transactionRepository = transactionRepository;
        this.transactionAccountLogRepository = transactionAccountLogRepository;
        this.accountService = accountService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.profileRequestRepository = profileRequestRepository;
        this.accountRepository = accountRepository;
    }

    public List<ProfileRequestDTO> getAllAdminRequests() {
        List<ProfileRequestDTO> RequestDTOList = Lists.newArrayList();
        List<Request> requestList = requestRepository.findByRequestTypeInAndIsDeleted(Lists.newArrayList(RequestType.TIER1_TO_TIER2, RequestType.TIER2_TO_TIER1, RequestType.UPDATE_EMP_PROFILE), false);
        for (Request request : requestList) {
            ProfileRequestDTO RequestDTO = new ProfileRequestDTO();
            RequestDTO.setEmail(request.getLinkedProfileRequest().getEmail());
            RequestDTO.setPhoneNumber(request.getLinkedProfileRequest().getPhoneNumber());
            RequestDTO.setRequestId(request.getRequestId());
            RequestDTO.setStatus(request.getStatus());
            RequestDTO.setRoleChange(request.getLinkedProfileRequest().isChangeRoleRequest());
            RequestDTO.setDescription(request.getDescription());
            RequestDTO.setCreatedDate(request.getCreatedDate());
            RequestDTO.setModifiedDate(request.getModifiedDate());
            RequestDTOList.add(RequestDTO);
        }
        return RequestDTOList;
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

    @Transactional
    public void updateUserProfile(Request request, User approver, String requestType, String action) {

        if (request.getLinkedProfileRequest().getPhoneNumber().isEmpty() && request.getLinkedProfileRequest().getEmail().isEmpty()) {
            throw new GenericRuntimeException("Both phone number and email are empty");
        }

        request.setRequestType(requestType);
        request.setApprovedBy(approver);
        request.setStatus(action);
        request.setModifiedDate(Instant.now());
        request.setDeleted(true);
        requestRepository.save(request);

        User user = request.getRequestBy();
        switch (action) {
            case StatusType.APPROVED:
                if (!request.getLinkedProfileRequest().getPhoneNumber().isEmpty()) {
                    user.setPhoneNumber(request.getLinkedProfileRequest().getPhoneNumber());
                }
                if (!request.getLinkedProfileRequest().getEmail().isEmpty()) {
                    user.setEmail(request.getLinkedProfileRequest().getEmail());
                }
                userRepository.save(user);
                break;
            case StatusType.DECLINED:
                //don't do anything
                break;
            default:
                throw new GenericRuntimeException("Invalid Action");
        }
    }

    @Transactional
    public void updateAccountCreationRequest(Request request, User approver, String requestType, String action) {
        request.setRequestType(requestType);
        request.setApprovedBy(approver);
        request.setStatus(action);
        request.setModifiedDate(Instant.now());
        requestRepository.save(request);

        Account account = request.getLinkedAccount();

        switch (action) {
            case StatusType.APPROVED:
                account.setActive(true);
                accountRepository.save(account);
                break;
            case StatusType.DECLINED:
                accountService.deleteAccount(account);
                break;
            default:
                throw new GenericRuntimeException("Invalid Action");
        }
    }

    public List<ProfileRequestDTO> getAllProfileUpdateRequests(String requestType) {
        List<ProfileRequestDTO> RequestDTOList = Lists.newArrayList();
        List<Request> requestList = requestRepository.findByRequestTypeInAndIsDeleted(Lists.newArrayList(requestType), false);
        for (Request request : requestList) {
            ProfileRequestDTO RequestDTO = new ProfileRequestDTO();
            RequestDTO.setEmail(request.getLinkedProfileRequest().getEmail());
            RequestDTO.setPhoneNumber(request.getLinkedProfileRequest().getPhoneNumber());
            RequestDTO.setRequestId(request.getRequestId());
            RequestDTO.setStatus(request.getStatus());
            RequestDTO.setDescription(request.getDescription());
            RequestDTO.setCreatedDate(request.getCreatedDate());
            RequestDTO.setModifiedDate(request.getModifiedDate());
            RequestDTOList.add(RequestDTO);
        }
        return RequestDTOList;
    }

    @Transactional
    public void createProfileUpdateRequest(ProfileRequestDTO requestDTO, String requestType) {

        Request request = new Request();
        request.setCreatedDate(Instant.now());
        request.setDeleted(false);
        request.setDescription(requestType);
        request.setStatus(TransactionStatus.PENDING);
        request.setRequestType(requestType);
        request.setRequestBy(userService.getCurrentUser());

        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setRequest(request);
        profileRequest.setEmail(requestDTO.getEmail());
        profileRequest.setPhoneNumber(requestDTO.getPhoneNumber());
        request.setLinkedProfileRequest(profileRequest);
        requestRepository.save(request);
        profileRequestRepository.save(profileRequest);
    }

    @Transactional
    public void createChangeRoleRequest(String requestType) {
        Request request = new Request();
        request.setCreatedDate(Instant.now());
        request.setDeleted(false);
        request.setDescription(requestType);
        request.setStatus(TransactionStatus.PENDING);
        request.setRequestType(requestType);
        request.setRequestBy(userService.getCurrentUser());

        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setRequest(request);
        profileRequest.setChangeRoleRequest(true);
        request.setLinkedProfileRequest(profileRequest);
        requestRepository.save(request);
        profileRequestRepository.save(profileRequest);

    }

    @Transactional
    public void updateChangeRoleRequest(Request request, String status, User approver) {
        request.setStatus(status);
        request.setApprovedBy(approver);
        request.setModifiedDate(Instant.now());
        requestRepository.save(request);
    }

    /*
    public void createAdditionalAccountRequest(CreateAccountDTO createAccountDTO, String requestType) {

        Request request = new Request();
        request.setCreatedDate(Instant.now());
        request.setDeleted(false);
        request.setDescription(requestType);
        request.setStatus(TransactionStatus.PENDING);
        request.setRequestType(requestType);
        request.setRequestBy(userService.getCurrentUser());

        String accNum = accountService.getNumericString(MAX_ACCOUNT_NUM_LEN);
        createAccountDTO.setAccountNumber(accNum);
        Account account = accountService.createAccount(userService.getCurrentUser(), createAccountDTO);
        request.setLinkedAccount();
        requestRepository.save(request);
    }
    */
}
