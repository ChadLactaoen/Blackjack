package com.lactaoen.blackjack.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Dealer extends Player {

    public Dealer() {
    }

    public Dealer(String name, int seatNum, boolean isAuto) {
        super(name, seatNum);
        this.isAuto = isAuto;
    }

    @JsonIgnore
    @Override
    public int getSeatNum() {
        return super.getSeatNum();
    }

    @JsonIgnore
    @Override
    public int getChips() {
        return super.getChips();
    }

    @JsonIgnore
    @Override
    public int getHandsPlayed() {
        return super.getHandsPlayed();
    }

    @JsonIgnore
    @Override
    public Integer getNextBet() {
        return super.getNextBet();
    }

    @JsonIgnore
    @Override
    public boolean isActive() {
        return super.isActive();
    }

    @JsonIgnore
    @Override
    public boolean isBetInForNextRound() {
        return super.isBetInForNextRound();
    }

    private boolean isAuto;

    public boolean isAuto() {
        return isAuto;
    }

    public void setAuto(boolean isAuto) {
        this.isAuto = isAuto;
    }

    public void toggleAuto() {
        isAuto = !isAuto;
    }
}
