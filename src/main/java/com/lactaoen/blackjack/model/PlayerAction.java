package com.lactaoen.blackjack.model;

import com.lactaoen.blackjack.model.Action;

public class PlayerAction {

    private String playerName;
    private Action action;

    public PlayerAction(String playerName, Action action) {
        this.playerName = playerName;
        this.action = action;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
