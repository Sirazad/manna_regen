package com.manna.regen.model;

/**
 * Represents the character's full state.
 * Time is measured in segments (1 segment = 1 second).
 * Magic exhaustion limit = 25 * level.
 */
public class GameCharacter {

    private double level;
    private int stamina;
    private MagicWieldingType magicWieldingType;
    private double maxManna;
    private double maxPszi;
    private double currentManna;
    private double currentPszi;
    /** Current time in segments (1 segment = 1 second). */
    private long currentTimeSegments;
    /** Current magic exhaustion value. Starts at magicExhaustionLimit and is reduced by spending. */
    private double magicExhaustion;

    public GameCharacter() {
        this.level = 1.0;
        this.stamina = 10;
        this.magicWieldingType = MagicWieldingType.NONE;
        this.maxManna = 100.0;
        this.maxPszi = 50.0;
        this.currentManna = 100.0;
        this.currentPszi = 50.0;
        this.currentTimeSegments = 0;
        this.magicExhaustion = getMagicExhaustionLimit();
    }

    /** Returns the magic exhaustion limit (maximum possible value): 25 * level. */
    public double getMagicExhaustionLimit() {
        return 25.0 * level;
    }

    public double getLevel() { return level; }
    public void setLevel(double level) {
        this.level = level;
        // Cap magic exhaustion to new limit
        double limit = getMagicExhaustionLimit();
        if (this.magicExhaustion > limit) {
            this.magicExhaustion = limit;
        }
    }

    public int getStamina() { return stamina; }
    public void setStamina(int stamina) { this.stamina = stamina; }

    public MagicWieldingType getMagicWieldingType() { return magicWieldingType; }
    public void setMagicWieldingType(MagicWieldingType magicWieldingType) { this.magicWieldingType = magicWieldingType; }

    public double getMaxManna() { return maxManna; }
    public void setMaxManna(double maxManna) {
        this.maxManna = maxManna;
        if (this.currentManna > maxManna) {
            this.currentManna = maxManna;
        }
    }

    public double getMaxPszi() { return maxPszi; }
    public void setMaxPszi(double maxPszi) {
        this.maxPszi = maxPszi;
        if (this.currentPszi > maxPszi) {
            this.currentPszi = maxPszi;
        }
    }

    public double getCurrentManna() { return currentManna; }
    public void setCurrentManna(double currentManna) {
        this.currentManna = Math.max(0, Math.min(currentManna, maxManna));
    }

    public double getCurrentPszi() { return currentPszi; }
    public void setCurrentPszi(double currentPszi) {
        this.currentPszi = Math.max(0, Math.min(currentPszi, maxPszi));
    }

    public long getCurrentTimeSegments() { return currentTimeSegments; }
    public void setCurrentTimeSegments(long currentTimeSegments) {
        this.currentTimeSegments = Math.max(0, currentTimeSegments);
    }

    public double getMagicExhaustion() { return magicExhaustion; }
    public void setMagicExhaustion(double magicExhaustion) {
        this.magicExhaustion = magicExhaustion;
    }
}
