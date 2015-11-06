package com.lactaoen.blackjack.model.wrapper;

import com.lactaoen.blackjack.model.PlayerInfo;

import java.util.List;

public class PlayerListWrapper {

    private List<PlayerInfo> players;

    public PlayerListWrapper() {
    }

    public PlayerListWrapper(List<PlayerInfo> players) {
        this.players = players;
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerInfo> players) {
        this.players = players;
    }

    public String getMessageType() {
        return "playerListWrapper";
    }
}
