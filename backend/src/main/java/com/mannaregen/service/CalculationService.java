package com.mannaregen.service;

import com.mannaregen.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Core calculation service for manna/pszi regeneration,
 * magic exhaustion, and time management.
 */
@Service
public class CalculationService {

    /**
     * Get the total minutes needed to fully regenerate pszi from 0 to max
     * for a given activity type.
     */
    public double getPsziFillMinutes(ActivityType activity) {
        return switch (activity) {
            case DEEP_MEDITATION -> 30.0;
            case SLEEP -> 150.0;           // 2.5 hours
            case SITTING -> 320.0;          // 5 hours 20 minutes
            case COMFORTABLE_WALK -> 675.0; // 11 hours 15 minutes
            case SPEED_RIDING_FIGHT -> 960.0; // 16 hours
        };
    }

    /**
     * Calculate pszi regenerated over a given number of segments for an activity.
     *
     * @param maxPszi       the character's max pszi
     * @param timeSegments  duration in segments (1 segment = 1 second)
     * @param activity      the activity being performed
     * @return the amount of pszi regenerated (fractional)
     */
    public double calculatePsziRegen(int maxPszi, long timeSegments, ActivityType activity) {
        if (maxPszi <= 0) {
            return 0;
        }
        double totalMinutesToFill = getPsziFillMinutes(activity);
        double regenPerMinute = (double) maxPszi / totalMinutesToFill;
        double elapsedMinutes = timeSegments / 60.0;
        return regenPerMinute * elapsedMinutes;
    }

    /**
     * Get the magic exhaustion recovery rate per minute based on stamina and level.
     * Returns 0 if stamina is below 4 (no recovery).
     */
    public double getMagicExhaustionRecoveryPerMinute(int stamina, double level) {
        if (stamina < 4) {
            return 0.0;
        }
        if (stamina <= 5) {
            return level / 33.0;
        }
        if (stamina <= 7) {
            return level / 25.0;
        }
        if (stamina <= 9) {
            return level / 18.0;
        }
        if (stamina == 10) {
            return level / 12.0;
        }
        if (stamina <= 12) {
            return level / 10.0;
        }
        if (stamina <= 14) {
            return level / 7.0;
        }
        if (stamina <= 16) {
            return level / 5.0;
        }
        if (stamina <= 18) {
            return level / 3.0;
        }
        if (stamina <= 20) {
            return level;
        }
        if (stamina <= 22) {
            return level * 2.0;
        }
        if (stamina <= 24) {
            return level * 3.0;
        }
        if (stamina <= 26) {
            return level * 4.0;
        }
        // Above 26 - use maximum rate
        return level * 4.0;
    }

    /**
     * Calculate magic exhaustion recovered over time during a skip action.
     * Recovery does not apply during SPEED_RIDING_FIGHT or when pain points affected.
     */
    public double calculateMagicExhaustionRecovery(int stamina, double level,
                                                    long timeSegments, ActivityType activity,
                                                    boolean maxPainPointsAffected) {
        if (activity == ActivityType.SPEED_RIDING_FIGHT) {
            return 0.0;
        }
        if (maxPainPointsAffected) {
            return 0.0;
        }
        double recoveryPerMinute = getMagicExhaustionRecoveryPerMinute(stamina, level);
        double elapsedMinutes = timeSegments / 60.0;
        return recoveryPerMinute * elapsedMinutes;
    }

    /**
     * Perform an action and return the response with updated state and effects summary.
     */
    public ActionResponse performAction(ActionRequest request) {
        GameState before = copyState(request.getCurrentState());
        GameState state = copyState(request.getCurrentState());

        ActionResponse response = switch (request.getActionType()) {
            case SPEND_PSZI -> handleSpendPszi(state, request);
            case SPEND_MANA -> handleSpendMana(state, request);
            case SKIP_TIME -> handleSkipTime(state, request);
        };

        if (!response.isWarning()) {
            response.setEffects(buildEffects(before, response.getUpdatedState()));
        }

        return response;
    }

    private int calculateRestingPeriod(int amount, int max) {
        if (max <= 0) return 1;
        double pct = (double) amount / max * 100.0;
        if (pct <= 25.0) return 1;
        if (pct <= 75.0) return 2;
        if (pct <= 100.0) return 3;
        return 60; // 1 minute
    }

    private ActionResponse handleSpendPszi(GameState state, ActionRequest request) {
        int amount = request.getAmount();

        // Hard error: resting period active
        if (state.getCurrentTimeSegments() < state.getRestingUntilSegments()) {
            long remaining = state.getRestingUntilSegments() - state.getCurrentTimeSegments();
            ActionResponse error = new ActionResponse(state,
                    "Character is still resting for " + remaining + " more segment(s).");
            error.setError(true);
            return error;
        }

        if (amount > state.getCurrentPszi()) {
            int overage = amount - state.getCurrentPszi();
            ActionResponse error = new ActionResponse(state,
                    "You want to spend " + overage + " more pszi than you have available.");
            error.setError(true);
            return error;
        }

        double newExhaustion = state.getMagicExhaustionLimit() - amount;
        if (newExhaustion < 0 && !request.isForceAction()) {
            return new ActionResponse(state,
                    "Magic exhaustion limit would go below 0 (to " +
                            String.format("%.1f", newExhaustion) +
                            "). Do you want to proceed?");
        }

        state.setCurrentPszi(Math.max(0, state.getCurrentPszi() - amount));
        state.setMagicExhaustionLimit(newExhaustion);
        state.setCurrentTimeSegments(state.getCurrentTimeSegments() + request.getTimeSegments());
        long restingPeriod = calculateRestingPeriod(amount, state.getMaxPszi());
        state.setRestingUntilSegments(state.getCurrentTimeSegments() + 1 + restingPeriod);

        return new ActionResponse(state);
    }

