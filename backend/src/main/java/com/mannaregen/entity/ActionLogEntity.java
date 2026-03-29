package com.mannaregen.entity;

import com.mannaregen.model.ActionType;
import com.mannaregen.model.ActivityType;
import com.mannaregen.model.WielderType;
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
    private String effects;

    // Full game state snapshot (after the action)
    private Integer snapshotMaxManna;
    private Integer snapshotCurrentManna;
    private Integer snapshotMaxPszi;
    private Integer snapshotCurrentPszi;

    @Enumerated(EnumType.STRING)
    private WielderType snapshotWielderType;

    private Double snapshotLevel;
    private Integer snapshotStamina;
    private Double snapshotMagicExhaustionLimit;
    private Long snapshotTimeSegments;
    private Long snapshotRestingUntilSegments;

    public ActionLogEntity() {
    }

    @PrePersist
    public void prePersist() {
        if (performedAt == null) {
            performedAt = LocalDateTime.now();
        }
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }

    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public Long getTimeSegments() { return timeSegments; }
    public void setTimeSegments(Long timeSegments) { this.timeSegments = timeSegments; }

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public boolean isMaxPainPointsAffected() { return maxPainPointsAffected; }
    public void setMaxPainPointsAffected(boolean maxPainPointsAffected) { this.maxPainPointsAffected = maxPainPointsAffected; }

    public boolean isForced() { return forced; }
    public void setForced(boolean forced) { this.forced = forced; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEffects() { return effects; }
    public void setEffects(String effects) { this.effects = effects; }

    public Integer getSnapshotMaxManna() { return snapshotMaxManna; }
    public void setSnapshotMaxManna(Integer snapshotMaxManna) { this.snapshotMaxManna = snapshotMaxManna; }

    public Integer getSnapshotCurrentManna() { return snapshotCurrentManna; }
    public void setSnapshotCurrentManna(Integer snapshotCurrentManna) { this.snapshotCurrentManna = snapshotCurrentManna; }

    public Integer getSnapshotMaxPszi() { return snapshotMaxPszi; }
    public void setSnapshotMaxPszi(Integer snapshotMaxPszi) { this.snapshotMaxPszi = snapshotMaxPszi; }

    public Integer getSnapshotCurrentPszi() { return snapshotCurrentPszi; }
    public void setSnapshotCurrentPszi(Integer snapshotCurrentPszi) { this.snapshotCurrentPszi = snapshotCurrentPszi; }

    public WielderType getSnapshotWielderType() { return snapshotWielderType; }
    public void setSnapshotWielderType(WielderType snapshotWielderType) { this.snapshotWielderType = snapshotWielderType; }

    public Double getSnapshotLevel() { return snapshotLevel; }
    public void setSnapshotLevel(Double snapshotLevel) { this.snapshotLevel = snapshotLevel; }

    public Integer getSnapshotStamina() { return snapshotStamina; }
    public void setSnapshotStamina(Integer snapshotStamina) { this.snapshotStamina = snapshotStamina; }

    public Double getSnapshotMagicExhaustionLimit() { return snapshotMagicExhaustionLimit; }
    public void setSnapshotMagicExhaustionLimit(Double snapshotMagicExhaustionLimit) { this.snapshotMagicExhaustionLimit = snapshotMagicExhaustionLimit; }

    public Long getSnapshotTimeSegments() { return snapshotTimeSegments; }
    public void setSnapshotTimeSegments(Long snapshotTimeSegments) { this.snapshotTimeSegments = snapshotTimeSegments; }

    public Long getSnapshotRestingUntilSegments() { return snapshotRestingUntilSegments; }
    public void setSnapshotRestingUntilSegments(Long snapshotRestingUntilSegments) { this.snapshotRestingUntilSegments = snapshotRestingUntilSegments; }
}
