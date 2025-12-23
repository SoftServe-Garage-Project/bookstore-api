package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.enums.UserRole; // Убедись, что импорт правильный
import com.softserve.bookstoreapi.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder; // Нужен для шифрования пароля

    @Value("${app.system-account.email}")
    private String systemEmail;

    @Value("${app.system-account.password}")
    private String systemPassword;

    public DataInitializer(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Проверяем, существует ли аккаунт, чтобы не создавать дубликаты
        if (accountRepository.findByEmail(systemEmail).isEmpty()) {
            createSystemAccount();
        }
    }

    private void createSystemAccount() {
        Account systemAccount = new Account();
        systemAccount.setEmail(systemEmail);
        systemAccount.setUsername("System Provider");

        systemAccount.setPassword(passwordEncoder.encode(systemPassword));
        systemAccount.setRole(UserRole.ROLE_ADMIN);
        systemAccount.setBalance(new BigDecimal("90000000.00"));

        accountRepository.save(systemAccount);
        System.out.println("System Provider account created successfully: " + systemEmail);
    }
}