package com.lactaoen.blackjack.model;

/**
 * This model is used upon player registration as the return type. It is essentially a duplicate
 * of Player object, but without certain key elements that can be ignored on registration.
 */
public class PlayerInfo {

    private String playerId;
    private String name;
    private int seatNum;
    private int chips;
    private boolean isActive;

    public PlayerInfo() {
    }

    public PlayerInfo transpose(Player player) {
        playerId = player.getPlayerId();
        name = player.getName();
        seatNum = player.getSeatNum();
        chips = player.getChips();
        isActive = player.isActive();
        return this;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public void setSeatNum(int seatNum) {
        this.seatNum = seatNum;
    }

    public int getChips() {
        return chips;
    }

    public void setChips(int chips) {
        this.chips = chips;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
