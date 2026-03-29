package com.mannaregen.controller;

import com.mannaregen.entity.CharacterEntity;
import com.mannaregen.entity.GameSessionEntity;
import com.mannaregen.model.GameState;
import com.mannaregen.model.WielderType;
import com.mannaregen.repository.CharacterRepository;
import com.mannaregen.repository.GameSessionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterRepository characterRepository;
    private final GameSessionRepository sessionRepository;

    public CharacterController(CharacterRepository characterRepository,
                               GameSessionRepository sessionRepository) {
        this.characterRepository = characterRepository;
        this.sessionRepository = sessionRepository;
    }

    @GetMapping
    public List<CharacterEntity> listActive() {
        return characterRepository.findByDeletedFalseOrderByNameAsc();
    }

    @GetMapping("/deleted")
    public List<CharacterEntity> listDeleted() {
        return characterRepository.findByDeletedTrueOrderByNameAsc();
    }

    @PostMapping
    public CharacterEntity create(@RequestBody CharacterEntity character) {
        character.setId(null);
        character.setDeleted(false);
        return characterRepository.save(character);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CharacterEntity> update(@PathVariable Long id,
                                                   @RequestBody CharacterEntity update) {
        return characterRepository.findById(id).map(existing -> {
            existing.setName(update.getName());
            existing.setMaxManna(update.getMaxManna());
            existing.setMaxPszi(update.getMaxPszi());
            existing.setWielderType(update.getWielderType());
            existing.setLevel(update.getLevel());
            existing.setStamina(update.getStamina());
            return ResponseEntity.ok(characterRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        return characterRepository.findById(id).map(c -> {
            c.setDeleted(true);
            characterRepository.save(c);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<CharacterEntity> restore(@PathVariable Long id) {
        return characterRepository.findById(id).map(c -> {
            c.setDeleted(false);
            return ResponseEntity.ok(characterRepository.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Load a character: returns the most recent session state if one exists,
     * otherwise returns a fully-rested state derived from the character's base stats.
     */
    @GetMapping("/{id}/load")
    public ResponseEntity<GameState> load(@PathVariable Long id) {
        return characterRepository.findById(id).map(character -> {
            List<GameSessionEntity> sessions = sessionRepository
                    .findByCharacterNameOrderBySavedAtDesc(character.getName());

            GameState state = new GameState();
            state.setMaxManna(character.getMaxManna());
            state.setMaxPszi(character.getMaxPszi());
            state.setWielderType(character.getWielderType() != null
                    ? character.getWielderType() : WielderType.NONE);
            state.setLevel(character.getLevel());
            state.setStamina(character.getStamina());

            if (!sessions.isEmpty()) {
                GameSessionEntity last = sessions.get(0);
                state.setCurrentManna(last.getCurrentManna());
                state.setCurrentPszi(last.getCurrentPszi());
                state.setMagicExhaustionLimit(last.getMagicExhaustionLimit());
                state.setCurrentTimeSegments(last.getCurrentTimeSegments());
                state.setRestingUntilSegments(last.getRestingUntilSegments());
            } else {
                state.setCurrentManna(character.getMaxManna());
                state.setCurrentPszi(character.getMaxPszi());
                state.setMagicExhaustionLimit(25.0 * character.getLevel());
                state.setCurrentTimeSegments(0);
            }

            return ResponseEntity.ok(state);
        }).orElse(ResponseEntity.notFound().build());
    }
}
