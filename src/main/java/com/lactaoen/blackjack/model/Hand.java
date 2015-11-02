package com.lactaoen.blackjack.model;

import java.util.ArrayList;
import java.util.List;

public class Hand {

    private int betAmount;
    private List<Card> cards;
    private boolean isTurn;
    private Result result;

    public Hand() {
        betAmount = 0;
        cards = new ArrayList<>();
        isTurn = false;
        result = null;
    }

    public Hand(int betAmount) {
        this.betAmount = betAmount;
        cards = new ArrayList<>();
        isTurn = false;
        result = null;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public boolean isTurn() {
        return isTurn;
    }

    public void setTurn(boolean isTurn) {
        this.isTurn = isTurn;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public void switchIsTurn() {
        isTurn = !isTurn;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public int getHandValue() {
        // Add to the sum all cards in hand that are not Aces
        int sum = cards.stream().filter(c -> c.getRank() != Rank.ACE).mapToInt(t -> t.getRank().getValue()).sum();

        int aceCount = (int) cards.stream().filter(c -> c.getRank() == Rank.ACE).count();

        // Add Ace as hard for now
        sum += (aceCount * Rank.ACE.getValue());

        if (aceCount > 0 && sum > 21) {
            // Make aces soft one by one until we have a value that's <= 21
            for (int i = 1; i <= aceCount; i++) {
                sum -= 10;
                if (sum <= 21) {
                    break;
                }
            }
        }
        return sum;
    }

    public boolean isSplittable() {
        return cards.size() == 2 && cards.get(0).getRank() == cards.get(1).getRank();
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && cards.stream().mapToInt(c -> c.getRank().getValue()).sum() == 21;
    }
}
