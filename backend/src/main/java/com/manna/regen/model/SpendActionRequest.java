package com.manna.regen.model;

/**
 * Request body for spending pszi or mana.
 */
public class SpendActionRequest {

    /** Amount of pszi or mana to spend. */
    private double amount;

    /** Duration of the action in segments (1 segment = 1 second). */
    private long timeSegments;

    /** If true, proceed even when magic exhaustion would go below 0. */
    private boolean force;

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public long getTimeSegments() { return timeSegments; }
    public void setTimeSegments(long timeSegments) { this.timeSegments = timeSegments; }

    public boolean isForce() { return force; }
    public void setForce(boolean force) { this.force = force; }
}
