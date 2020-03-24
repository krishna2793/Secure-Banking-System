package edu.asu.sbs.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import edu.asu.sbs.config.Constants;
import edu.asu.sbs.globals.AccountType;
import edu.asu.sbs.globals.AccountTypeAttributeConverter;
import lombok.Data;
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
@Data
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
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(nullable = false)
    @JsonIgnore
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "account")
    private Set<TransactionAccountLog> accountLogs = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "fromAccount")
    private Set<Transaction> debitTransactions = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "toAccount")
    private Set<Transaction> creditTransactions = new HashSet<>();

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "chequeFromAccount")
    private Set<Cheque> fromCheques = new HashSet<>();

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "chequeToAccount")
    private Set<Cheque> toCheques = new HashSet<>();

}
