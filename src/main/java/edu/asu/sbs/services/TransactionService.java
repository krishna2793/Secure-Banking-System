package edu.asu.sbs.services;

import com.google.common.collect.Lists;
import edu.asu.sbs.config.RequestType;
import edu.asu.sbs.config.TransactionStatus;
import edu.asu.sbs.config.TransactionType;
import edu.asu.sbs.errors.GenericRuntimeException;
import edu.asu.sbs.models.*;
import edu.asu.sbs.repositories.*;
import edu.asu.sbs.services.dto.TransactionDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionAccountLogRepository transactionAccountLogRepository;
    private final ChequeRepository chequeRepository;
    private final RequestRepository requestRepository;
    private final UserService userService;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository, TransactionAccountLogRepository transactionAccountLogRepository, ChequeRepository chequeRepository, RequestRepository requestRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transactionAccountLogRepository = transactionAccountLogRepository;
        this.chequeRepository = chequeRepository;
        this.requestRepository = requestRepository;
        this.userService = userService;
    }

    public List<Transaction> getTransactions() {
        List<Transaction> transactionList = Lists.newArrayList();
        transactionRepository.findAll().forEach(transactionList::add);
        return transactionList;
    }

    @Transactional
    public Transaction createTransaction(TransactionDTO transactionDTO, String transactionStatus) {
        if (validateTransaction(transactionDTO, transactionStatus)) {
            Optional<Account> optionalFromAccount = accountRepository.findById(transactionDTO.getFromAccount());
            Optional<Account> optionalToAccount = accountRepository.findById(transactionDTO.getToAccount());
            if (optionalFromAccount.isPresent() && optionalToAccount.isPresent() && transactionDTO.getTransactionAmount() > 0) {
                Account fromAccount = optionalFromAccount.get();
                Account toAccount = optionalToAccount.get();
                switch (transactionDTO.getTransactionType()) {
                    case TransactionType.DEBIT:
                        if (fromAccount.getAccountBalance() >= transactionDTO.getTransactionAmount()) {
                            fromAccount.setAccountBalance(fromAccount.getAccountBalance() - transactionDTO.getTransactionAmount());
                            if (transactionDTO.getTransactionAmount() <= 1000 && !transactionStatus.equals(TransactionStatus.PENDING)) {
                                toAccount.setAccountBalance(toAccount.getAccountBalance() + transactionDTO.getTransactionAmount());
                            }
                        }
                        break;
                    case TransactionType.CREDIT:
                        if (toAccount.getAccountBalance() >= transactionDTO.getTransactionAmount()) {
                            toAccount.setAccountBalance(toAccount.getAccountBalance() - transactionDTO.getTransactionAmount());
                            if (transactionDTO.getTransactionAmount() <= 1000 && !transactionStatus.equals(TransactionStatus.PENDING)) {
                                fromAccount.setAccountBalance(fromAccount.getAccountBalance() + transactionDTO.getTransactionAmount());
                            }
                        }
                        break;
                    default:
                        throw new GenericRuntimeException("Invalid Transaction Type");
                }
                TransactionAccountLog transactionAccountLog = new TransactionAccountLog();
                transactionAccountLog.setLogDescription(transactionDTO.getDescription());
                transactionAccountLog.setLogTime(Instant.now());
                transactionAccountLog = transactionAccountLogRepository.save(transactionAccountLog);
                Transaction transaction = new Transaction();
                transaction.setCreatedTime(Instant.now());
                transaction.setModifiedTime(Instant.now());
                transaction.setDescription(transactionDTO.getDescription());
                transaction.setFromAccount(fromAccount);
                if (transactionDTO.getTransactionAmount() > 1000) {
                    transaction.setStatus(TransactionStatus.PENDING);
                } else {
                    transaction.setStatus(transactionStatus);
                }
                transaction.setTransactionType(transactionDTO.getTransactionType());
                transaction.setTransactionAmount(transactionDTO.getTransactionAmount());
                transaction.setLog(transactionAccountLog);
                transaction.setToAccount(toAccount);
                transaction.setFromAccount(fromAccount);
                accountRepository.save(toAccount);
                accountRepository.save(fromAccount);
                transaction = transactionRepository.save(transaction);
                if (transactionDTO.getTransactionAmount() > 1000) {
                    Request request = new Request();
                    request.setCreatedDate(Instant.now());
                    request.setLinkedTransaction(transaction);
                    request.setDeleted(false);
                    request.setModifiedDate(Instant.now());
                    request.setDescription("Transaction more than 1000$");
                    request.setStatus(TransactionStatus.PENDING);
                    request.setRequestType(RequestType.APPROVE_CRITICAL_TRANSACTION);
                    request.setRequestBy(userService.getCurrentUser());
                    requestRepository.save(request);
                }
                return transaction;
            }
        }

        return null;
    }

    private boolean validateTransaction(TransactionDTO transactionDTO, String transactionStatus) {
        if (!transactionDTO.getFromAccount().equals(transactionDTO.getToAccount())) {
            switch (transactionStatus) {
                case TransactionStatus.APPROVED:
                case TransactionStatus.PENDING:
                case TransactionStatus.REJECTED:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    @Transactional
    public void issueCheque(TransactionDTO transactionDTO) {
        transactionDTO.setTransactionType(TransactionType.DEBIT);
        Transaction transaction = createTransaction(transactionDTO, TransactionStatus.PENDING);
        Cheque cheque = new Cheque();
        cheque.setAmount(transactionDTO.getTransactionAmount());
        cheque.setTransaction(transaction);
    }

    @Transactional
    public String clearCheque(Long chequeId) {
        Optional<Cheque> optionalCheque = chequeRepository.findById(chequeId);
        if (optionalCheque.isPresent()) {
            Cheque cheque = optionalCheque.get();
            Transaction transaction = cheque.getTransaction();
            if (transaction.getTransactionType().equals(TransactionStatus.PENDING) && !cheque.isDeleted()) {
                if (transaction.getTransactionAmount() > 1000) {
                    Request request = transaction.getRequest();
                    if (!request.getStatus().equals(TransactionStatus.APPROVED)) {
                        return "Transaction to be Approved by TIER2";
                    }
                }
                Account toAccount = transaction.getToAccount();
                if (toAccount.isActive()) {
                    toAccount.setAccountBalance(toAccount.getAccountBalance() + transaction.getTransactionAmount());
                    transaction.setStatus(TransactionStatus.APPROVED);
                    TransactionAccountLog transactionAccountLog = transaction.getLog();
                    transactionAccountLog.setLogDescription("CHEQUE CLEARED");
                    cheque.setDeleted(true);
                    transactionRepository.save(transaction);
                    chequeRepository.save(cheque);
                    transactionAccountLogRepository.save(transactionAccountLog);
                }
            }
            return "Cheque Cleared";

        } else {
            return "Cheque Not Found";
        }
    }

    @Transactional
    public void approveCriticalTransaction(Long requestId) {
        Optional<Request> optionalRequest = requestRepository.findOneByRequestId(requestId);
        optionalRequest.ifPresent(request -> {
            Transaction transaction = request.getLinkedTransaction();
            if (request.getStatus().equals(TransactionStatus.PENDING) && transaction.getTransactionAmount() > 1000) {
                request.setDescription("Critical Request Approved");
                request.setModifiedDate(Instant.now());
                request.setApprovedBy(userService.getCurrentUser());
                request.setStatus(TransactionStatus.APPROVED);
                requestRepository.save(request);
            }
        });
    }
}
