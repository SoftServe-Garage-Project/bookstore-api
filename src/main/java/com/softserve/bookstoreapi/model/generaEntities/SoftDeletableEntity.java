package com.softserve.bookstoreapi.model.generaEntities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletableEntity extends AuditableEntity {

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return !active || deletedAt != null;
    }

    public void softDelete() {
        this.active = false;
        this.deletedAt = LocalDateTime.now();
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void restore() {
        this.active = true;
        this.deletedAt = null;
        this.setUpdatedAt(LocalDateTime.now());
    }
}
