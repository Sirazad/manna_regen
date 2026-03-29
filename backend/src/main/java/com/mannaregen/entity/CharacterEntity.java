package com.mannaregen.entity;

import com.mannaregen.model.WielderType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "characters")
public class CharacterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private int maxManna;
    private int maxPszi;

    @Enumerated(EnumType.STRING)
    private WielderType wielderType;

    private double level;
    private int stamina;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaxManna() { return maxManna; }
    public void setMaxManna(int maxManna) { this.maxManna = maxManna; }

    public int getMaxPszi() { return maxPszi; }
    public void setMaxPszi(int maxPszi) { this.maxPszi = maxPszi; }

    public WielderType getWielderType() { return wielderType; }
    public void setWielderType(WielderType wielderType) { this.wielderType = wielderType; }

    public double getLevel() { return level; }
    public void setLevel(double level) { this.level = level; }

    public int getStamina() { return stamina; }
    public void setStamina(int stamina) { this.stamina = stamina; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
