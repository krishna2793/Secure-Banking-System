package edu.asu.sbs.globals;

import edu.asu.sbs.models.Transaction;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public final class TransactionStatus {
    private final String SUCCESS = "SUCCESS";
    private final String PENDING = "PENDING";
    private final String FAILED = "FAILED";
    private TransactionStatus() {

    }
}
