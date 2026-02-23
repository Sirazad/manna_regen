package com.manna.regen.model;

/**
 * Request body for the skip-time action.
 */
public class SkipTimeRequest {

    /** Duration to skip in segments (1 segment = 1 second). */
    private long timeSegments;

    /** Activity performed during the skipped time. */
    private ActivityType activity;

    /** Whether max pain points are affected (disables magic exhaustion refill). */
    private boolean maxPainPointsAffected;

    public long getTimeSegments() { return timeSegments; }
    public void setTimeSegments(long timeSegments) { this.timeSegments = timeSegments; }

    public ActivityType getActivity() { return activity; }
    public void setActivity(ActivityType activity) { this.activity = activity; }

    public boolean isMaxPainPointsAffected() { return maxPainPointsAffected; }
    public void setMaxPainPointsAffected(boolean maxPainPointsAffected) {
        this.maxPainPointsAffected = maxPainPointsAffected;
    }
}
