package edu.asu.sbs.globals;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public final class UserType {
    private static final String TIER1 = "TIER1";
    private static final String TIER2 = "TIER2";
    private static final String TIER3 = "TIER3";
    private static final String MERCHANT = "MERCHANT";
    private static final String EXTERNAL_USER = "EXTERNAL_USER";

    private UserType() {
    }
}
