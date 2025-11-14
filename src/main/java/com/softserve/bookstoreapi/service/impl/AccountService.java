package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.dto.AccountDTO;
import com.softserve.bookstoreapi.dto.UserRegisterRequestDTO;
import com.softserve.bookstoreapi.dto.UserRegisterResponseDTO;
import com.softserve.bookstoreapi.exception.EmailAlreadyExistsException;
import com.softserve.bookstoreapi.model.Account;
import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static com.softserve.bookstoreapi.logger.LoggerUtils.obfuscate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ModelMapper mapper;

    public Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }


    @Transactional
    public UserRegisterResponseDTO registerUser(UserRegisterRequestDTO requestDTO) {
        if (accountRepository.existsByEmail(requestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("error.email.already.exists", requestDTO.getEmail());
        }

        Account account = new Account();
        account.setUsername(requestDTO.getUsername());
        account.setEmail(requestDTO.getEmail());
        account.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        account.setRole(UserRole.ROLE_CUSTOMER);
        account.setBalance(BigDecimal.ZERO);

        Account savedAccount = accountRepository.save(account);
        log.info("Successful registration for email: {}", obfuscate(requestDTO.getEmail()));
        return mapper.map(savedAccount, UserRegisterResponseDTO.class);
    }
}
