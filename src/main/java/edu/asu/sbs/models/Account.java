package edu.asu.sbs.models;


import edu.asu.sbs.config.Constants;
import edu.asu.sbs.globals.AccountType;
import edu.asu.sbs.globals.AccountTypeAttributeConverter;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Account implements Serializable {

    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Pattern(regexp = Constants.ACCOUNT_NUMBER_REGEX)
    @Column(unique = true, nullable = false, length = 17)
    @Size(min = 1, max = 17)
    private String accountNumber;

    @NotNull
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @Convert(converter = AccountTypeAttributeConverter.class)
    private AccountType accountType;

    @NotNull
    @Column(nullable = false)
    private Double accountBalance;

    @NotNull
    @Column(nullable = false)
    private boolean isActive;

    @ManyToOne
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @OneToMany(mappedBy = "account")
    private Set<TransactionAccountLog> accountLogs = new HashSet<>();

    @OneToMany(mappedBy = "fromAccount")
    private Set<Transaction> debitTransactions = new HashSet<>();

    @OneToMany(mappedBy = "toAccount")
    private Set<Transaction> creditTransactions = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "chequeFromAccount")
    private Set<Cheque> fromCheques = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "chequeToAccount")
    private Set<Cheque> toCheques = new HashSet<>();

}
