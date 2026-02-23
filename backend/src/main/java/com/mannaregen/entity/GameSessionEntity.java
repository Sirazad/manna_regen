package com.mannaregen.entity;

import com.mannaregen.model.WielderType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_session")
public class GameSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String characterName;

    @Column(nullable = false)
    private LocalDateTime savedAt;

    private int maxManna;
    private int currentManna;
    private int maxPszi;
    private int currentPszi;

    @Enumerated(EnumType.STRING)
    private WielderType wielderType;

    private double level;
    private int stamina;
    private double magicExhaustionLimit;
    private long currentTimeSegments;

    public GameSessionEntity() {
    }

    @PrePersist
    public void prePersist() {
        if (savedAt == null) {
            savedAt = LocalDateTime.now();
        }
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }

    public int getMaxManna() {
        return maxManna;
    }

    public void setMaxManna(int maxManna) {
        this.maxManna = maxManna;
    }

    public int getCurrentManna() {
        return currentManna;
    }

    public void setCurrentManna(int currentManna) {
        this.currentManna = currentManna;
    }

    public int getMaxPszi() {
        return maxPszi;
    }

    public void setMaxPszi(int maxPszi) {
        this.maxPszi = maxPszi;
    }

    public int getCurrentPszi() {
        return currentPszi;
    }

    public void setCurrentPszi(int currentPszi) {
        this.currentPszi = currentPszi;
    }

    public WielderType getWielderType() {
        return wielderType;
    }

    public void setWielderType(WielderType wielderType) {
        this.wielderType = wielderType;
    }

    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        this.level = level;
    }

    public int getStamina() {
        return stamina;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }

    public double getMagicExhaustionLimit() {
        return magicExhaustionLimit;
    }

    public void setMagicExhaustionLimit(double magicExhaustionLimit) {
        this.magicExhaustionLimit = magicExhaustionLimit;
    }

    public long getCurrentTimeSegments() {
        return currentTimeSegments;
    }

    public void setCurrentTimeSegments(long currentTimeSegments) {
        this.currentTimeSegments = currentTimeSegments;
    }
}
