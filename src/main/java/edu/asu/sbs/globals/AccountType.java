package edu.asu.sbs.globals;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public final class AccountType {
    private static final String CURRENT = "CURRENT";
    private static final String SAVINGS = "SAVINGS";
    private static final String CHECKING = "CHECKING";

    private AccountType() {
    }
}
