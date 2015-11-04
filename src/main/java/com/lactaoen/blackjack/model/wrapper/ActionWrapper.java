package com.lactaoen.blackjack.model.wrapper;

import com.lactaoen.blackjack.model.Action;

public class ActionWrapper {

    private String playerId;
    private int handNum;
    private Action action;

    public ActionWrapper() {
    }

    public ActionWrapper(String playerId, int handNum, Action action) {
        this.playerId = playerId;
        this.handNum = handNum;
        this.action = action;
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

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
