package edu.asu.sbs.services;

import com.google.common.collect.Lists;
import edu.asu.sbs.config.TransactionStatus;
import edu.asu.sbs.config.TransactionType;
import edu.asu.sbs.errors.GenericRuntimeException;
import edu.asu.sbs.models.Account;
import edu.asu.sbs.models.Cheque;
import edu.asu.sbs.models.Transaction;
import edu.asu.sbs.models.TransactionAccountLog;
import edu.asu.sbs.repositories.AccountRepository;
import edu.asu.sbs.repositories.ChequeRepository;
import edu.asu.sbs.repositories.TransactionAccountLogRepository;
import edu.asu.sbs.repositories.TransactionRepository;
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

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository, TransactionAccountLogRepository transactionAccountLogRepository, ChequeRepository chequeRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transactionAccountLogRepository = transactionAccountLogRepository;
        this.chequeRepository = chequeRepository;
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
                        if (fromAccount.getAccountBalance() > transactionDTO.getTransactionAmount()) {
                            toAccount.setAccountBalance(toAccount.getAccountBalance() + transactionDTO.getTransactionAmount());
                            fromAccount.setAccountBalance(fromAccount.getAccountBalance() - transactionDTO.getTransactionAmount());
                        }
                        break;
                    case TransactionType.CREDIT:
                        if (toAccount.getAccountBalance() > transactionDTO.getTransactionAmount()) {
                            fromAccount.setAccountBalance(fromAccount.getAccountBalance() + transactionDTO.getTransactionAmount());
                            toAccount.setAccountBalance(toAccount.getAccountBalance() - transactionDTO.getTransactionAmount());
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
                transaction.setUpdatedTime(Instant.now());
                transaction.setDescription(transactionDTO.getDescription());
                transaction.setFromAccount(fromAccount);
                transaction.setToAccount(toAccount);
                transaction.setStatus(TransactionStatus.APPROVED);
                transaction.setTransactionType(transactionDTO.getTransactionType());
                transaction.setTransactionAmount(transactionDTO.getTransactionAmount());
                transaction.setLog(transactionAccountLog);
                if (!transactionStatus.equals(TransactionStatus.PENDING)) {
                    transaction.setToAccount(toAccount);
                }
                transaction.setFromAccount(fromAccount);
                accountRepository.save(toAccount);
                accountRepository.save(fromAccount);
                return transactionRepository.save(transaction);
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
        Transaction transaction = createTransaction(transactionDTO, TransactionStatus.PENDING);
        Cheque cheque = new Cheque();
        cheque.setAmount(transactionDTO.getTransactionAmount());
        cheque.setTransaction(transaction);
    }


    public void clearCheque(Long chequeId) {
        Optional<Cheque> optionalCheque = chequeRepository.findById(chequeId);
        optionalCheque.ifPresent(cheque -> {
            Transaction transaction = cheque.getTransaction();
            if (transaction.getTransactionType().equals(TransactionStatus.PENDING)) {
                Account toAccount = transaction.getToAccount();
                Account fromAccount = transaction.getFromAccount();

            }
        });

    }
}
