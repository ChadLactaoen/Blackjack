package com.lactaoen.blackjack.model.wrapper;

public class BetWrapper {

    private String playerId;
    private int betAmount;

    public BetWrapper() {
    }

    public BetWrapper(String playerId, int betAmount) {
        this.playerId = playerId;
        this.betAmount = betAmount;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }
}
