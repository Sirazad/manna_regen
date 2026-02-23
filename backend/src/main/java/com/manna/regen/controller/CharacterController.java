package com.manna.regen.controller;

import com.manna.regen.model.*;
import com.manna.regen.service.CharacterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing endpoints for character management and actions.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    /** Get the current character state. */
    @GetMapping("/character")
    public ResponseEntity<GameCharacter> getCharacter() {
        return ResponseEntity.ok(characterService.getCharacter());
    }

    /** Update the character's base stats (only non-null fields are updated). */
    @PatchMapping("/character")
    public ResponseEntity<GameCharacter> updateCharacter(@RequestBody CharacterUpdateRequest request) {
        return ResponseEntity.ok(characterService.updateCharacter(request));
    }

    /** Reset the character to default values. */
    @PostMapping("/character/reset")
    public ResponseEntity<GameCharacter> resetCharacter() {
        return ResponseEntity.ok(characterService.resetCharacter());
    }

    /** Spend pszi: deduct amount and advance time. */
    @PostMapping("/action/spend-pszi")
    public ResponseEntity<ActionResponse> spendPszi(@RequestBody SpendActionRequest request) {
        return ResponseEntity.ok(characterService.spendPszi(request));
    }

    /** Spend mana: deduct amount and advance time. */
    @PostMapping("/action/spend-mana")
    public ResponseEntity<ActionResponse> spendMana(@RequestBody SpendActionRequest request) {
        return ResponseEntity.ok(characterService.spendMana(request));
    }

    /** Skip time: restore pszi and magic exhaustion based on activity and stamina. */
    @PostMapping("/action/skip-time")
    public ResponseEntity<ActionResponse> skipTime(@RequestBody SkipTimeRequest request) {
        return ResponseEntity.ok(characterService.skipTime(request));
    }
}
