package com.mannaregen.model;

/**
 * Response after performing an action. Contains the updated game state,
 * any warnings, and a summary of effects.
 */
public class ActionResponse {
    private GameState updatedState;
    private boolean warning;
    private boolean error;
    private String warningMessage;
    private String effects;

    public ActionResponse() {
    }

    public ActionResponse(GameState updatedState) {
        this.updatedState = updatedState;
        this.warning = false;
    }

    public ActionResponse(GameState updatedState, String warningMessage) {
        this.updatedState = updatedState;
        this.warning = true;
        this.warningMessage = warningMessage;
    }

    public GameState getUpdatedState() { return updatedState; }
    public void setUpdatedState(GameState updatedState) { this.updatedState = updatedState; }

    public boolean isWarning() { return warning; }
    public void setWarning(boolean warning) { this.warning = warning; }

    public boolean isError() { return error; }
    public void setError(boolean error) { this.error = error; }

    public String getWarningMessage() { return warningMessage; }
    public void setWarningMessage(String warningMessage) { this.warningMessage = warningMessage; }

    public String getEffects() { return effects; }
    public void setEffects(String effects) { this.effects = effects; }
}
