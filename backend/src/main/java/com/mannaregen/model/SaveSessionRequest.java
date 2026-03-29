package com.mannaregen.model;

/**
 * Request to save a game session under a character name.
 */
public class SaveSessionRequest {
    private String characterName;
    private GameState currentState;

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(GameState currentState) {
        this.currentState = currentState;
    }
}
