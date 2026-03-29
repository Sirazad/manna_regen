package com.mannaregen.controller;

import com.mannaregen.entity.ActionLogEntity;
import com.mannaregen.entity.GameSessionEntity;
import com.mannaregen.model.*;
import com.mannaregen.repository.ActionLogRepository;
import com.mannaregen.repository.GameSessionRepository;
import com.mannaregen.service.CalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SessionController {

    private final GameSessionRepository sessionRepository;
    private final ActionLogRepository actionLogRepository;
    private final CalculationService calculationService;

    public SessionController(GameSessionRepository sessionRepository,
                             ActionLogRepository actionLogRepository,
                             CalculationService calculationService) {
        this.sessionRepository = sessionRepository;
        this.actionLogRepository = actionLogRepository;
        this.calculationService = calculationService;
    }

    @PostMapping("/session/save")
    public GameSessionEntity saveSession(@RequestBody SaveSessionRequest request) {
        GameSessionEntity entity = toSessionEntity(request.getCharacterName(), request.getCurrentState());
        return sessionRepository.save(entity);
    }

    @GetMapping("/session/list")
    public List<GameSessionEntity> listSessions() {
        return sessionRepository.findAllByOrderByCharacterNameAscSavedAtDesc();
    }

    @GetMapping("/session/{id}")
    public ResponseEntity<GameSessionEntity> getSession(@PathVariable Long id) {
        return sessionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/session/character/{name}")
    public List<GameSessionEntity> getSessionsByCharacter(@PathVariable String name) {
        return sessionRepository.findByCharacterNameOrderBySavedAtDesc(name);
    }

    @GetMapping("/history/{characterName}")
    public List<ActionLogEntity> getHistory(@PathVariable String characterName) {
        return actionLogRepository.findByCharacterNameOrderByPerformedAtDesc(characterName);
    }

    @DeleteMapping("/history/{logId}")
    public ResponseEntity<Void> deleteHistoryEntry(@PathVariable Long logId) {
        if (!actionLogRepository.existsById(logId)) {
            return ResponseEntity.notFound().build();
        }
        actionLogRepository.deleteById(logId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history/restore/{logId}")
    public ResponseEntity<GameState> restoreFromHistory(@PathVariable Long logId) {
        return actionLogRepository.findById(logId)
                .map(log -> ResponseEntity.ok(toGameState(log)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/action/{characterName}")
    public ActionResponse performAndLogAction(@PathVariable String characterName,
                                               @RequestBody ActionRequest request) {
        ActionResponse response = calculationService.performAction(request);

        if (!response.isWarning()) {
            GameState snapshot = response.getUpdatedState();
            ActionLogEntity log = new ActionLogEntity();
            log.setCharacterName(characterName);
            log.setActionType(request.getActionType());
            log.setAmount(request.getAmount());
            log.setTimeSegments(request.getTimeSegments());
            log.setActivityType(request.getActivityType());
            log.setMaxPainPointsAffected(request.isMaxPainPointsAffected());
            log.setForced(request.isForceAction());
            log.setDescription(buildDescription(request));
            log.setEffects(response.getEffects());
            log.setSnapshotMaxManna(snapshot.getMaxManna());
            log.setSnapshotCurrentManna(snapshot.getCurrentManna());
            log.setSnapshotMaxPszi(snapshot.getMaxPszi());
            log.setSnapshotCurrentPszi(snapshot.getCurrentPszi());
            log.setSnapshotWielderType(snapshot.getWielderType());
            log.setSnapshotLevel(snapshot.getLevel());
            log.setSnapshotStamina(snapshot.getStamina());
            log.setSnapshotMagicExhaustionLimit(snapshot.getMagicExhaustionLimit());
            log.setSnapshotTimeSegments(snapshot.getCurrentTimeSegments());
            log.setSnapshotRestingUntilSegments(snapshot.getRestingUntilSegments());
            actionLogRepository.save(log);
        }

        return response;
    }

    private String buildDescription(ActionRequest request) {
        return switch (request.getActionType()) {
            case SPEND_PSZI -> "Spent " + request.getAmount() + " pszi over " +
                    request.getTimeSegments() + " segments";
            case SPEND_MANA -> "Spent " + request.getAmount() + " mana over " +
                    request.getTimeSegments() + " segments";
            case SKIP_TIME -> "Skipped " + request.getTimeSegments() + " segments (" +
                    (request.getActivityType() != null ? request.getActivityType().name() : "unknown") + ")";
        };
    }

    private GameSessionEntity toSessionEntity(String characterName, GameState state) {
        GameSessionEntity entity = new GameSessionEntity();
        entity.setCharacterName(characterName);
        entity.setMaxManna(state.getMaxManna());
        entity.setCurrentManna(state.getCurrentManna());
        entity.setMaxPszi(state.getMaxPszi());
        entity.setCurrentPszi(state.getCurrentPszi());
        entity.setWielderType(state.getWielderType());
        entity.setLevel(state.getLevel());
        entity.setStamina(state.getStamina());
        entity.setMagicExhaustionLimit(state.getMagicExhaustionLimit());
        entity.setCurrentTimeSegments(state.getCurrentTimeSegments());
        entity.setRestingUntilSegments(state.getRestingUntilSegments());
        return entity;
    }

    private GameState toGameState(ActionLogEntity log) {
        GameState state = new GameState();
        state.setMaxManna(log.getSnapshotMaxManna() != null ? log.getSnapshotMaxManna() : 0);
        state.setCurrentManna(log.getSnapshotCurrentManna() != null ? log.getSnapshotCurrentManna() : 0);
        state.setMaxPszi(log.getSnapshotMaxPszi() != null ? log.getSnapshotMaxPszi() : 0);
        state.setCurrentPszi(log.getSnapshotCurrentPszi() != null ? log.getSnapshotCurrentPszi() : 0);
        state.setWielderType(log.getSnapshotWielderType() != null ? log.getSnapshotWielderType() : WielderType.NONE);
        state.setLevel(log.getSnapshotLevel() != null ? log.getSnapshotLevel() : 0);
        state.setStamina(log.getSnapshotStamina() != null ? log.getSnapshotStamina() : 0);
        state.setMagicExhaustionLimit(log.getSnapshotMagicExhaustionLimit() != null ? log.getSnapshotMagicExhaustionLimit() : 0);
        state.setCurrentTimeSegments(log.getSnapshotTimeSegments() != null ? log.getSnapshotTimeSegments() : 0);
        state.setRestingUntilSegments(log.getSnapshotRestingUntilSegments() != null ? log.getSnapshotRestingUntilSegments() : 0);
        return state;
    }
}
