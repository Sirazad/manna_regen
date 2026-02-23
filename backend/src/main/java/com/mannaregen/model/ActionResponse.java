package com.mannaregen.model;

/**
 * Response after performing an action. Contains the updated game state
 * and any warnings that were generated.
 */
public class ActionResponse {
    private GameState updatedState;
    private boolean warning;
    private String warningMessage;

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

    public GameState getUpdatedState() {
        return updatedState;
    }

    public void setUpdatedState(GameState updatedState) {
        this.updatedState = updatedState;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }
}
