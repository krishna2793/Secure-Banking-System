package edu.asu.sbs.security.jwt;


import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;

@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private Key key;
    private long tokenValidityInMilliseconds;
    private long tokenValidityInMillisecondsForRememberMe;
//    private final Defaults defaults;

//    public TokenProvider(Defaults defaults) {
//        this.defaults = defaults;
//    }


    @PostConstruct
    void init() {
        byte[] keyBytes;
        String secret = null;
    }

}
