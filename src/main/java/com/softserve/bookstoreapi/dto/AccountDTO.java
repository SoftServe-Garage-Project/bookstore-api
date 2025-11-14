package com.softserve.bookstoreapi.dto;

import com.softserve.bookstoreapi.model.enums.Permissions;
import com.softserve.bookstoreapi.model.enums.UserRole;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class AccountDTO {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private BigDecimal balance;
    private List<Permissions> permissions;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
