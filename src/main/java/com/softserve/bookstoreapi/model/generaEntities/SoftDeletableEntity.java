package com.softserve.bookstoreapi.model.generaEntities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletableEntity extends AuditableEntity {

    @Column(nullable = false)
    private Boolean isActive = true;

    public boolean isDeleted() {
        return !isActive;
    }
}
