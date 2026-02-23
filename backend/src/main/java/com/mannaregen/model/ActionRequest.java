package com.mannaregen.model;

/**
 * Request body for performing an action.
 */
public class ActionRequest {
    private ActionType actionType;
    /** Amount of pszi or mana to spend (for SPEND_PSZI / SPEND_MANA) */
    private int amount;
    /** Duration of the action in segments */
    private long timeSegments;
    /** Activity type (for SKIP_TIME) */
    private ActivityType activityType;
    /** Whether max pain points are affected (for SKIP_TIME) */
    private boolean maxPainPointsAffected;
    /** If true, proceed even if magic exhaustion would go below 0 */
    private boolean forceAction;

    /** The current game state before this action */
    private GameState currentState;

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public long getTimeSegments() {
        return timeSegments;
    }

    public void setTimeSegments(long timeSegments) {
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

    public boolean isForceAction() {
        return forceAction;
    }

    public void setForceAction(boolean forceAction) {
        this.forceAction = forceAction;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(GameState currentState) {
        this.currentState = currentState;
    }
}
