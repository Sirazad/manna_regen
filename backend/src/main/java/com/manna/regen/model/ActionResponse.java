package com.manna.regen.model;

/**
 * Response returned after any action. Contains the updated character state
 * and optional warning if magic exhaustion would go below 0.
 */
public class ActionResponse {

    private GameCharacter character;
    private boolean requiresConfirmation;
    private String warning;

    public ActionResponse(GameCharacter character) {
        this.character = character;
        this.requiresConfirmation = false;
        this.warning = null;
    }

    public ActionResponse(GameCharacter character, String warning) {
        this.character = character;
        this.requiresConfirmation = true;
        this.warning = warning;
    }

    public GameCharacter getCharacter() { return character; }
    public void setCharacter(GameCharacter character) { this.character = character; }

    public boolean isRequiresConfirmation() { return requiresConfirmation; }
    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }
}
