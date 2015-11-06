package com.lactaoen.blackjack.model.wrapper;

// Return object for get the active player's hand info.
// Used only by Trebek for admin reasons.
public class PlayerHandWrapper {

    private String playerId;
    private int handNum;

    public PlayerHandWrapper() {
    }

    public PlayerHandWrapper(String playerId, int handNum) {
        this.playerId = playerId;
        this.handNum = handNum;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getHandNum() {
        return handNum;
    }

    public void setHandNum(int handNum) {
        this.handNum = handNum;
    }

    public String getMessageType() {
        return "playerHandWrapper";
    }
}
