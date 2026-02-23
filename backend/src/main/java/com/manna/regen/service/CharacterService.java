package com.manna.regen.service;

import com.manna.regen.model.*;
import org.springframework.stereotype.Service;

/**
 * Core service containing all game logic for manna/pszi management.
 *
 * <p>Time is measured in segments. 1 segment = 1 second, 2 segments = half-round,
 * 4 segments = 1 round, and then standard time units apply.</p>
 *
 * <p>Magic exhaustion limit = 25 * level. Current magic exhaustion starts at
 * the limit and is reduced by spending pszi/mana. It is restored during rest
 * activities (except SPEED_RIDING_FIGHT) based on stamina, and cannot exceed
 * the limit.</p>
 */
@Service
public class CharacterService {

    // Pszi restoration durations in seconds to fill max pszi
    private static final double PSZI_FILL_SECONDS_DEEP_MEDITATION = 30.0 * 60;       // 30 min
    private static final double PSZI_FILL_SECONDS_SLEEP = 150.0 * 60;               // 2.5 h
    private static final double PSZI_FILL_SECONDS_SITTING = 320.0 * 60;             // 5h 20min
    private static final double PSZI_FILL_SECONDS_WALK = 675.0 * 60;               // 11h 15min
    private static final double PSZI_FILL_SECONDS_FIGHT = 960.0 * 60;              // 16h

    private final GameCharacter character = new GameCharacter();

    /** Returns the current character state. */
    public GameCharacter getCharacter() {
        return character;
    }

    /** Resets the character to default values. */
    public GameCharacter resetCharacter() {
        character.setLevel(1.0);
        character.setStamina(10);
        character.setMagicWieldingType(MagicWieldingType.NONE);
        character.setMaxManna(100.0);
        character.setMaxPszi(50.0);
        character.setCurrentManna(100.0);
        character.setCurrentPszi(50.0);
        character.setCurrentTimeSegments(0);
        character.setMagicExhaustion(character.getMagicExhaustionLimit());
        return character;
    }

    /**
     * Updates the character's base stats. Only fields that are non-null in the request are updated.
     */
    public GameCharacter updateCharacter(CharacterUpdateRequest request) {
        if (request.getLevel() != null) {
            character.setLevel(request.getLevel());
        }
        if (request.getStamina() != null) {
            character.setStamina(request.getStamina());
        }
        if (request.getMagicWieldingType() != null) {
            character.setMagicWieldingType(request.getMagicWieldingType());
        }
        if (request.getMaxManna() != null) {
            character.setMaxManna(request.getMaxManna());
        }
        if (request.getMaxPszi() != null) {
            character.setMaxPszi(request.getMaxPszi());
        }
        if (request.getCurrentManna() != null) {
            character.setCurrentManna(request.getCurrentManna());
        }
        if (request.getCurrentPszi() != null) {
            character.setCurrentPszi(request.getCurrentPszi());
        }
        if (request.getMagicExhaustion() != null) {
            double limit = character.getMagicExhaustionLimit();
            character.setMagicExhaustion(Math.min(request.getMagicExhaustion(), limit));
        }
        return character;
    }

    /**
     * Spends pszi. Deducts amount from current pszi and from magic exhaustion,
     * and advances time by timeSegments.
     *
     * @param request the spend action parameters
     * @return ActionResponse with updated state; if magic exhaustion would go below 0
     *         and force is false, returns a warning response without changing state.
     */
    public ActionResponse spendPszi(SpendActionRequest request) {
        double newExhaustion = character.getMagicExhaustion() - request.getAmount();
        if (newExhaustion < 0 && !request.isForce()) {
            return new ActionResponse(
                character,
                String.format(
                    "Performing this action would bring magic exhaustion to %.2f (below 0). " +
                    "Note: while magic exhaustion is below 0, pszi cannot be increased and " +
                    "magic exhaustion cannot be refilled. Do you still want to proceed?",
                    newExhaustion
                )
            );
        }
        character.setCurrentPszi(character.getCurrentPszi() - request.getAmount());
        character.setMagicExhaustion(newExhaustion);
        character.setCurrentTimeSegments(character.getCurrentTimeSegments() + request.getTimeSegments());
        return new ActionResponse(character);
    }

    /**
     * Spends mana. Deducts amount from current manna and from magic exhaustion,
     * and advances time by timeSegments.
     *
     * @param request the spend action parameters
     * @return ActionResponse with updated state; if magic exhaustion would go below 0
     *         and force is false, returns a warning response without changing state.
     */
    public ActionResponse spendMana(SpendActionRequest request) {
        double newExhaustion = character.getMagicExhaustion() - request.getAmount();
        if (newExhaustion < 0 && !request.isForce()) {
            return new ActionResponse(
                character,
                String.format(
                    "Performing this action would bring magic exhaustion to %.2f (below 0). " +
                    "Note: while magic exhaustion is below 0, pszi cannot be increased and " +
                    "magic exhaustion cannot be refilled. Do you still want to proceed?",
                    newExhaustion
                )
            );
        }
        character.setCurrentManna(character.getCurrentManna() - request.getAmount());
        character.setMagicExhaustion(newExhaustion);
        character.setCurrentTimeSegments(character.getCurrentTimeSegments() + request.getTimeSegments());
        return new ActionResponse(character);
    }

