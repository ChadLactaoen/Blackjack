package com.lactaoen.blackjack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private List<Card> cards;

    /**
     * Initializes the Deck with one set of 52 playing cards in random order.
     */
    public Deck() {
        cards = new ArrayList<>();
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        shuffle();
    }

    /**
     * Initializes the Deck with a number of 52-count playing card sets in random order.
     *
     * @param deckCount
     *  The number of 52-count playing card sets to initialize the deck with.
     */
    public Deck(int deckCount) {
        cards = new ArrayList<>();
        for (int i = 1; i <= deckCount; i++) {
            for (Rank rank : Rank.values()) {
                for (Suit suit : Suit.values()) {
                    cards.add(new Card(rank, suit));
                }
            }
        }
        shuffle();
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card dealCard() {
        Card card = cards.get(cards.size() - 1);
        cards.remove(cards.size() - 1);
        return card;
    }

    public boolean isPastCutCard() {
        return cards.size() < 30;
    }

    public int getDeckSize() {
        return cards.size();
    }
}
