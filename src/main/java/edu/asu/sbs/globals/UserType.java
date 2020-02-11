package edu.asu.sbs.globals;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class UserType {
    private static final String TIER1 = "TIER1";
    private static final String TIER2 = "TIER2";
    private static final String TIER3 = "TIER3";
}