    /**
     * Skips time, applying pszi and magic exhaustion regeneration based on activity and stamina.
     *
     * <p>Pszi is restored at a rate determined by the activity type. Magic exhaustion is
     * restored based on stamina level (except during SPEED_RIDING_FIGHT, or when
     * maxPainPointsAffected is true, or when current magic exhaustion is already below 0).</p>
     *
     * @param request the skip-time parameters
     * @return ActionResponse with updated state
     */
    public ActionResponse skipTime(SkipTimeRequest request) {
        long segments = request.getTimeSegments();
        double durationSeconds = segments; // 1 segment = 1 second

        // Calculate pszi restoration
        double psziPerSecond = getPsziRestorationRatePerSecond(request.getActivity());
        double psziGain = psziPerSecond * durationSeconds;

        // Only restore pszi if magic exhaustion is not below 0
        double currentExhaustion = character.getMagicExhaustion();
        if (currentExhaustion >= 0) {
            double newPszi = Math.min(character.getCurrentPszi() + psziGain, character.getMaxPszi());
            character.setCurrentPszi(newPszi);
        }

        // Calculate magic exhaustion restoration
        boolean canRestoreExhaustion = !request.isMaxPainPointsAffected()
            && request.getActivity() != ActivityType.SPEED_RIDING_FIGHT
            && currentExhaustion >= 0;

        if (canRestoreExhaustion) {
            double exhaustionPerSecond = getMagicExhaustionRestorationRatePerSecond(
                character.getStamina(), character.getLevel()
            );
            double exhaustionGain = exhaustionPerSecond * durationSeconds;
            double limit = character.getMagicExhaustionLimit();
            double newExhaustion = Math.min(currentExhaustion + exhaustionGain, limit);
            character.setMagicExhaustion(newExhaustion);
        }

        // Advance time
        character.setCurrentTimeSegments(character.getCurrentTimeSegments() + segments);
        return new ActionResponse(character);
    }

    /**
     * Returns the pszi restoration rate per second for a given activity.
     * The rate is expressed as a fraction of max pszi per second, so total
     * restoration = rate * maxPszi * durationSeconds.
     */
    public double getPsziRestorationRatePerSecond(ActivityType activity) {
        double fillSeconds = switch (activity) {
            case DEEP_MEDITATION -> PSZI_FILL_SECONDS_DEEP_MEDITATION;
            case SLEEP -> PSZI_FILL_SECONDS_SLEEP;
            case SITTING -> PSZI_FILL_SECONDS_SITTING;
            case COMFORTABLE_WALK -> PSZI_FILL_SECONDS_WALK;
            case SPEED_RIDING_FIGHT -> PSZI_FILL_SECONDS_FIGHT;
        };
        return character.getMaxPszi() / fillSeconds;
    }

    /**
     * Returns the magic exhaustion restoration rate per second based on stamina.
     * Rates are defined per minute; we convert by dividing by 60.
     *
     * <p>Stamina ≤ 3: no restoration.
     * Stamina 4-5:   level / 33 per minute.
     * Stamina 6-7:   level / 25 per minute.
     * Stamina 8-9:   level / 18 per minute.
     * Stamina 10:    level / 12 per minute.
     * Stamina 11-12: level / 10 per minute.
     * Stamina 13-14: level / 7 per minute.
     * Stamina 15-16: level / 5 per minute.
     * Stamina 17-18: level / 3 per minute.
     * Stamina 19-20: level / 1 per minute.
     * Stamina 21-22: level * 2 per minute.
     * Stamina 23-24: level * 3 per minute.
     * Stamina 25-26: level * 4 per minute.</p>
     */
    public double getMagicExhaustionRestorationRatePerSecond(int stamina, double level) {
        if (stamina <= 3) {
            return 0.0;
        }
        double ratePerMinute;
        if (stamina <= 5) {
            ratePerMinute = level / 33.0;
        } else if (stamina <= 7) {
            ratePerMinute = level / 25.0;
        } else if (stamina <= 9) {
            ratePerMinute = level / 18.0;
        } else if (stamina == 10) {
            ratePerMinute = level / 12.0;
        } else if (stamina <= 12) {
            ratePerMinute = level / 10.0;
        } else if (stamina <= 14) {
            ratePerMinute = level / 7.0;
        } else if (stamina <= 16) {
            ratePerMinute = level / 5.0;
        } else if (stamina <= 18) {
            ratePerMinute = level / 3.0;
        } else if (stamina <= 20) {
            ratePerMinute = level;
        } else if (stamina <= 22) {
            ratePerMinute = level * 2.0;
        } else if (stamina <= 24) {
            ratePerMinute = level * 3.0;
        } else {
            ratePerMinute = level * 4.0;
        }
        return ratePerMinute / 60.0;
    }
}
