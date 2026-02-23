package com.mannaregen.service;

import com.mannaregen.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculationServiceTest {

    private CalculationService service;

    @BeforeEach
    void setUp() {
        service = new CalculationService();
    }

    // --- Pszi regen tests ---

    @Test
    void deepMeditationFillsInThirtyMinutes() {
        // 30 max pszi, deep meditation, 30 minutes = 1800 segments -> should fill fully
        double regen = service.calculatePsziRegen(30, 1800, ActivityType.DEEP_MEDITATION);
        assertEquals(30.0, regen, 0.01);
    }

    @Test
    void sleepFillsInTwoAndHalfHours() {
        // 30 max pszi, sleep, 150 minutes = 9000 segments -> should fill fully
        double regen = service.calculatePsziRegen(30, 9000, ActivityType.SLEEP);
        assertEquals(30.0, regen, 0.01);
    }

    @Test
    void sittingPsziRegenRate() {
        // 30 max pszi, sitting, 320 minutes = 19200 segments -> should fill fully
        double regen = service.calculatePsziRegen(30, 19200, ActivityType.SITTING);
        assertEquals(30.0, regen, 0.01);
    }

    @Test
    void comfortableWalkPsziRegenRate() {
        // 30 max pszi, walking, 675 minutes = 40500 segments
        double regen = service.calculatePsziRegen(30, 40500, ActivityType.COMFORTABLE_WALK);
        assertEquals(30.0, regen, 0.01);
    }

    @Test
    void fightingPsziRegenRate() {
        // 30 max pszi, fighting, 960 minutes = 57600 segments
        double regen = service.calculatePsziRegen(30, 57600, ActivityType.SPEED_RIDING_FIGHT);
        assertEquals(30.0, regen, 0.01);
    }

    @Test
    void partialPsziRegen() {
        // 30 max pszi, deep meditation, 1 minute = 60 segments -> 1 pszi
        double regen = service.calculatePsziRegen(30, 60, ActivityType.DEEP_MEDITATION);
        assertEquals(1.0, regen, 0.01);
    }

    @Test
    void zeroPsziRegenForZeroMax() {
        double regen = service.calculatePsziRegen(0, 1800, ActivityType.DEEP_MEDITATION);
        assertEquals(0.0, regen, 0.01);
    }

    // --- Magic exhaustion recovery tests ---

    @Test
    void noRecoveryForLowStamina() {
        assertEquals(0.0, service.getMagicExhaustionRecoveryPerMinute(2, 10));
        assertEquals(0.0, service.getMagicExhaustionRecoveryPerMinute(3, 10));
    }

    @Test
    void recoveryForStamina4to5() {
        // level / 33 per minute
        double rate = service.getMagicExhaustionRecoveryPerMinute(4, 33);
        assertEquals(1.0, rate, 0.01);
    }

    @Test
    void recoveryForStamina6to7() {
        // level / 25 per minute
        double rate = service.getMagicExhaustionRecoveryPerMinute(6, 25);
        assertEquals(1.0, rate, 0.01);
    }

    @Test
    void recoveryForStamina19to20() {
        // level / 1 per minute
        double rate = service.getMagicExhaustionRecoveryPerMinute(19, 10);
        assertEquals(10.0, rate, 0.01);
    }

    @Test
    void recoveryForStamina21to22() {
        // level * 2 per minute
        double rate = service.getMagicExhaustionRecoveryPerMinute(21, 10);
        assertEquals(20.0, rate, 0.01);
    }

    @Test
    void noRecoveryDuringFight() {
        double recovery = service.calculateMagicExhaustionRecovery(
                10, 10, 600, ActivityType.SPEED_RIDING_FIGHT, false);
        assertEquals(0.0, recovery);
    }

    @Test
    void noRecoveryWhenPainPointsAffected() {
        double recovery = service.calculateMagicExhaustionRecovery(
                10, 10, 600, ActivityType.SLEEP, true);
        assertEquals(0.0, recovery);
    }

    @Test
    void recoveryDuringSleep() {
        // stamina 10, level 12, 10 minutes (600 segments), sleeping
        // rate = 12/12 = 1 per minute, 10 minutes = 10
        double recovery = service.calculateMagicExhaustionRecovery(
                10, 12, 600, ActivityType.SLEEP, false);
        assertEquals(10.0, recovery, 0.01);
    }

    // --- Action tests ---

    @Test
    void spendPsziDeductsAndAdvancesTime() {
        GameState state = createTestState();
        state.setCurrentPszi(20);
        state.setMagicExhaustionLimit(25);

        ActionRequest req = new ActionRequest();
        req.setActionType(ActionType.SPEND_PSZI);
        req.setAmount(5);
        req.setTimeSegments(4); // 1 round
        req.setCurrentState(state);

        ActionResponse resp = service.performAction(req);

        assertFalse(resp.isWarning());
        assertEquals(15, resp.getUpdatedState().getCurrentPszi());
        assertEquals(20.0, resp.getUpdatedState().getMagicExhaustionLimit(), 0.01);
        assertEquals(4, resp.getUpdatedState().getCurrentTimeSegments());
    }

    @Test
    void spendManaDeductsAndAdvancesTime() {
        GameState state = createTestState();
        state.setCurrentManna(30);
        state.setMagicExhaustionLimit(25);

        ActionRequest req = new ActionRequest();
        req.setActionType(ActionType.SPEND_MANA);
        req.setAmount(10);
        req.setTimeSegments(2); // half round
        req.setCurrentState(state);

        ActionResponse resp = service.performAction(req);

        assertFalse(resp.isWarning());
        assertEquals(20, resp.getUpdatedState().getCurrentManna());
        assertEquals(15.0, resp.getUpdatedState().getMagicExhaustionLimit(), 0.01);
        assertEquals(2, resp.getUpdatedState().getCurrentTimeSegments());
    }

    @Test
    void spendPsziWarnsWhenExhaustionBelowZero() {
        GameState state = createTestState();
        state.setCurrentPszi(20);
        state.setMagicExhaustionLimit(3);

        ActionRequest req = new ActionRequest();
        req.setActionType(ActionType.SPEND_PSZI);
        req.setAmount(5);
        req.setTimeSegments(4);
        req.setCurrentState(state);

        ActionResponse resp = service.performAction(req);

        assertTrue(resp.isWarning());
        assertNotNull(resp.getWarningMessage());
        // State should not be changed when warning
        assertEquals(20, resp.getUpdatedState().getCurrentPszi());
    }

    @Test
    void spendPsziForceActionBelowZero() {
        GameState state = createTestState();
        state.setCurrentPszi(20);
        state.setMagicExhaustionLimit(3);

        ActionRequest req = new ActionRequest();
        req.setActionType(ActionType.SPEND_PSZI);
        req.setAmount(5);
        req.setTimeSegments(4);
        req.setForceAction(true);
        req.setCurrentState(state);

        ActionResponse resp = service.performAction(req);

        assertFalse(resp.isWarning());
        assertEquals(15, resp.getUpdatedState().getCurrentPszi());
        assertEquals(-2.0, resp.getUpdatedState().getMagicExhaustionLimit(), 0.01);
    }

    @Test
    void skipTimeRegeneratesPsziAndExhaustion() {
        GameState state = createTestState();
        state.setLevel(1);
        state.setStamina(10);
        state.setMaxPszi(30);
        state.setCurrentPszi(0);
        state.setMagicExhaustionLimit(0);

        ActionRequest req = new ActionRequest();
        req.setActionType(ActionType.SKIP_TIME);
        req.setActivityType(ActivityType.DEEP_MEDITATION);
        req.setTimeSegments(1800); // 30 minutes
        req.setCurrentState(state);

        ActionResponse resp = service.performAction(req);

        assertFalse(resp.isWarning());
        assertEquals(30, resp.getUpdatedState().getCurrentPszi());
        // Magic exhaustion: stamina 10, level 1, rate = 1/12 per min, 30 min = 2.5
        assertEquals(2.5, resp.getUpdatedState().getMagicExhaustionLimit(), 0.01);
        assertEquals(1800, resp.getUpdatedState().getCurrentTimeSegments());
    }

    @Test
    void skipTimeCapsPsziAtMax() {
        GameState state = createTestState();
        state.setLevel(1);
        state.setStamina(20);
        state.setMaxPszi(10);
        state.setCurrentPszi(8);

        ActionRequest req = new ActionRequest();
        req.setActionType(ActionType.SKIP_TIME);
        req.setActivityType(ActivityType.DEEP_MEDITATION);
        req.setTimeSegments(1800); // 30 minutes - would fill fully from 0
        req.setCurrentState(state);

        ActionResponse resp = service.performAction(req);
        assertEquals(10, resp.getUpdatedState().getCurrentPszi());
    }

    @Test
    void skipTimeCapsExhaustionAtMaxLevel() {
        GameState state = createTestState();
        state.setLevel(1);
        state.setStamina(20);
        state.setMaxPszi(10);
        state.setCurrentPszi(0);
        state.setMagicExhaustionLimit(24);

        ActionRequest req = new ActionRequest();
        req.setActionType(ActionType.SKIP_TIME);
        req.setActivityType(ActivityType.SLEEP);
        req.setTimeSegments(3600); // 1 hour
        req.setCurrentState(state);

        ActionResponse resp = service.performAction(req);
        // Max exhaustion is 25 * 1 = 25
        assertEquals(25.0, resp.getUpdatedState().getMagicExhaustionLimit(), 0.01);
    }

    private GameState createTestState() {
        GameState state = new GameState();
        state.setMaxManna(50);
        state.setCurrentManna(50);
        state.setMaxPszi(30);
        state.setCurrentPszi(30);
        state.setWielderType(WielderType.MAGE);
        state.setLevel(1);
        state.setStamina(10);
        state.setMagicExhaustionLimit(25);
        state.setCurrentTimeSegments(0);
        return state;
    }
}
