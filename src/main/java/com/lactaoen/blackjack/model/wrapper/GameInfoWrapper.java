package com.lactaoen.blackjack.model.wrapper;

import com.lactaoen.blackjack.model.*;

import java.util.List;

public class GameInfoWrapper {

    private List<Player> players;
    private Dealer dealer;
    private Card dealerUpCard;
    private Round gameStatus;
    private PlayerAction lastAction;
    private int cardsLeftInDeck;

    public GameInfoWrapper() {
    }

    public GameInfoWrapper(List<Player> players, Dealer dealer, Card dealerUpCard, Round gameStatus, PlayerAction lastAction, int cardsLeftInDeck) {
        this.players = players;
        this.dealer = dealer;
        this.dealerUpCard = dealerUpCard;
        this.gameStatus = gameStatus;
        this.lastAction = lastAction;
        this.cardsLeftInDeck = cardsLeftInDeck;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
    }

    public Card getDealerUpCard() {
        return dealerUpCard;
    }

    public void setDealerUpCard(Card dealerUpCard) {
        this.dealerUpCard = dealerUpCard;
    }

    public Round getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(Round gameStatus) {
        this.gameStatus = gameStatus;
    }

    public PlayerAction getLastAction() {
        return lastAction;
    }

    public void setLastAction(PlayerAction lastAction) {
        this.lastAction = lastAction;
    }

    public int getCardsLeftInDeck() {
        return cardsLeftInDeck;
    }

    public void setCardsLeftInDeck(int cardsLeftInDeck) {
        this.cardsLeftInDeck = cardsLeftInDeck;
    }
}
