package com.lactaoen.blackjack.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Technically not a model since it holds state information **/
public class Game {

    private final int MAX_PLAYERS_PER_GAME = 4;

    private Deck deck;
    private Dealer dealer;
    private List<Player> players;

    public Game() {
        deck = new Deck(2);
        dealer = new Dealer("Debby the Dealer", 5, true);
        players = new ArrayList<>();
    }

    public Game(int deckCount) {
        deck = new Deck(deckCount);
        dealer = new Dealer("Debby the Dealer", 5, true);
        players = new ArrayList<>();
    }

    public Deck getDeck() {
        if (deck == null) {
            deck = new Deck(2);
        }
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Player addPlayer(String name) {
        Player player = null;
        if (players.size() < MAX_PLAYERS_PER_GAME) {
            player = new Player(name, players.size() + 1);
            players.add(player);
        }
        return player;
    }

    public Card dealCard() {
        return deck.dealCard();
    }

    public Card getDealerUpCard() {
        try {
            return dealer.getHands().get(0).getCards().get(0);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    public int getDeckSize() {
        return deck.getCards().size();
    }

    public Hand getDealerHand() {
        if (dealer.getHands().isEmpty()) {
            dealer.getHands().add(new Hand());
        }
        return dealer.getHands().get(0);
    }

    public void switchCurrentHandOffAndTurnNextHandOn() {
        // Create a hand list to keep sequential order of what hands are currently being played
        // Reason it's done on the fly in this method and not stored is because players can
        // split hands, so we need to be able to account for that
        List<Hand> hands = new ArrayList<>();
        for (Player p : players) {
            for (Hand h : p.getHands()) {
                hands.add(h);
            }
        }

        // Find the current hand being played on and declare it done
        int currentIsTurnIndex = 0;
        for (int i = 0; i < hands.size(); i++) {
            if (hands.get(i).isTurn()) {
                currentIsTurnIndex = i;
                break;
            }
        }

        hands.get(currentIsTurnIndex).switchIsTurn();
        // Switch next hand on only if it's not the last player hand left to act
        if (currentIsTurnIndex != hands.size() - 1) {
            Hand hand = hands.get(currentIsTurnIndex + 1);
            // Check if this hand is a blackjack. If it is, then skip and go to next one
            if (hand.isBlackjack()) {
                hand.switchIsTurn();
                switchCurrentHandOffAndTurnNextHandOn();
            } else {
                hand.switchIsTurn();

                // If this hand only has one card in it, it was split. Need to add one more card.
                if (hand.getCards().size() == 1) {
                    hand.addCard(dealCard());
                }
            }
        }
    }

    public boolean isBettingRoundDone() {
        return players.stream().filter(p -> p.getNextBet() != null).count()
                == players.stream().filter(Player::isActive).count();
    }

    public void dealNewHand() {
        // Restart a new deck if we're past the cut card
        if (deck.isPastCutCard()) {
            deck = new Deck(2);
        }

        // Deal each player a card one at a time, including the dealer
        for (int i = 1; i <= 2; i++) {
            players.stream().filter(Player::isActive).forEach(player -> {
                player.getHands().get(0).addCard(dealCard());
            });
            getDealerHand().addCard(dealCard());
        }
    }

    public List<Hand> getAllHands() {
        List<Hand> hands = new ArrayList<>();
        for (Player p : players) {
            hands.addAll(p.getHands().stream().collect(Collectors.toList()));
        }
        return hands;
    }

    public boolean isActionDone() {
        boolean isActionDone = true;
        for (Player p : players) {
            for (Hand h : p.getHands()) {
                if (h.isTurn()) {
                    isActionDone = false;
                    break;
                }
            }
        }
        return isActionDone;
    }
}
