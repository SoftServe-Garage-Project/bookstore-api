package com.softserve.bookstoreapi.service.impl;

import com.softserve.bookstoreapi.dto.UserRegisterRequestDTO;
import com.softserve.bookstoreapi.dto.UserRegisterResponseDTO;
import com.softserve.bookstoreapi.exception.EmailAlreadyExistsException;
import com.softserve.bookstoreapi.model.User;
import com.softserve.bookstoreapi.model.enums.UserRole;
import com.softserve.bookstoreapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.softserve.bookstoreapi.logger.LoggerUtils.obfuscate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ModelMapper mapper;

    @Transactional
    public UserRegisterResponseDTO registerUser(UserRegisterRequestDTO requestDTO) {
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("error.email.already.exists", requestDTO.getEmail());
        }

        User user = new User();
        user.setUsername(requestDTO.getUsername());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setRole(UserRole.ROLE_CUSTOMER);
        user.setBalance(BigDecimal.ZERO);

        User savedUser = userRepository.save(user);
        log.info("Successful registration for email: {}", obfuscate(requestDTO.getEmail()));
        return mapper.map(savedUser, UserRegisterResponseDTO.class);
    }
}
