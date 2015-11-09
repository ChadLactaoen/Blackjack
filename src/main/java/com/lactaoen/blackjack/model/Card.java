package com.lactaoen.blackjack.model;

public class Card {

    private Rank rank;
    private Suit suit;

    public Card() {
    }

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public void setSuit(Suit suit) {
        this.suit = suit;
    }

    public char getAlias() {
        switch(rank) {
            case TEN:
                return 'T';
            case JACK:
                return 'J';
            case QUEEN:
                return 'Q';
            case KING:
                return 'K';
            case ACE:
                return 'A';
            default:
                return Character.forDigit(rank.getValue(), 10);
        }
    }

    public int getCardValue() {
        return rank.getValue();
    }

    @Override
    public String toString() {
        return rank + " OF " + suit;
    }
}
