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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
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
        GameSessionEntity entity = toEntity(request.getCharacterName(), request.getCurrentState());
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

    @PostMapping("/action/{characterName}")
    public ActionResponse performAndLogAction(@PathVariable String characterName,
                                               @RequestBody ActionRequest request) {
        ActionResponse response = calculationService.performAction(request);

        // Log the action if it was successful (not a warning)
        if (!response.isWarning()) {
            ActionLogEntity log = new ActionLogEntity();
            log.setCharacterName(characterName);
            log.setActionType(request.getActionType());
            log.setAmount(request.getAmount());
            log.setTimeSegments(request.getTimeSegments());
            log.setActivityType(request.getActivityType());
            log.setMaxPainPointsAffected(request.isMaxPainPointsAffected());
            log.setForced(request.isForceAction());
            log.setDescription(buildDescription(request));
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

    private GameSessionEntity toEntity(String characterName, GameState state) {
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
        return entity;
    }
}
