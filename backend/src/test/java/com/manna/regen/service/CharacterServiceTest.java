package com.manna.regen.service;

import com.manna.regen.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterServiceTest {

    private CharacterService service;

    @BeforeEach
    void setUp() {
        service = new CharacterService();
        // Reset to known state before each test
        service.resetCharacter();
        // Set up a known character: level 2, stamina 19, 30 pszi, 100 mana
        CharacterUpdateRequest req = new CharacterUpdateRequest();
        req.setLevel(2.0);
        req.setStamina(19);
        req.setMaxManna(100.0);
        req.setMaxPszi(30.0);
        req.setCurrentManna(100.0);
        req.setCurrentPszi(30.0);
        service.updateCharacter(req);
        // magic exhaustion limit = 25 * 2 = 50; current starts at 50
        service.getCharacter().setMagicExhaustion(50.0);
    }

    @Test
    void testMagicExhaustionLimit() {
        assertEquals(50.0, service.getCharacter().getMagicExhaustionLimit(), 0.001);
    }

    @Test
    void testSpendPsziReducesCurrentPsziAndExhaustion() {
        SpendActionRequest req = new SpendActionRequest();
        req.setAmount(5.0);
        req.setTimeSegments(4); // 1 round

        ActionResponse response = service.spendPszi(req);

        assertFalse(response.isRequiresConfirmation());
        assertEquals(25.0, response.getCharacter().getCurrentPszi(), 0.001);
        assertEquals(45.0, response.getCharacter().getMagicExhaustion(), 0.001);
        assertEquals(4L, response.getCharacter().getCurrentTimeSegments());
    }

    @Test
    void testSpendManaReducesCurrentMannaAndExhaustion() {
        SpendActionRequest req = new SpendActionRequest();
        req.setAmount(10.0);
        req.setTimeSegments(2); // half-round

        ActionResponse response = service.spendMana(req);

        assertFalse(response.isRequiresConfirmation());
        assertEquals(90.0, response.getCharacter().getCurrentManna(), 0.001);
        assertEquals(40.0, response.getCharacter().getMagicExhaustion(), 0.001);
        assertEquals(2L, response.getCharacter().getCurrentTimeSegments());
    }

    @Test
    void testSpendPsziWarningWhenExhaustionWouldGoBelowZero() {
        // Set exhaustion very low
        service.getCharacter().setMagicExhaustion(3.0);

        SpendActionRequest req = new SpendActionRequest();
        req.setAmount(5.0);
        req.setTimeSegments(4);
        req.setForce(false);

        ActionResponse response = service.spendPszi(req);

        assertTrue(response.isRequiresConfirmation());
        assertNotNull(response.getWarning());
        // State must NOT be changed
        assertEquals(30.0, response.getCharacter().getCurrentPszi(), 0.001);
        assertEquals(3.0, response.getCharacter().getMagicExhaustion(), 0.001);
    }

    @Test
    void testSpendPsziWithForceProceeds() {
        service.getCharacter().setMagicExhaustion(3.0);

        SpendActionRequest req = new SpendActionRequest();
        req.setAmount(5.0);
        req.setTimeSegments(4);
        req.setForce(true);

        ActionResponse response = service.spendPszi(req);

        assertFalse(response.isRequiresConfirmation());
        assertEquals(25.0, response.getCharacter().getCurrentPszi(), 0.001);
        assertEquals(-2.0, response.getCharacter().getMagicExhaustion(), 0.001);
    }

    @Test
    void testSkipTimeRestoresPsziDeepMeditation() {
        service.getCharacter().setCurrentPszi(0.0);

        SkipTimeRequest req = new SkipTimeRequest();
        // 30 minutes = 1800 seconds = 1800 segments to fill all 30 pszi in deep meditation
        req.setTimeSegments(1800);
        req.setActivity(ActivityType.DEEP_MEDITATION);
        req.setMaxPainPointsAffected(false);

        ActionResponse response = service.skipTime(req);

        // Should be full (or very close)
        assertEquals(30.0, response.getCharacter().getCurrentPszi(), 0.001);
    }

    @Test
    void testSkipTimePsziCappedAtMax() {
        service.getCharacter().setCurrentPszi(28.0);

        SkipTimeRequest req = new SkipTimeRequest();
        req.setTimeSegments(1800); // 30 min deep meditation
        req.setActivity(ActivityType.DEEP_MEDITATION);
        req.setMaxPainPointsAffected(false);

        ActionResponse response = service.skipTime(req);

        // Pszi must not exceed max (30)
        assertEquals(30.0, response.getCharacter().getCurrentPszi(), 0.001);
    }

    @Test
    void testSkipTimeRestoresMagicExhaustion() {
        // level=2, stamina=19 -> rate = level/1 per minute = 2 per minute = 2/60 per second
        service.getCharacter().setMagicExhaustion(0.0);

        SkipTimeRequest req = new SkipTimeRequest();
        req.setTimeSegments(60); // 1 minute = 60 segments
        req.setActivity(ActivityType.SITTING);
        req.setMaxPainPointsAffected(false);

        ActionResponse response = service.skipTime(req);

        // After 1 minute: 2 points restored (level/1 per minute, level=2)
        assertEquals(2.0, response.getCharacter().getMagicExhaustion(), 0.001);
    }

    @Test
    void testSkipTimeFightDoesNotRestoreMagicExhaustion() {
        service.getCharacter().setMagicExhaustion(10.0);

        SkipTimeRequest req = new SkipTimeRequest();
        req.setTimeSegments(3600); // 1 hour
        req.setActivity(ActivityType.SPEED_RIDING_FIGHT);
        req.setMaxPainPointsAffected(false);

        ActionResponse response = service.skipTime(req);

        // Fight should not restore magic exhaustion
        assertEquals(10.0, response.getCharacter().getMagicExhaustion(), 0.001);
    }

    @Test
    void testSkipTimeMaxPainPointsDisablesExhaustionRefill() {
        service.getCharacter().setMagicExhaustion(10.0);

        SkipTimeRequest req = new SkipTimeRequest();
        req.setTimeSegments(3600);
        req.setActivity(ActivityType.DEEP_MEDITATION);
        req.setMaxPainPointsAffected(true); // disables exhaustion refill

        ActionResponse response = service.skipTime(req);

        // Exhaustion should stay the same
        assertEquals(10.0, response.getCharacter().getMagicExhaustion(), 0.001);
    }

    @Test
    void testMagicExhaustionRestorationRateStaminaLow() {
        // stamina <= 3 -> 0
        assertEquals(0.0, service.getMagicExhaustionRestorationRatePerSecond(3, 10.0), 0.0001);
    }

    @Test
    void testMagicExhaustionRestorationRateStamina19() {
        // stamina 19-20 -> level / 1 per minute = level / 60 per second
        double rate = service.getMagicExhaustionRestorationRatePerSecond(19, 4.0);
        assertEquals(4.0 / 60.0, rate, 0.00001);
    }

    @Test
    void testMagicExhaustionRestorationRateStamina25() {
        // stamina 25-26 -> level*4 per minute
        double rate = service.getMagicExhaustionRestorationRatePerSecond(25, 5.0);
        assertEquals(5.0 * 4.0 / 60.0, rate, 0.00001);
    }

    @Test
    void testMagicExhaustionCannotExceedLimit() {
        service.getCharacter().setMagicExhaustion(49.0); // limit is 50

        SkipTimeRequest req = new SkipTimeRequest();
        req.setTimeSegments(3600); // long time
        req.setActivity(ActivityType.DEEP_MEDITATION);
        req.setMaxPainPointsAffected(false);

        ActionResponse response = service.skipTime(req);

        assertEquals(50.0, response.getCharacter().getMagicExhaustion(), 0.001);
    }

    @Test
    void testTimeAdvances() {
        SpendActionRequest req = new SpendActionRequest();
        req.setAmount(1.0);
        req.setTimeSegments(8);
        service.spendPszi(req);

        req.setTimeSegments(4);
        service.spendPszi(req);

        assertEquals(12L, service.getCharacter().getCurrentTimeSegments());
    }

    @Test
    void testUpdateCharacter() {
        CharacterUpdateRequest req = new CharacterUpdateRequest();
        req.setLevel(5.0);
        req.setStamina(15);
        req.setMagicWieldingType(MagicWieldingType.MAGE);
        req.setMaxManna(200.0);
        req.setCurrentManna(150.0);

        service.updateCharacter(req);
        GameCharacter c = service.getCharacter();

        assertEquals(5.0, c.getLevel(), 0.001);
        assertEquals(15, c.getStamina());
        assertEquals(MagicWieldingType.MAGE, c.getMagicWieldingType());
        assertEquals(200.0, c.getMaxManna(), 0.001);
        assertEquals(150.0, c.getCurrentManna(), 0.001);
    }

    @Test
    void testCurrentMannaCannotExceedMax() {
        CharacterUpdateRequest req = new CharacterUpdateRequest();
        req.setCurrentManna(200.0); // max is 100
        service.updateCharacter(req);
        assertEquals(100.0, service.getCharacter().getCurrentManna(), 0.001);
    }
}
