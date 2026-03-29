package com.mannaregen.controller;

import com.mannaregen.entity.ActionLogEntity;
import com.mannaregen.entity.GameSessionEntity;
import com.mannaregen.model.*;
import com.mannaregen.repository.ActionLogRepository;
import com.mannaregen.repository.GameSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SessionControllerTest {

    @Autowired
    private SessionController sessionController;

    @Autowired
    private GameSessionRepository sessionRepository;

    @Autowired
    private ActionLogRepository actionLogRepository;

    @BeforeEach
    void setUp() {
        actionLogRepository.deleteAll();
        sessionRepository.deleteAll();
    }

    @Test
    void saveAndLoadSession() {
        SaveSessionRequest request = new SaveSessionRequest();
        request.setCharacterName("Gandalf");

        GameState state = new GameState();
        state.setMaxManna(50);
        state.setCurrentManna(30);
        state.setMaxPszi(20);
        state.setCurrentPszi(15);
        state.setWielderType(WielderType.MAGE);
        state.setLevel(5);
        state.setStamina(10);
        state.setMagicExhaustionLimit(100);
        state.setCurrentTimeSegments(1800);
        request.setCurrentState(state);

        GameSessionEntity saved = sessionController.saveSession(request);
        assertNotNull(saved.getId());
        assertEquals("Gandalf", saved.getCharacterName());
        assertNotNull(saved.getSavedAt());

        // Load it back
        var loaded = sessionController.getSession(saved.getId());
        assertTrue(loaded.getStatusCode().is2xxSuccessful());
        var entity = loaded.getBody();
        assertNotNull(entity);
        assertEquals(50, entity.getMaxManna());
        assertEquals(30, entity.getCurrentManna());
        assertEquals(5.0, entity.getLevel());
    }

    @Test
    void listSessionsByCharacterName() {
        saveSession("Gandalf", 50, 1);
        saveSession("Gandalf", 45, 2);
        saveSession("Frodo", 10, 1);

        List<GameSessionEntity> gandalfSessions =
                sessionController.getSessionsByCharacter("Gandalf");
        assertEquals(2, gandalfSessions.size());

        List<GameSessionEntity> allSessions = sessionController.listSessions();
        assertEquals(3, allSessions.size());
    }

    @Test
    void performActionLogsHistory() {
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

        ActionRequest req = new ActionRequest();
        req.setActionType(ActionType.SPEND_PSZI);
        req.setAmount(5);
        req.setTimeSegments(4);
        req.setCurrentState(state);

        ActionResponse response = sessionController.performAndLogAction("Gandalf", req);
        assertFalse(response.isWarning());
        assertEquals(25, response.getUpdatedState().getCurrentPszi());

        // Check history
        List<ActionLogEntity> history = sessionController.getHistory("Gandalf");
        assertEquals(1, history.size());
        assertEquals(ActionType.SPEND_PSZI, history.get(0).getActionType());
        assertEquals(5, history.get(0).getAmount());
        assertTrue(history.get(0).getDescription().contains("pszi"));
    }

    @Test
    void warningActionNotLogged() {
        GameState state = new GameState();
        state.setMaxManna(50);
        state.setCurrentManna(50);
        state.setMaxPszi(30);
        state.setCurrentPszi(30);
        state.setWielderType(WielderType.MAGE);
        state.setLevel(1);
        state.setStamina(10);
        state.setMagicExhaustionLimit(3);
        state.setCurrentTimeSegments(0);

        ActionRequest req = new ActionRequest();
        req.setActionType(ActionType.SPEND_PSZI);
        req.setAmount(5);
        req.setTimeSegments(4);
        req.setCurrentState(state);

        ActionResponse response = sessionController.performAndLogAction("Gandalf", req);
        assertTrue(response.isWarning());

        // Warning actions should NOT be logged
        List<ActionLogEntity> history = sessionController.getHistory("Gandalf");
        assertEquals(0, history.size());
    }

    private void saveSession(String name, int manna, int timeSegments) {
        SaveSessionRequest request = new SaveSessionRequest();
        request.setCharacterName(name);
        GameState state = new GameState();
        state.setMaxManna(manna);
        state.setCurrentManna(manna);
        state.setMaxPszi(20);
        state.setCurrentPszi(20);
        state.setWielderType(WielderType.MAGE);
        state.setLevel(1);
        state.setStamina(10);
        state.setMagicExhaustionLimit(25);
        state.setCurrentTimeSegments(timeSegments);
        request.setCurrentState(state);
        sessionController.saveSession(request);
    }
}
