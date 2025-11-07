package com.softserve.bookstoreapi.logger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggerUtils {

    public void logRegistrationSuccess(String email) {
        log.info("Successful registration for ({})", maskEmail(email));
    }

    public void logRegistrationFailure(String email, String reason) {
        log.warn("Failed registration attempt for ({}): {}", maskEmail(email), reason);
    }

    private String maskEmail(String email) {
        if (email == null) return "unknown";
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) return "***";
        return email.substring(0, Math.min(3, atIndex)) + "***" + email.substring(atIndex);
    }
}
