package com.mannaregen.service;

import com.mannaregen.model.*;
import org.springframework.stereotype.Service;

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
     * Returns 0 if stamina is below 3 (no recovery).
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
     * Perform an action and return the response with updated state.
     */
    public ActionResponse performAction(ActionRequest request) {
        GameState state = copyState(request.getCurrentState());

        return switch (request.getActionType()) {
            case SPEND_PSZI -> handleSpendPszi(state, request);
            case SPEND_MANA -> handleSpendMana(state, request);
            case SKIP_TIME -> handleSkipTime(state, request);
        };
    }

    private ActionResponse handleSpendPszi(GameState state, ActionRequest request) {
        int amount = request.getAmount();
        double newExhaustion = state.getMagicExhaustionLimit() - amount;

        // Check if magic exhaustion would go below 0
        if (newExhaustion < 0 && !request.isForceAction()) {
            return new ActionResponse(state,
                    "Magic exhaustion limit would go below 0 (to " +
                            String.format("%.1f", newExhaustion) +
                            "). Do you want to proceed?");
        }

        state.setCurrentPszi(Math.max(0, state.getCurrentPszi() - amount));
        state.setMagicExhaustionLimit(newExhaustion);
        state.setCurrentTimeSegments(state.getCurrentTimeSegments() + request.getTimeSegments());

        return new ActionResponse(state);
    }

    private ActionResponse handleSpendMana(GameState state, ActionRequest request) {
        int amount = request.getAmount();
        double newExhaustion = state.getMagicExhaustionLimit() - amount;

        // Check if magic exhaustion would go below 0
        if (newExhaustion < 0 && !request.isForceAction()) {
            return new ActionResponse(state,
                    "Magic exhaustion limit would go below 0 (to " +
                            String.format("%.1f", newExhaustion) +
                            "). Do you want to proceed?");
        }

        state.setCurrentManna(Math.max(0, state.getCurrentManna() - amount));
        state.setMagicExhaustionLimit(newExhaustion);
        state.setCurrentTimeSegments(state.getCurrentTimeSegments() + request.getTimeSegments());

        return new ActionResponse(state);
    }

    private ActionResponse handleSkipTime(GameState state, ActionRequest request) {
        long timeSegments = request.getTimeSegments();
        ActivityType activity = request.getActivityType();
        boolean painAffected = request.isMaxPainPointsAffected();

        // Pszi regeneration
        double psziRegen = calculatePsziRegen(state.getMaxPszi(), timeSegments, activity);
        int newPszi = (int) Math.min(state.getMaxPszi(),
                state.getCurrentPszi() + psziRegen);
        state.setCurrentPszi(newPszi);

        // Magic exhaustion recovery
        double exhaustionRecovery = calculateMagicExhaustionRecovery(
                state.getStamina(), state.getLevel(), timeSegments,
                activity, painAffected);
        double maxExhaustion = state.getMaxMagicExhaustion();
        double newExhaustion = Math.min(maxExhaustion,
                state.getMagicExhaustionLimit() + exhaustionRecovery);
        state.setMagicExhaustionLimit(newExhaustion);

        // Advance time
        state.setCurrentTimeSegments(state.getCurrentTimeSegments() + timeSegments);

        return new ActionResponse(state);
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
        return copy;
    }
}