    private ActionResponse handleSpendMana(GameState state, ActionRequest request) {
        int amount = request.getAmount();

        // Hard error: resting period active
        if (state.getCurrentTimeSegments() < state.getRestingUntilSegments()) {
            long remaining = state.getRestingUntilSegments() - state.getCurrentTimeSegments();
            ActionResponse error = new ActionResponse(state,
                    "Character is still resting for " + remaining + " more segment(s).");
            error.setError(true);
            return error;
        }

        if (amount > state.getCurrentManna() && !request.isForceAction()) {
            int overage = amount - state.getCurrentManna();
            return new ActionResponse(state,
                    "You only have " + state.getCurrentManna() + " mana available and want to spend " +
                    amount + " (+" + overage + " more). Proceed?");
        }

        double newExhaustion = state.getMagicExhaustionLimit() - amount;
        if (newExhaustion < 0 && !request.isForceAction()) {
            return new ActionResponse(state,
                    "Magic exhaustion limit would go below 0 (to " +
                            String.format("%.1f", newExhaustion) +
                            "). Do you want to proceed?");
        }

        state.setCurrentManna(Math.max(0, state.getCurrentManna() - amount));
        state.setMagicExhaustionLimit(newExhaustion);
        state.setCurrentTimeSegments(state.getCurrentTimeSegments() + request.getTimeSegments());
        long restingPeriod = calculateRestingPeriod(amount, state.getMaxManna());
        state.setRestingUntilSegments(state.getCurrentTimeSegments() + 1 + restingPeriod);

        return new ActionResponse(state);
    }

    private ActionResponse handleSkipTime(GameState state, ActionRequest request) {
        long timeSegments = request.getTimeSegments();
        ActivityType activity = request.getActivityType();
        boolean painAffected = request.isMaxPainPointsAffected();

        double psziRegen = calculatePsziRegen(state.getMaxPszi(), timeSegments, activity);
        int newPszi = (int) Math.min(state.getMaxPszi(),
                state.getCurrentPszi() + psziRegen);
        state.setCurrentPszi(newPszi);

        double exhaustionRecovery = calculateMagicExhaustionRecovery(
                state.getStamina(), state.getLevel(), timeSegments,
                activity, painAffected);
        double maxExhaustion = state.getMaxMagicExhaustion();
        double newExhaustion = Math.min(maxExhaustion,
                state.getMagicExhaustionLimit() + exhaustionRecovery);
        state.setMagicExhaustionLimit(newExhaustion);

        state.setCurrentTimeSegments(state.getCurrentTimeSegments() + timeSegments);

        return new ActionResponse(state);
    }

    private String buildEffects(GameState before, GameState after) {
        List<String> parts = new ArrayList<>();

        int psziDiff = after.getCurrentPszi() - before.getCurrentPszi();
        if (psziDiff != 0) parts.add("Pszi: " + formatIntDiff(psziDiff));

        int mannaDiff = after.getCurrentManna() - before.getCurrentManna();
        if (mannaDiff != 0) parts.add("Mana: " + formatIntDiff(mannaDiff));

        double exhaustionDiff = after.getMagicExhaustionLimit() - before.getMagicExhaustionLimit();
        if (Math.abs(exhaustionDiff) > 0.001) parts.add("Magic Exhaustion: " + formatDoubleDiff(exhaustionDiff));

        long timeDiff = after.getCurrentTimeSegments() - before.getCurrentTimeSegments();
        if (timeDiff != 0) parts.add("Time: +" + timeDiff + " seg");

        return String.join(", ", parts);
    }

    private String formatIntDiff(int diff) {
        return (diff >= 0 ? "+" : "") + diff;
    }

    private String formatDoubleDiff(double diff) {
        return (diff >= 0 ? "+" : "") + String.format("%.1f", diff);
    }

    private GameState copyState(GameState source) {
        GameState copy = new GameState();
        copy.setMaxManna(source.getMaxManna());
        copy.setCurrentManna(source.getCurrentManna());
        copy.setMaxPszi(source.getMaxPszi());
        copy.setCurrentPszi(source.getCurrentPszi());
        copy.setWielderType(source.getWielderType());
        copy.setLevel(source.getLevel());
        copy.setStamina(source.getStamina());
        copy.setMagicExhaustionLimit(source.getMagicExhaustionLimit());
        copy.setCurrentTimeSegments(source.getCurrentTimeSegments());
        copy.setRestingUntilSegments(source.getRestingUntilSegments());
        return copy;
    }
}
