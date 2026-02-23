package com.mannaregen.entity;

import com.mannaregen.model.ActionType;
import com.mannaregen.model.ActivityType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "action_log")
public class ActionLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String characterName;

    @Column(nullable = false)
    private LocalDateTime performedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    private Integer amount;
    private Long timeSegments;

    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    private boolean maxPainPointsAffected;
    private boolean forced;

    private String description;

    public ActionLogEntity() {
    }

    @PrePersist
    public void prePersist() {
        if (performedAt == null) {
            performedAt = LocalDateTime.now();
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

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Long getTimeSegments() {
        return timeSegments;
    }

    public void setTimeSegments(Long timeSegments) {
        this.timeSegments = timeSegments;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public boolean isMaxPainPointsAffected() {
        return maxPainPointsAffected;
    }

    public void setMaxPainPointsAffected(boolean maxPainPointsAffected) {
        this.maxPainPointsAffected = maxPainPointsAffected;
    }

    public boolean isForced() {
        return forced;
    }

    public void setForced(boolean forced) {
        this.forced = forced;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
