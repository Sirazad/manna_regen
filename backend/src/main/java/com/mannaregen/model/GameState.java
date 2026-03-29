package com.mannaregen.model;

/**
 * Represents the full game state for a character session.
 */
public class GameState {
    private int maxManna;
    private int currentManna;
    private int maxPszi;
    private int currentPszi;
    private WielderType wielderType;
    private double level;
    private int stamina;
    private double magicExhaustionLimit;
    private long currentTimeSegments; // time in segments (1 segment = 1 second)
    private long restingUntilSegments; // absolute segment until which spending is blocked

    public GameState() {
        this.wielderType = WielderType.NONE;
        this.magicExhaustionLimit = 0;
        this.currentTimeSegments = 0;
    }

    /** Magic exhaustion max is always 25 * level */
    public double getMaxMagicExhaustion() {
        return 25.0 * level;
    }

    // --- Getters and Setters ---

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

    public long getRestingUntilSegments() {
        return restingUntilSegments;
    }

    public void setRestingUntilSegments(long restingUntilSegments) {
        this.restingUntilSegments = restingUntilSegments;
    }
}
