package edu.asu.sbs.services;

import com.google.common.collect.Lists;
import edu.asu.sbs.globals.AccountType;
import edu.asu.sbs.globals.CreditDebitType;
import edu.asu.sbs.models.Account;
import edu.asu.sbs.models.User;
import edu.asu.sbs.repositories.AccountRepository;
import edu.asu.sbs.repositories.TransactionAccountLogRepository;
import edu.asu.sbs.services.dto.AccountDTO;
import edu.asu.sbs.services.dto.CreditDebitDTO;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionAccountLogRepository transactionAccountLogRepository;

    public AccountService(AccountRepository accountRepository, TransactionAccountLogRepository transactionAccountLogRepository) {
        this.accountRepository = accountRepository;
        this.transactionAccountLogRepository = transactionAccountLogRepository;
    }

    public List<Account> getAccounts() {
        List<Account> accountList = Lists.newArrayList();
        accountRepository.findAll().forEach(accountList::add);
        return accountList;
    }


    public void createAccount(User customer, AccountDTO accountDTO) {
        Account newAccount = new Account();
        newAccount.setAccountBalance(accountDTO.getInitialDeposit());
        newAccount.setAccountNumber(accountDTO.getAccountNumber());
        newAccount.setAccountType(accountDTO.getAccountType());
        newAccount.setUser(customer);
        accountRepository.save(newAccount);
    }

    public void credit(Account account, Double amount) throws Exception {
        try {
            Double currentBalance = account.getAccountBalance();
            account.setAccountBalance(currentBalance + amount);
            accountRepository.save(account);
        } catch (Exception e) {
            throw new Exception("Failed to credit from account " + account.getAccountNumber(), e);
        }
    }

    public List<Account> getAccountsForUser(User user) {
        return accountRepository.findByUser(user);
    }

    @Transactional
    public void makeSelfTransaction(User currentUser, CreditDebitDTO creditDebitRequest) throws Exception {
        List<Account> currentUserAccounts = accountRepository.findByUserAndLock(currentUser);
        for (Account currentUserAccount : currentUserAccounts) {
            if (currentUserAccount.getId().equals(creditDebitRequest.getId())) {
                System.out.println("Accounts :\n" + currentUserAccount.getId());
                if (creditDebitRequest.getCreditDebitType() == CreditDebitType.CREDIT) {
                    credit(currentUserAccount, creditDebitRequest.getAmount());
                } else if (creditDebitRequest.getCreditDebitType() == CreditDebitType.DEBIT) {
                    debit(currentUserAccount, creditDebitRequest.getAmount());
                }
                return;
            }
        }
        throw new Exception("Invalid Account");
    }

    private void debit(Account account, Double amount) throws Exception {
        try {
            Double currentBalance = account.getAccountBalance();
            if (currentBalance < amount)
                throw new Exception("Insufficient Funds");
            if (account.isActive()) {
                account.setAccountBalance(currentBalance - amount);
                accountRepository.save(account);
            } else {
                throw new Exception("Inactive account");
            }
        } catch (Exception e) {
            throw new Exception("Failed to debit from account " + account.getAccountNumber(), e);
        }
    }

    public Account getAccountById(Long id) {
        return (accountRepository.getAccountById(id));
    }

    public void updateAccountType(Long accountId, AccountType accountType) {
        Account account = getAccountById(accountId);
        account.setAccountType(accountType);
    }

    public void closeUserAccount(Long id) {
        if (id != null) {
            Account account = getAccountById(id);
            account.setActive(false);
        }
    }
}
