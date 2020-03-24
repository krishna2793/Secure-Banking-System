package edu.asu.sbs.services;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import edu.asu.sbs.config.Constants;
import edu.asu.sbs.models.User;
import edu.asu.sbs.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.ParametersAreNonnullByDefault;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;


@Service
@ParametersAreNonnullByDefault
public class OTPService {

    private final UserRepository userRepository;
    private LoadingCache<String, Integer> otpCache;
    private Random random = SecureRandom.getInstanceStrong();

    public OTPService(UserRepository userRepository) throws NoSuchAlgorithmException {
        super();
        this.userRepository = userRepository;
        otpCache = CacheBuilder.newBuilder().
                expireAfterWrite(Constants.EXPIRE_MINS, TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
            public Integer load(String key) {
                return 0;
            }
        });
    }

    public Integer generateOTP(String key) {
        int otp = 100000 + this.random.nextInt(900000);
        otpCache.put(key, otp);
        return otp;
    }

    public int getOTP(String key) {
        try {
            return otpCache.get(key);
        } catch (Exception e) {
            return 0;
        }
    }

    public void clearOTP(String key) {
        otpCache.invalidate(key);
    }

    public Optional<User> generateOTP(Authentication auth) {
        return userRepository.findOneByUserName(auth.getName()).map(user -> {
            user.setOtp(generateOTP(user.getEmail()));
            return user;
        });
    }
}
