package com.lactaoen.blackjack.model.wrapper;

public class UnregistrationWrapper {

    private String playerId;

    public UnregistrationWrapper() {
    }

    public UnregistrationWrapper(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
